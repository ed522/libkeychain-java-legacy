package com.ed522.libkeychain;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import com.ed522.libkeychain.util.Constants;

public class TempTest {
    //FIXME this should not exist
    @org.junit.Test
    public void main() throws Exception {
        KeyPairGenerator aGen = KeyPairGenerator.getInstance(Constants.ASYMMETRIC_CIPHER);
        aGen.initialize(new ECGenParameterSpec(Constants.ASYMMETRIC_CURVE_NAME));
        KeyPair keys = aGen.generateKeyPair();

        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
            // X500 name of issuer
            new X500Name("cn=" + "name"),
            // Serial number of cert
            BigInteger.ZERO,
            // Not before (now)
            Date.from(Instant.now()),
            // Not after (max GeneralizedDate)
            Date.from(Instant.now().plus(Constants.CERT_EXPIRY_DAYS, ChronoUnit.DAYS)),
            // Locale (default)
            Locale.getDefault(),
            // X500 name of subject
            new X500Name("cn=" + "name"),
            // Public key info
            new SubjectPublicKeyInfo(
                // ecdsa-with-sha256
                // RFC 7427
                new AlgorithmIdentifier(new ASN1ObjectIdentifier(Constants.SIGNATURE_ALGORITHM_ASN1_OID)),
                keys.getPublic().getEncoded()
            )
        );
        // Self-sign cert
        X509CertificateHolder holder = builder.build(new JcaContentSignerBuilder(Constants.SIGNATURE_ALGORITHM).build(keys.getPrivate()));
        // Get the cert by using a CertificateFactory on the holder's getEncoded() method
        Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(holder.getEncoded()));
    }

}
