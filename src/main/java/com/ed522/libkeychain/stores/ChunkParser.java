package com.ed522.libkeychain.stores;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

import com.ed522.libkeychain.util.Constants;

public class ChunkParser {
	
	/*
	 * Structure:
	 * 32B SALT       for block key derivation
	 * 16B IV         MUST NOT be reused
	 * 4B LEN         length of data + tag
	 * <LEN> DATA   \ these are both
	 * 16B TAG      / parsed together
	 */
	
    private static final int SALT_LENGTH = 32;
	private static final int EXTRA_DATA = 64;
	private Key masterKey;

	private static final void copyArray(byte[] src, byte[] dst, int srcOff, int dstOff, int len) {

		if (srcOff == -1) srcOff = src.length; // autodetect

		for (int i = 0; i < len; i++) {
			dst[i + dstOff] = src[i + srcOff];
		}

	}
	private static final int writeInt(int value, byte[] dst, int off) {
		ByteBuffer buf = ByteBuffer.wrap(dst);
		buf.putInt(off, value);
		return 4;
	}
	private static final int readInt(byte[] value, int off) {
		return ByteBuffer.wrap(value).getInt(off);
	}
	private static final boolean incrementByteArray(byte[] value) {

		for (int i = value.length - 1; i >= 0; i--) {
			value[i]++;
			if (value[i] != 0) return false;
		}
		return true; // overflowed to all 0

	}

	public ChunkParser(Key key) {
		this.masterKey = key;
	}

	public int newChunk(byte[] data, OutputStream output) throws IOException, GeneralSecurityException {

		DataOutputStream out = new DataOutputStream(output);

		byte[] salt = new byte[SALT_LENGTH];
		new SecureRandom().nextBytes(salt);
		out.write(salt);

		byte[] blockKeyRaw = new byte[Constants.SYMMETRIC_KEY_LENGTH_BYTES];
		HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
		hkdf.init(new HKDFParameters(masterKey.getEncoded(), salt, null));
		hkdf.generateBytes(blockKeyRaw, 0, blockKeyRaw.length);
		Key blockKey = new SecretKeySpec(blockKeyRaw, Constants.SYMMETRIC_CIPHER);

		// generate new IV
		byte[] iv = new byte[Constants.SYMMETRIC_IV_LENGTH];
		out.write(iv);
		
		out.writeInt(data.length + Constants.SYMMETRIC_TAG_LENGTH);

		Cipher cipher = Cipher.getInstance(Constants.SYMMETRIC_CIPHER);
		cipher.init(Cipher.ENCRYPT_MODE, blockKey, new IvParameterSpec(iv));

		out.write(cipher.doFinal(data));
		return EXTRA_DATA + data.length;

	}

	public int newChunk(byte[] data, RandomAccessFile out) throws IOException, GeneralSecurityException {

		out.seek(out.length());
		byte[] salt = new byte[SALT_LENGTH];
		new SecureRandom().nextBytes(salt);
		out.write(salt);

		byte[] blockKeyRaw = new byte[Constants.SYMMETRIC_KEY_LENGTH_BYTES];
		HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
		hkdf.init(new HKDFParameters(masterKey.getEncoded(), salt, null));
		hkdf.generateBytes(blockKeyRaw, 0, blockKeyRaw.length);
		Key blockKey = new SecretKeySpec(blockKeyRaw, Constants.SYMMETRIC_CIPHER);

		// generate new IV
		byte[] iv = new byte[Constants.SYMMETRIC_IV_LENGTH];
		out.write(iv);

		out.writeInt(data.length + Constants.SYMMETRIC_TAG_LENGTH);
		
		Cipher cipher = Cipher.getInstance(Constants.SYMMETRIC_CIPHER);
		cipher.init(Cipher.ENCRYPT_MODE, blockKey, new IvParameterSpec(iv));
		
		out.write(cipher.doFinal(data));
		return EXTRA_DATA + data.length;

	}

	public byte[] newChunk(byte[] data) throws IOException, GeneralSecurityException {
		byte[] out = new byte[EXTRA_DATA + data.length];

		int offset = 0;
		byte[] salt = new byte[SALT_LENGTH];
		new SecureRandom().nextBytes(salt);
		copyArray(salt, out, 0, 0, salt.length);
		offset += salt.length;

		byte[] blockKeyRaw = new byte[Constants.SYMMETRIC_KEY_LENGTH_BYTES];
		HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
		hkdf.init(new HKDFParameters(masterKey.getEncoded(), salt, null));
		hkdf.generateBytes(blockKeyRaw, 0, blockKeyRaw.length);
		Key blockKey = new SecretKeySpec(blockKeyRaw, Constants.SYMMETRIC_CIPHER);

		// gen IV
		byte[] iv = new byte[Constants.SYMMETRIC_IV_LENGTH];
		copyArray(iv, out, 0, offset, iv.length);
		offset += iv.length;

		writeInt(data.length + Constants.SYMMETRIC_TAG_LENGTH, out, offset);
		offset += Integer.BYTES;

		Cipher cipher = Cipher.getInstance(Constants.SYMMETRIC_CIPHER);
		cipher.init(Cipher.ENCRYPT_MODE, blockKey, new IvParameterSpec(iv));

		copyArray(cipher.doFinal(data), out, 0, offset, data.length + Constants.SYMMETRIC_TAG_LENGTH);

		return out;

	}

	public byte[] updateChunk(byte[] chunk, byte[] newData) throws IOException, GeneralSecurityException {

		byte[] out = new byte[EXTRA_DATA + newData.length];

		byte[] salt = new byte[SALT_LENGTH];
		copyArray(chunk, salt, 0, 0, salt.length);
		copyArray(salt, out, 0, 0, salt.length);
		int offset = salt.length;
		
		byte[] iv = new byte[Constants.SYMMETRIC_IV_LENGTH];
		copyArray(chunk, iv, offset, 0, iv.length);
		if (incrementByteArray(iv) /* true on overflow, new salt/IV */) {
			return newChunk(newData);
		}

		copyArray(iv, out, 0, offset, iv.length);
		offset += iv.length;

		byte[] blockKeyRaw = new byte[Constants.SYMMETRIC_KEY_LENGTH_BYTES];
		HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
		hkdf.init(new HKDFParameters(masterKey.getEncoded(), salt, null));
		hkdf.generateBytes(blockKeyRaw, 0, blockKeyRaw.length);
		Key blockKey = new SecretKeySpec(blockKeyRaw, Constants.SYMMETRIC_CIPHER);

		writeInt(newData.length + Constants.SYMMETRIC_TAG_LENGTH, out, offset);
		offset += Integer.BYTES;

		Cipher cipher = Cipher.getInstance(Constants.SYMMETRIC_CIPHER);
		cipher.init(Cipher.ENCRYPT_MODE, blockKey, new IvParameterSpec(iv));

		copyArray(cipher.doFinal(newData), out, 0, offset, newData.length + Constants.SYMMETRIC_TAG_LENGTH);

		return out;

	}

	public int decryptChunk(InputStream input, byte[] output) throws IOException, GeneralSecurityException {

		DataInputStream in = new DataInputStream(input);

		byte[] salt = new byte[SALT_LENGTH];
		if (in.read(salt) != SALT_LENGTH) throw new ShortBufferException(); // read salt

		byte[] iv = new byte[Constants.SYMMETRIC_IV_LENGTH];
		if (in.read(iv) != Constants.SYMMETRIC_IV_LENGTH) throw new ShortBufferException(); // read IV
		
		int len = in.readInt();
		
		byte[] blockKeyRaw = new byte[Constants.SYMMETRIC_KEY_LENGTH_BYTES];
		HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
		hkdf.init(new HKDFParameters(masterKey.getEncoded(), salt, null));
		hkdf.generateBytes(blockKeyRaw, 0, blockKeyRaw.length);
		Key blockKey = new SecretKeySpec(blockKeyRaw, Constants.SYMMETRIC_CIPHER);
		
		Cipher cipher = Cipher.getInstance(Constants.SYMMETRIC_CIPHER);
		cipher.init(Cipher.DECRYPT_MODE, blockKey, new IvParameterSpec(iv));

		cipher.doFinal(in.readNBytes(len), 0, len, output);

		return EXTRA_DATA + len;

	}

	public byte[] decryptChunk(RandomAccessFile input) throws IOException, GeneralSecurityException {

		byte[] salt = new byte[SALT_LENGTH];
		if (input.read(salt) != SALT_LENGTH) throw new ShortBufferException(); // read salt

		byte[] iv = new byte[Constants.SYMMETRIC_IV_LENGTH];
		if (input.read(iv) != Constants.SYMMETRIC_IV_LENGTH) throw new ShortBufferException(); // read IV
		
		int len = input.readInt();
		
		byte[] blockKeyRaw = new byte[Constants.SYMMETRIC_KEY_LENGTH_BYTES];
		HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
		hkdf.init(new HKDFParameters(masterKey.getEncoded(), salt, null));
		hkdf.generateBytes(blockKeyRaw, 0, blockKeyRaw.length);
		Key blockKey = new SecretKeySpec(blockKeyRaw, Constants.SYMMETRIC_CIPHER);
		
		Cipher cipher = Cipher.getInstance(Constants.SYMMETRIC_CIPHER);
		cipher.init(Cipher.DECRYPT_MODE, blockKey, new IvParameterSpec(iv));

		byte[] data = new byte[len];
		if (input.read(data) != len) throw new ShortBufferException();
		
		return cipher.doFinal(data, 0, len);

	}

	public byte[] decryptChunk(byte[] input) throws IOException, GeneralSecurityException {

		int offset = 0;
		// get salt
		byte[] salt = new byte[SALT_LENGTH];
		copyArray(input, salt, offset, 0, SALT_LENGTH);
		offset += salt.length;
		
		// get IV
		byte[] iv = new byte[Constants.SYMMETRIC_IV_LENGTH];
		copyArray(input, iv, offset, 0, Constants.SYMMETRIC_IV_LENGTH);
		offset += iv.length;
		
		// get length
		int len = readInt(input, offset);
		offset += 4;

		// decrypt data
		byte[] blockKeyRaw = new byte[Constants.SYMMETRIC_KEY_LENGTH_BYTES];
		HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
		hkdf.init(new HKDFParameters(masterKey.getEncoded(), salt, null));
		hkdf.generateBytes(blockKeyRaw, 0, blockKeyRaw.length);
		Key blockKey = new SecretKeySpec(blockKeyRaw, Constants.SYMMETRIC_CIPHER);
		
		Cipher cipher = Cipher.getInstance(Constants.SYMMETRIC_CIPHER);
		cipher.init(Cipher.DECRYPT_MODE, blockKey, new IvParameterSpec(iv));

		return cipher.doFinal(input, offset, len);

	}

	public int chunkLength(RandomAccessFile f) throws IOException {

		f.seek(44 + f.getFilePointer());
		int val = f.readInt() + 48;
		f.seek(f.getFilePointer() - 48);
		return val;

	}

}
