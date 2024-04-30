package com.ed522.libkeychain.keystore;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.util.Arrays;

public class Keystore {

    private static final String PBKDF_MODE = "PBKDF2WithSHA256";

    protected static final byte[] VERIFICATION_BYTES = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};
    
    /*
     * Lengths may be added based on requirements.
     * 
     * Each file shall be split into *segments*, that each have their own IV.
     * Each segment is of a variable length, with an IV at the start, a 32-bit length and an extra 12 bytes for the AEAD tag.
     * 
     * Each segment is headed with 128 bits of random IV.
     * Segments are reencrypted upon modification and the IV is regenerated.
     * 
     * All encryption uses GCM and IVs are incremented from 0.
     * Upon overflow the file is regenerated with a new salt, IV and key.
     * 
     * The crypto used is CBC and it is re-encrypted on every startup.
     * The HMAC used is SHA256 and is recalculated with the crypto.
     * 
     * Structure:
     * 
     * Header
     *  MAGIC: "LKAS"               PLAIN       // verify that it's the right file (US ASCII)
     *  SALT: 32B                   PLAIN       // salt the password
     *  ----- everything past this is encrypted and segmented -----
     *  [[ start seg0 ]]
     *  VERIFY: 16B                 CRYPT       // test if key is correct, one block (see constant for value)
     *  ENTRIES: 8B                 CRYPT       // entry count
     *  [[ end seg0 ]]
     *  CLICERTS: cert[]            CRYPT       // vast majority of the file is this
     */

    private final RandomAccessFile raf;
    private final SecretKey masterKey;
    private final ObservableArrayList<CertificateEntry> clientCerts;

    /**
     * Build a completely new file.
     * @throws IOException 
     * @throws GeneralSecurityException
     * @throws InvalidCipherTextException 
     */
    private static void buildFile(RandomAccessFile file, String password, byte[] saltToSet) throws IOException, GeneralSecurityException {

        file.write("LKAS".getBytes(StandardCharsets.US_ASCII));

        new SecureRandom().nextBytes(saltToSet);
        file.write(saltToSet);
        
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF_MODE);
        SecretKey masterKey = factory.generateSecret(new PBEKeySpec(password.toCharArray(), saltToSet, 650_000));
        
        // chunked
        ChunkParser parser = new ChunkParser(masterKey);
        // verification, 0 entries
        byte[] chunk0Bytes = Arrays.concatenate(VERIFICATION_BYTES, new byte[8]);

        file.write(parser.newChunk(chunk0Bytes));

    }
    private static void buildFile(OutputStream out, String password, byte[] saltToSet) throws IOException, GeneralSecurityException {

        out.write("LKAS".getBytes(StandardCharsets.US_ASCII));
        
        new SecureRandom().nextBytes(saltToSet);
        out.write(saltToSet);
        
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF_MODE);
        SecretKey masterKey = factory.generateSecret(new PBEKeySpec(password.toCharArray(), saltToSet, 650_000));
        
        // chunked
        ChunkParser parser = new ChunkParser(masterKey);
        // verification, 0 entries
        byte[] chunk0Bytes = Arrays.concatenate(VERIFICATION_BYTES, new byte[8]);

        out.write(parser.newChunk(chunk0Bytes));

    }
    private static List<CertificateEntry> readFile(RandomAccessFile file, String password, byte[] saltToSet) throws GeneralSecurityException, IOException {

        if (saltToSet.length != 32) throw new IllegalArgumentException("Bad salt length (needs to be 32)");
        
        List<CertificateEntry> certs = new ArrayList<>();

        byte[] magicIn = new byte[4];
        file.read(magicIn);
        // magic
        if (!new String(magicIn, StandardCharsets.US_ASCII).equals("LKAS")) throw new StreamCorruptedException("Bad magic number (wrong file?)");

        file.read(saltToSet);

        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF_MODE);
        SecretKey masterKey = factory.generateSecret(new PBEKeySpec(password.toCharArray(), saltToSet, 650_000));

        // read first chunk
        ChunkParser parser = new ChunkParser(masterKey);
        ByteBuffer data = ByteBuffer.wrap(parser.decryptChunk(file));
        byte[] verification = new byte[VERIFICATION_BYTES.length];

        data.get(verification);
        if (!Arrays.areEqual(verification, VERIFICATION_BYTES)) throw new StreamCorruptedException("Bad encryption verification (wrong password?)");

        long entries = data.getLong();

        // read certificate entries

        for (int i = 0; i < entries; i++) {
            certs.add(CertificateEntry.parse(parser.decryptChunk(file)));
        }

        return certs;

    }

    /**
     * Creates a {@code Keystore} for a file.
     * 
     * If you want to store to a file, USE THIS. This updates the store incrementally. However, the entire keystore will be rewritten each time it is initialized.
     * 
     * @param file The file to store. Must not be null.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public Keystore(File file, String password) throws IOException, GeneralSecurityException {
        
        if (!file.exists() && !file.createNewFile()) throw new IllegalStateException("Failed to create file");

        this.raf = new RandomAccessFile(file, "rws");
        clientCerts = new ObservableArrayList<>(CertificateEntry.class);
        byte[] masterSalt = new byte[32];

        // build new
        if (file.length() == 0) {
            buildFile(this.raf, password, masterSalt);
        } else {
            // read
            clientCerts.addAll(readFile(this.raf, password, masterSalt));
        }
        
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF_MODE);
        masterKey = factory.generateSecret(new PBEKeySpec(password.toCharArray(), masterSalt, 650_000));

    }

    /**
     * Creates a {@code Keystore} for the specified output stream.
     * 
     * DO NOT USE FOR FILES. This will rewrite the keystore every single time. Use the File constructor.
     * 
     * @param stream
     * @throws GeneralSecurityException 
     * @throws IOException 
     * @see Keystore#Keystore(File, String)
     */
    public Keystore(OutputStream stream, CertificateEntry[] clientCerts, String password) throws IOException, GeneralSecurityException {

        this.raf = null;
        this.clientCerts = new ObservableArrayList<>(CertificateEntry.class);
        this.clientCerts.addOnAdd((CertificateEntry entry) -> {
            try {
                newEntry(entry);
            } catch (IOException | GeneralSecurityException e) {
                throw new IllegalStateException(e);
            }
        });
        this.clientCerts.addOnRemove((CertificateEntry entry) -> {
            try {
                removeEntry(entry.getAlias());
            } catch (GeneralSecurityException | IOException e) {
                throw new IllegalStateException(e);
            }
        });

        byte[] salt = new byte[32];

        buildFile(stream, password, salt);

        for (CertificateEntry e : clientCerts) {
            stream.write(e.encode());
        }
        
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF_MODE);
        masterKey = factory.generateSecret(new PBEKeySpec(password.toCharArray(), salt, 650_000));

    }

    private void removeEntry(String name, EntryType type) throws GeneralSecurityException, IOException {

        // 1. find the entry
        // 2. shift chunks of the file back
        // 3. truncate file

        long initial = raf.getFilePointer();

        /* find entry */
        raf.seek(122); // skip master salt and seg0

        long entryOffset = -1;
        long size = -1;
        ChunkParser parser = new ChunkParser(masterKey);
        
        while (true) {

            entryOffset = raf.getFilePointer();
            byte[] chunk = parser.decryptChunk(raf);
            CertificateEntry entry = CertificateEntry.parse(chunk);
            size = chunk.length + 52l /* extra data */;

            if (entry.getAlias().equals(name)) break;

        }

        if (entryOffset == -1 || size == -1) throw new NoSuchElementException("No certificate with name in file");

        /* shift chunks */
        while (raf.getFilePointer() < raf.length()) {

            long initialOffset = raf.getFilePointer();
            int cLen = parser.chunkLength(raf);
            byte[] chunk = new byte[cLen + 52];
            raf.seek(initialOffset - size);
            raf.write(chunk);

        }

        /* shrink */
        raf.setLength(raf.length() - size);

        // return
        raf.seek(initial);

    }

    private void newEntry(CertificateEntry entry) throws IOException, GeneralSecurityException {

        raf.seek(raf.length());
        new ChunkParser(masterKey).newChunk(entry.encode(), raf);

    }

}
