package com.ed522.libkeychain.stores.keystore;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

public class KeystoreEntry {

    static final byte PRIVATE = 0;
    static final byte PUBLIC = 1;
    static final byte SECRET = 2;

    private final EntryType type;
    private final PrivateKey privateValue;
    private final Certificate certValue;
    private final SecretKey secretValue;
    private final String name;

    private static final int ensure(byte[] val, int len) throws ShortBufferException {
        if (val.length < len) throw new ShortBufferException(
            String.format("Not enough bytes: need %i, got %i", len, val.length)
        );
        else return len;
    }

    public KeystoreEntry(String name, PrivateKey value) {
        this.name = name;
        this.type = EntryType.PRIVATE;
        this.privateValue = value;
        this.certValue = null;
        this.secretValue = null;
    }
    public KeystoreEntry(String name, Certificate value) {
        this.name = name;
        this.type = EntryType.PUBLIC;
        this.privateValue = null;
        this.certValue = value;
        this.secretValue = null;
    }
    public KeystoreEntry(String name, SecretKey value) {
        this.name = name;
        this.type = EntryType.SECRET;
        this.privateValue = null;
        this.certValue = null;
        this.secretValue = value;
    }

    public static KeystoreEntry parse(final byte[] value) throws ShortBufferException, InvalidKeySpecException, NoSuchAlgorithmException, CertificateException {

        // note: ensure() throws an exception if the buffer is shorter
        // than the requirement (arg 2), and it returns the requirement

        int lenNeeded = 0;
        // get name
        ByteBuffer buf = ByteBuffer.wrap(value);
        lenNeeded = ensure(value, 4 + lenNeeded);
        int namelen = buf.getInt();

        lenNeeded = ensure(value, namelen + lenNeeded);
        byte[] val = new byte[namelen];
        buf.get(val);
        String name = new String(val);

        // get ID
        lenNeeded = ensure(val, 1 + lenNeeded);
        byte id = buf.get();
        EntryType type = EntryType.forID(id);

        // get value
        lenNeeded = ensure(val, 4 + lenNeeded);
        int vallen = buf.getInt();
        
        ensure(val, vallen + lenNeeded);
        val = new byte[vallen];
        buf.get(val);

        if (type.equals(EntryType.PUBLIC)) {
            // no algorithm name needed
            Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(val));
            return new KeystoreEntry(name, cert);
        } else if (type.equals(EntryType.PRIVATE)) {

            // copy contents
            ByteBuffer b = ByteBuffer.allocate(val.length);
            b.put(val);

            ensure(val, 2);
            short algoNameLen = b.getShort();
            byte[] algoNameRaw = new byte[algoNameLen];
            b.get(algoNameRaw);

            String algoName = new String(algoNameRaw, StandardCharsets.UTF_8);

            val = new byte[vallen - algoNameLen];
            b.get(val);            

            X509EncodedKeySpec spec = new X509EncodedKeySpec(val);

            KeyFactory factory = KeyFactory.getInstance(algoName);

            return new KeystoreEntry(name, factory.generatePrivate(spec));
        } else {

            // secret key

            // copy contents
            ByteBuffer b = ByteBuffer.allocate(val.length);
            b.put(val);

            ensure(val, 2);
            short algoNameLen = b.getShort();
            byte[] algoNameRaw = new byte[algoNameLen];
            b.get(algoNameRaw);

            String algoName = new String(algoNameRaw, StandardCharsets.UTF_8);

            val = new byte[vallen - algoNameLen];
            b.get(val);

            return new KeystoreEntry(name, new SecretKeySpec(val, algoName));

        }

    }

    public Certificate getCertificate() {
        if (!this.type.equals(EntryType.PUBLIC))
            throw new IllegalStateException("Wrong type: this entry is not a certificate");
        else return certValue;
    }
    public PrivateKey getPrivate() {
        if (!this.type.equals(EntryType.PRIVATE))
            throw new IllegalStateException("Wrong type: this entry is not a private key");
        else return privateValue;
    }
    public SecretKey getSecret() {
        if (!this.type.equals(EntryType.SECRET))
            throw new IllegalStateException("Wrong type: this entry is not a secret key");
        else return secretValue;
    }
    public String getName() {
        return name;
    }
    public EntryType getType() {
        return type;
    }
    public byte getTypeByte() {
        return type.value();
    }

    public byte[] encode() throws CertificateEncodingException {
        
        ByteBuffer buf = ByteBuffer.allocate(this.calcSize());
        
        buf.putInt(name.getBytes(StandardCharsets.UTF_8).length);
        buf.put(name.getBytes(StandardCharsets.UTF_8));
        buf.put(type.value());
        
        if (this.getType().equals(EntryType.PRIVATE)) {

            // algorithm
            buf.putInt(
                6 + this.getPrivate().getEncoded().length + 
                this.getPrivate()
                    .getAlgorithm()
                    .getBytes(StandardCharsets.UTF_8)
                    .length
            );
            buf.putShort(exactCast(
                this.getPrivate()
                    .getAlgorithm()
                    .getBytes(StandardCharsets.UTF_8)
                    .length
            ));
            buf.put(this.getPrivate().getAlgorithm().getBytes(StandardCharsets.UTF_8));
            buf.put(this.getPrivate().getEncoded());

        } else if (this.getType().equals(EntryType.SECRET)) {

            // algorithm
            buf.putInt(
                6 + this.getSecret().getEncoded().length + 
                this.getSecret()
                    .getAlgorithm()
                    .getBytes(StandardCharsets.UTF_8)
                    .length
            );
            buf.putShort(exactCast(
                this.getSecret()
                    .getAlgorithm()
                    .getBytes(StandardCharsets.UTF_8)
                    .length)
            );
            buf.put(this.getSecret().getAlgorithm().getBytes(StandardCharsets.UTF_8));
            buf.put(this.getSecret().getEncoded());
        } else if (this.getType().equals(EntryType.PUBLIC)) {

            buf.putInt(this.getCertificate().getEncoded().length);
            buf.put(this.getCertificate().getEncoded());

        }

        return buf.array();

    }

    public short exactCast(int val) {
        if (val < Short.MIN_VALUE || val > Short.MAX_VALUE) throw new ArithmeticException("Out of range");
        else return (short) val;
    }

    public int calcSize() throws CertificateEncodingException {

        // strange formatting but for a reason
        // all of the lines are their own value
        // all constant values are compressed

        if (type.equals(EntryType.PRIVATE)) {
            // private key
            // 11 = 4 byte name tag (4) + 1 byte type (5) + 4 byte value length (9) + 2 byte algorithm length tag (11)
            return 
                11 + 
                this.getName().getBytes(StandardCharsets.UTF_8).length + 
                this.getPrivate().getAlgorithm().length() + 
                this.getPrivate().getEncoded().length;
        } else if (type.equals(EntryType.SECRET)) {
            // secret key
            // 11 = 4 byte name tag (4) + 1 byte type (5) + 4 byte value length (9) + 2 byte algorithm length tag (11)
            return 
                11 + 
                this.getName().getBytes(StandardCharsets.UTF_8).length + 
                this.getSecret().getAlgorithm().length() + 
                this.getSecret().getEncoded().length;
        } else {
            // certificate
            // 9 = 4 byte name tag (4) + 1 byte type (5) + 4 byte value length (9) (no algorithm name needed)
            return 
                9 + 
                this.getName().getBytes(StandardCharsets.UTF_8).length + 
                this.getCertificate().getEncoded().length;
        }

    }
    
}
