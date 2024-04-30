package com.ed522.libkeychain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.Before;
import org.junit.Test;

import com.ed522.libkeychain.stores.keystore.Keystore;
import com.ed522.libkeychain.stores.keystore.KeystoreEntry;

public class KeystoreTest {

    KeyPair firstPair;		// added at start, alias = test1
    SecretKey firstSecret;	// added at start, alias = test1

    SecretKey secondSecret;	// added at start, alias = test2
    
    KeyPair thirdPair;		// added at start, alias = test3

    KeyPair fourthPair;		// added after read, alias = test4
    SecretKey fourthSecret;	// added after read, alias = test4

    private static final String PASSWORD = "testpasswd";

    private static final Certificate generateCertificate(String cn, PublicKey pk, PrivateKey signer) throws CertificateException, OperatorCreationException, IOException {

        return CertificateFactory.getInstance("X.509").generateCertificate(
            new ByteArrayInputStream(
                new JcaX509v3CertificateBuilder(
                    X500Name.getInstance(cn), 
                    BigInteger.valueOf(204), 
                    Date.from(Instant.now()), 
                    Date.from(Instant.now().plus(30, ChronoUnit.DAYS)), 
                    X500Name.getInstance(cn), 
                    pk
                ).build(
                    new JcaContentSignerBuilder(signer.getAlgorithm()).build(signer)
                ).getEncoded()
            )
        );

    }

    @Test
    public void storeTest() throws IOException, GeneralSecurityException, OperatorCreationException {
        
        File file = File.createTempFile("teststore", ".lks");
        Keystore first = new Keystore(file, PASSWORD);
        first.add(new KeystoreEntry("test1", firstSecret));
        first.add(new KeystoreEntry("test1", firstPair.getPrivate()));
        first.add(new KeystoreEntry("test1", generateCertificate("test1", firstPair.getPublic(), firstPair.getPrivate())));
        
        first.add(new KeystoreEntry("test2", secondSecret));

        first.add(new KeystoreEntry("test3", thirdPair.getPrivate()));
        first.add(new KeystoreEntry("test3", generateCertificate("test3", thirdPair.getPublic(), thirdPair.getPrivate())));
        
        Keystore second = new Keystore(file, PASSWORD);
        assertEquals(first, second);

        second.add(new KeystoreEntry("test4", fourthSecret));
        second.add(new KeystoreEntry("test4", fourthPair.getPrivate()));
        second.add(new KeystoreEntry("test4", generateCertificate("test4", fourthPair.getPublic(), fourthPair.getPrivate())));

        Keystore third = new Keystore(file, PASSWORD);
        assertEquals(second, third);
        assertNotEquals(first, third);

    }

    @Before
    public void setup() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        
        KeyPairGenerator aGen = KeyPairGenerator.getInstance("EdDSA");
        aGen.initialize(new ECGenParameterSpec("Ed25519"));
        this.firstPair = aGen.genKeyPair();
        this.thirdPair = aGen.genKeyPair();
        this.fourthPair = aGen.genKeyPair();

        KeyGenerator sGen = KeyGenerator.getInstance("AES");
        sGen.init(256);
        this.firstSecret = sGen.generateKey();
        this.secondSecret = sGen.generateKey();
        this.fourthSecret = sGen.generateKey();

    }
    
}
