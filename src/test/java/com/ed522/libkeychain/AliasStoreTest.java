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

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ed522.libkeychain.stores.aliasstore.AliasStore;
import com.ed522.libkeychain.stores.aliasstore.CertificateEntry;
import com.ed522.libkeychain.util.Logger;
import com.ed522.libkeychain.util.Logger.Level;
import com.ed522.libkeychain.util.Logger.LoggerOutputStream;

public class AliasStoreTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    Certificate firstCert;    	// added at start, alias = test1
    Certificate secondCert;   	// added at start, alias = test2
    
    Certificate thirdCert;		// added after read, alias = test3

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

        PrintStream logger = new PrintStream(new LoggerOutputStream(new Logger(), "AliasStoreTest", Level.INFO));
        
        AliasStore first = new AliasStore(file, PASSWORD);
        
        first.add(new CertificateEntry("test1", firstCert));
        first.add(new CertificateEntry("test2", secondCert));
        
        List<CertificateEntry> firstEntries = first.getEntries();
        first.close();

        AliasStore second = new AliasStore(file, PASSWORD);
        List<CertificateEntry> secondEntries = second.getEntries();
        
        logger.println("First: ");
        firstEntries.forEach(logger::println);
        logger.println("Second: ");
        secondEntries.forEach(logger::println);
        assertEquals(firstEntries, secondEntries);
        
        second.add(new CertificateEntry("test3", thirdCert));
        secondEntries = second.getEntries();
        second.close();
        
        AliasStore third = new AliasStore(file, PASSWORD);
        assertEquals(secondEntries, third.getEntries());
        assertNotEquals(firstEntries, third.getEntries());

        third.close();
        logger.close();

    }

    @Before
    public void setup() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, CertificateException, OperatorCreationException {
        
        file = new File("testaliasstore.lks");
        file.createNewFile();
        
        KeyPairGenerator aGen = KeyPairGenerator.getInstance("EdDSA");
        aGen.initialize(new ECGenParameterSpec("Ed448"));
        KeyPair first = aGen.genKeyPair();
        KeyPair second = aGen.genKeyPair();
        KeyPair third = aGen.genKeyPair();

        this.firstCert = generateCertificate("test1", first.getPublic(), first.getPrivate());
        this.secondCert = generateCertificate("test2", second.getPublic(), second.getPrivate());
        this.thirdCert = generateCertificate("test3", third.getPublic(), third.getPrivate());

    }

    @After
    public void teardown() {
        file.delete();
    }
    
}
