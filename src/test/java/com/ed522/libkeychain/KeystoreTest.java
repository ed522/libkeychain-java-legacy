package com.ed522.libkeychain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ed522.libkeychain.stores.keystore.Keystore;
import com.ed522.libkeychain.stores.keystore.KeystoreEntry;
import com.ed522.libkeychain.util.Logger;
import com.ed522.libkeychain.util.Constants;
import com.ed522.libkeychain.util.Logger.Level;
import com.ed522.libkeychain.util.Logger.LoggerOutputStream;

public class KeystoreTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    KeyPair firstPair;		// added at start, alias = test1
    SecretKey firstSecret;	// added at start, alias = test1

    SecretKey secondSecret;	// added at start, alias = test2
    
    KeyPair thirdPair;		// added at start, alias = test3

    KeyPair fourthPair;		// added after read, alias = test4
    SecretKey fourthSecret;	// added after read, alias = test4

    private static final String PASSWORD = "testpasswd";
    private File file;

    private static final Certificate generateCertificate(String cn, PublicKey pk, PrivateKey signer) throws CertificateException, OperatorCreationException, IOException {

        return CertificateFactory.getInstance("X.509").generateCertificate(
            new ByteArrayInputStream(
                new JcaX509v3CertificateBuilder(
                    new X500Name("CN=" + cn), 
                    BigInteger.valueOf(204), 
                    Date.from(Instant.now()), 
                    Date.from(Instant.now().plus(30, ChronoUnit.DAYS)), 
                    new X500Name("CN=" + cn), 
                    pk
                ).build(
                    new JcaContentSignerBuilder("Ed448").build(signer)
                ).getEncoded()
            )
        );

    }

    @Test
    public void storeTest() throws IOException, GeneralSecurityException, OperatorCreationException {

        PrintStream logger = new PrintStream(new LoggerOutputStream(new Logger(), "KeystoreTest", Level.INFO));
        
        Keystore first = new Keystore(file, PASSWORD);
        first.add(new KeystoreEntry("test1", firstSecret));
        first.add(new KeystoreEntry("test1", firstPair.getPrivate()));
        first.add(new KeystoreEntry("test1", generateCertificate("test1", firstPair.getPublic(), firstPair.getPrivate())));
        
        first.add(new KeystoreEntry("test2", secondSecret));

        first.add(new KeystoreEntry("test3", thirdPair.getPrivate()));
        first.add(new KeystoreEntry("test3", generateCertificate("test3", thirdPair.getPublic(), thirdPair.getPrivate())));
        
        List<KeystoreEntry> firstEntries = first.getEntries();
        first.close();

        Keystore second = new Keystore(file, PASSWORD);
        List<KeystoreEntry> secondEntries = second.getEntries();

        logger.println("First: ");
        firstEntries.forEach(logger::println);
        logger.println("Second: ");
        secondEntries.forEach(logger::println);
        assertEquals(firstEntries, secondEntries);

        second.add(new KeystoreEntry("test4", fourthSecret));
        second.add(new KeystoreEntry("test4", fourthPair.getPrivate()));
        second.add(new KeystoreEntry("test4", generateCertificate("test4", fourthPair.getPublic(), fourthPair.getPrivate())));

        secondEntries = second.getEntries();
        second.close();

        Keystore third = new Keystore(file, PASSWORD);
        assertEquals(secondEntries, third.getEntries());
        assertNotEquals(firstEntries, third.getEntries());

        third.close();
        logger.close();

    }

    @Before
    public void setup() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException {
        
        file = new File("testkeystore.lks");
        file.createNewFile();
        
        KeyPairGenerator aGen = KeyPairGenerator.getInstance(Constants.ASYMMETRIC_CIPHER);
        aGen.initialize(new ECGenParameterSpec(Constants.ASYMMETRIC_CURVE_NAME));
        this.firstPair = aGen.genKeyPair();
        this.thirdPair = aGen.genKeyPair();
        this.fourthPair = aGen.genKeyPair();

        KeyGenerator sGen = KeyGenerator.getInstance(Constants.SYMMETRIC_CIPHER);
        sGen.init(256);
        this.firstSecret = sGen.generateKey();
        this.secondSecret = sGen.generateKey();
        this.fourthSecret = sGen.generateKey();

    }

    @After
    public void teardown() {
        // file.delete();
    }
    
}
