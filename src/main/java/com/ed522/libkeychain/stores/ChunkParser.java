package com.ed522.libkeychain.stores;

import java.io.DataInputStream;
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
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;

public class ChunkParser {
	
	/*
	 * Structure:
	 * 32B SALT       for key derivation
	 * 16B IV         MUST NOT be reused
	 * 4B LEN         length of data + tag
	 * <LEN> DATA   \ these are boths
	 * 12B TAG      / parsed together
	 */

	private static final String CIPHER_MODE = "AES/GCM/NoPadding";
	private static final int EXTRA_DATA = 64;
	private Key masterKey;

	private static final void copyArray(byte[] src, byte[] dst, int srcOff, int dstOff, int len) {

		if (srcOff == -1) srcOff = src.length; // autodetect

		for (int i = 0; i < len; i++) {
			dst[i + srcOff] = src[i + dstOff];
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

		for (int i = 0; i < value.length; i++) {
			value[i]++;
			if (value[i] != 0) return false;
		}
		return true; // overflowed to all 0

	}

	public ChunkParser(Key key) {
		this.masterKey = key;
	}

	public int newChunk(byte[] data, OutputStream out) throws IOException, GeneralSecurityException {

		byte[] salt = new byte[32];
		new SecureRandom().nextBytes(salt);
		out.write(salt);

		byte[] blockKeyRaw = new byte[32];
		HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
		hkdf.init(new HKDFParameters(masterKey.getEncoded(), salt, null));
		hkdf.generateBytes(blockKeyRaw, 0, blockKeyRaw.length);
		Key blockKey = new SecretKeySpec(blockKeyRaw, "AES");

		// generate new IV
		byte[] iv = new byte[16];
		out.write(iv);

		Cipher cipher = Cipher.getInstance(CIPHER_MODE);
		cipher.init(Cipher.ENCRYPT_MODE, blockKey, new GCMParameterSpec(96, iv));

		out.write(data.length + 12);

		out.write(cipher.doFinal(data));
		return EXTRA_DATA + data.length;

	}

	public int newChunk(byte[] data, RandomAccessFile out) throws IOException, GeneralSecurityException {

		byte[] salt = new byte[32];
		new SecureRandom().nextBytes(salt);
		out.write(salt);

		byte[] blockKeyRaw = new byte[32];
		HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
		hkdf.init(new HKDFParameters(masterKey.getEncoded(), salt, null));
		hkdf.generateBytes(blockKeyRaw, 0, blockKeyRaw.length);
		Key blockKey = new SecretKeySpec(blockKeyRaw, "AES");

		// generate new IV
		byte[] iv = new byte[16];
		out.write(iv);

		Cipher cipher = Cipher.getInstance(CIPHER_MODE);
		cipher.init(Cipher.ENCRYPT_MODE, blockKey, new GCMParameterSpec(96, iv));

		out.write(data.length + 12);

		out.write(cipher.doFinal(data));
		return EXTRA_DATA + data.length;

	}

	public byte[] newChunk(byte[] data) throws IOException, GeneralSecurityException {
		byte[] out = new byte[EXTRA_DATA + data.length];

		int offset = 0;
		byte[] salt = new byte[32];
		new SecureRandom().nextBytes(salt);
		copyArray(salt, out, 0, 0, salt.length);
		offset += salt.length;

		byte[] blockKeyRaw = new byte[32];
		HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
		hkdf.init(new HKDFParameters(masterKey.getEncoded(), salt, null));
		hkdf.generateBytes(blockKeyRaw, 0, blockKeyRaw.length);
		Key blockKey = new SecretKeySpec(blockKeyRaw, "AES");

		// gen IV
		byte[] iv = new byte[16];
		copyArray(iv, out, offset, 0, iv.length);
		offset += iv.length;

		Cipher cipher = Cipher.getInstance(CIPHER_MODE);
		cipher.init(Cipher.ENCRYPT_MODE, blockKey, new GCMParameterSpec(96, iv));

		writeInt(data.length + 12, out, offset);
		offset += Integer.BYTES;

		copyArray(cipher.doFinal(data), out, offset, 0, -1 /* autodetect */);

		return out;

	}

	public byte[] updateChunk(byte[] chunk, byte[] newData) throws IOException, GeneralSecurityException {

		byte[] out = new byte[EXTRA_DATA + newData.length];

		byte[] salt = new byte[32];
		copyArray(chunk, salt, 0, 0, salt.length);
		int offset = salt.length;
		
		byte[] iv = new byte[16];
		copyArray(chunk, iv, offset, 0, iv.length);
		copyArray(iv, out, 0, offset, iv.length);
		offset += iv.length;

		if (incrementByteArray(iv) /* true on overflow, new salt/IV */) {
			return newChunk(newData);
		}

		byte[] blockKeyRaw = new byte[32];
		HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
		hkdf.init(new HKDFParameters(masterKey.getEncoded(), salt, null));
		hkdf.generateBytes(blockKeyRaw, 0, blockKeyRaw.length);
		Key blockKey = new SecretKeySpec(blockKeyRaw, "AES");

		writeInt(newData.length + 12, iv, offset);
		offset += Integer.BYTES;

		Cipher cipher = Cipher.getInstance(CIPHER_MODE);
		cipher.init(Cipher.ENCRYPT_MODE, blockKey, new GCMParameterSpec(96, iv));

		copyArray(cipher.doFinal(newData), out, 0, offset, -1 /* autodetect length */);

		return out;

	}

	public int decryptChunk(InputStream input, byte[] output) throws IOException, GeneralSecurityException {

		DataInputStream in = new DataInputStream(input);

		byte[] salt = new byte[32];
		if (in.read(salt) != 32) throw new ShortBufferException(); // read salt

		byte[] iv = new byte[16];
		if (in.read(iv) != 16) throw new ShortBufferException(); // read IV
		
		int len = in.readInt();
		
		byte[] blockKeyRaw = new byte[32];
		HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
		hkdf.init(new HKDFParameters(masterKey.getEncoded(), salt, null));
		hkdf.generateBytes(blockKeyRaw, 0, blockKeyRaw.length);
		Key blockKey = new SecretKeySpec(blockKeyRaw, "AES");
		
		Cipher cipher = Cipher.getInstance(CIPHER_MODE);
		cipher.init(Cipher.DECRYPT_MODE, blockKey, new GCMParameterSpec(96, iv));

		cipher.doFinal(in.readNBytes(len), 0, len, output);

		return EXTRA_DATA + len;

	}

	public byte[] decryptChunk(RandomAccessFile input) throws IOException, GeneralSecurityException {

		byte[] salt = new byte[32];
		if (input.read(salt) != 32) throw new ShortBufferException(); // read salt

		byte[] iv = new byte[16];
		if (input.read(iv) != 16) throw new ShortBufferException(); // read IV
		
		int len = input.readInt();
		
		byte[] blockKeyRaw = new byte[32];
		HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
		hkdf.init(new HKDFParameters(masterKey.getEncoded(), salt, null));
		hkdf.generateBytes(blockKeyRaw, 0, blockKeyRaw.length);
		Key blockKey = new SecretKeySpec(blockKeyRaw, "AES");
		
		Cipher cipher = Cipher.getInstance(CIPHER_MODE);
		cipher.init(Cipher.DECRYPT_MODE, blockKey, new GCMParameterSpec(96, iv));

		byte[] data = new byte[len];
		input.read(data);
		byte[] output = new byte[len - 12];
		cipher.doFinal(data, 0, len, output);

		return output;

	}

	public byte[] decryptChunk(byte[] input) throws IOException, GeneralSecurityException {

		int offset = 0;
		// get salt
		byte[] salt = new byte[32];
		copyArray(input, salt, offset, 0, 32);
		offset += salt.length;

		// get IV
		byte[] iv = new byte[16];
		copyArray(input, iv, offset, 0, 16);

		// get length
		int len = readInt(input, offset);
		offset += 4;

		// decrypt data
		byte[] blockKeyRaw = new byte[32];
		HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
		hkdf.init(new HKDFParameters(masterKey.getEncoded(), salt, null));
		hkdf.generateBytes(blockKeyRaw, 0, blockKeyRaw.length);
		Key blockKey = new SecretKeySpec(blockKeyRaw, "AES");
		
		Cipher cipher = Cipher.getInstance(CIPHER_MODE);
		cipher.init(Cipher.DECRYPT_MODE, blockKey, new GCMParameterSpec(96, iv));

		return cipher.doFinal(input, offset, len);

	}

	public int chunkLength(RandomAccessFile f) throws IOException {

		f.seek(48 + f.getFilePointer());
		return f.readInt() + 52;

	}

}
