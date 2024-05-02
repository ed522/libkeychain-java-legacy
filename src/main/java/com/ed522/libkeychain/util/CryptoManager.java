/**
 * Copyright 2023 ed522
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ed522.libkeychain.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.Arrays;

import com.ed522.libkeychain.logger.Logger;
import com.ed522.libkeychain.logger.Logger.Level;

public class CryptoManager {
    
    private static final String ERR_SECRET_NULL = "The secret key is null";
    private static final String LOGGER_NAME = "CryptoManager";
    private KeyPair keys;
    private Certificate cert;
    private ECPublicKeySpec pkSpec;
    private ECPrivateKeySpec skSpec;
    private SecretKey secret;
    private SecretKeySpec secretSpec;

    /**
     * Creates a new crypto manager without a certificate.
     * @param keys An keypair to use (may be null)
     * @param secret A secret key to use of any kind (may be null)
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public CryptoManager(KeyPair keys, SecretKey secret) throws InvalidKeySpecException, NoSuchAlgorithmException {

        this.keys = keys;
        this.secret = secret;
        this.cert = null;

        if (keys != null) {
            this.pkSpec = KeyFactory.getInstance(keys.getPublic().getAlgorithm()).getKeySpec(keys.getPublic(), ECPublicKeySpec.class);
            this.skSpec = KeyFactory.getInstance(keys.getPrivate().getAlgorithm()).getKeySpec(keys.getPrivate(), ECPrivateKeySpec.class);
        } else {
            this.pkSpec = null;
            this.skSpec = null;
        }
        if (secret != null)
            this.secretSpec = new SecretKeySpec(secret.getEncoded(), secret.getAlgorithm());
        else this.secretSpec = null;

    }

    /**
     * Creates a new crypto manager with a certificate.
     * @param priv A private key to use
     * @param cert A certificate to use, the public key is derived from it
     * @param secret A secret key to use of any kind
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException 
     */
    public CryptoManager(PrivateKey priv, Certificate cert, SecretKey secret) throws GeneralSecurityException {

        if (priv != null && !priv.getAlgorithm().equals(StandardAlgorithms.ASYMMETRIC_CIPHER)) throw new InvalidKeyException("Must be EC Ed448 key");
        if (cert != null && !cert.getPublicKey().getAlgorithm().equals(StandardAlgorithms.ASYMMETRIC_CIPHER)) throw new InvalidKeyException("Must be EC Ed448 key");

        if (cert != null) {
        
            this.keys = new KeyPair(cert.getPublicKey(), priv);
            this.cert = cert;
        
        } else {
            this.keys = new KeyPair(null, priv);
            this.cert = null;
        }

        this.secret = secret;

        if (cert != null)
            this.pkSpec = KeyFactory.getInstance(this.keys.getPublic().getAlgorithm()).getKeySpec(this.keys.getPublic(), ECPublicKeySpec.class);
        else 
            this.pkSpec = null;

        if (priv != null)
            this.skSpec = KeyFactory.getInstance(this.keys.getPrivate().getAlgorithm()).getKeySpec(this.keys.getPrivate(), ECPrivateKeySpec.class);
        else
            this.skSpec = null;

        if (secret != null)
            this.secretSpec = new SecretKeySpec(secret.getEncoded(), secret.getAlgorithm());
        else
            this.secretSpec = null;

    }

    public CryptoManager(String name) throws NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, IOException, OperatorCreationException, InvalidAlgorithmParameterException {

        KeyPairGenerator aGen = KeyPairGenerator.getInstance(StandardAlgorithms.ASYMMETRIC_CIPHER);
        aGen.initialize(new ECGenParameterSpec("secp521r1"));
        this.keys = aGen.generateKeyPair();

        KeyGenerator symGen = KeyGenerator.getInstance("AES");
        symGen.init(256);
        this.secret = symGen.generateKey();

        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
            // X500 name of issuer
            new X500Name("cn=" + name),
            // Serial number of cert
            BigInteger.ZERO,
            // Not before (now)
            Date.from(Instant.now()),
            // Not after (n days)
            Date.from(Instant.now().plus(365, ChronoUnit.DAYS)),
            // Locale (default)
            Locale.getDefault(),
            // X500 name of subject
            new X500Name("cn=" + name),
            // Public key info
            new SubjectPublicKeyInfo(
                // ecdsa-with-sha256
                // RFC 7427
                new AlgorithmIdentifier(new ASN1ObjectIdentifier("1.2.840.10045.4.3.2")),
                keys.getPublic().getEncoded()
            )
        );
        // Self-sign cert
        X509CertificateHolder holder = builder.build(new JcaContentSignerBuilder(StandardAlgorithms.SIGNATURE_ALGORITHM).build(keys.getPrivate()));
        // Get the cert by using a CertificateFactory on the holder's getEncoded() method
        // Needs to use a ByteArrayInputStream because generateCertificate() needs an
        // InputStream for some bizzare reason
        cert = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(holder.getEncoded()));

        this.pkSpec = KeyFactory.getInstance(keys.getPublic().getAlgorithm()).getKeySpec(keys.getPublic(), ECPublicKeySpec.class);
        this.skSpec = KeyFactory.getInstance(keys.getPrivate().getAlgorithm()).getKeySpec(keys.getPrivate(), ECPrivateKeySpec.class);
        this.secretSpec = new SecretKeySpec(secret.getEncoded(), secret.getAlgorithm());

    }

    public KeyPair getKeys() {

        return keys;

    }

    public SecretKey getSecret() {

        return secret;

    }

    public ECPublicKeySpec getPublicKeyspec() {
        
        return pkSpec;

    }

    public ECPrivateKeySpec getPrivateKeyspec() {
        
        return skSpec;

    }

    public SecretKeySpec getSecretKeyspec() {

        return secretSpec;

    }

    public Certificate getCert() {

        return this.cert;
    
    }

    public void setCertAndKeys(Certificate cert, PrivateKey priv) throws InvalidKeySpecException, NoSuchAlgorithmException {
        
        this.cert = cert;
        this.keys = new KeyPair(cert.getPublicKey(), priv);

        this.pkSpec = KeyFactory.getInstance(StandardAlgorithms.ASYMMETRIC_CIPHER).getKeySpec(cert.getPublicKey(), ECPublicKeySpec.class);
        this.skSpec = KeyFactory.getInstance(StandardAlgorithms.ASYMMETRIC_CIPHER).getKeySpec(priv, ECPrivateKeySpec.class);

    }

    public byte[] sign(byte[] message) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {

        Signature sign = Signature.getInstance(StandardAlgorithms.SIGNATURE_ALGORITHM);
        sign.initSign(keys.getPrivate());
        sign.update(message);
        return sign.sign();
        
    }

    public boolean verify(byte[] message, byte[] signature) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {

        return this.verifyUsingKey(message, signature, keys.getPublic());

    }

    public boolean verifyUsingKey(byte[] message, byte[] signature, PublicKey key) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {

        Signature verify = Signature.getInstance(StandardAlgorithms.SIGNATURE_ALGORITHM);
        verify.initVerify(key);
        verify.update(message);
        return verify.verify(signature);

    }

    public byte[] encryptSym(byte[] message, byte[] iv) throws GeneralSecurityException, InvalidCipherTextException {

        Objects.requireNonNull(this.secret, ERR_SECRET_NULL);
        
        Cipher cipher = Cipher.getInstance(StandardAlgorithms.SYMMETRIC_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));
        return cipher.doFinal(message);

    }

    public byte[] encryptAsym(byte[] message, byte[] iv) throws GeneralSecurityException {

        KeyGenerator generator = KeyGenerator.getInstance(StandardAlgorithms.SYMMETRIC_CIPHER);
        generator.init(256);

        SecretKey messageSecret = generator.generateKey();

        Cipher wrapper = Cipher.getInstance(StandardAlgorithms.ASYMMETRIC_WRAP);
        wrapper.init(Cipher.WRAP_MODE, this.getCert().getPublicKey());

        Cipher cipher = Cipher.getInstance(StandardAlgorithms.SYMMETRIC_TRANSFORMATION); // NOSONAR: Insecure padding (it's a stream cipher ffs)
        cipher.init(Cipher.ENCRYPT_MODE, messageSecret, new IvParameterSpec(iv));
        return Arrays.concatenate(wrapper.wrap(messageSecret), cipher.doFinal(message));

    }

    public byte[] decryptSym(byte[] message, byte[] iv) throws GeneralSecurityException, IllegalStateException, InvalidCipherTextException {
        
        Logger.getDefault().logFormatted(Level.TRACE, "Decrypt, IV = ${0}, message = ${1}", LOGGER_NAME, Base64Coder.byteToB64(iv), Base64Coder.byteToB64(message));
        Objects.requireNonNull(this.secret, ERR_SECRET_NULL);
        
        Cipher cipher = Cipher.getInstance(StandardAlgorithms.SYMMETRIC_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        return cipher.doFinal(message);

    }

    public byte[] decryptAsym(byte[] message, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        Key messageSecret = this.unwrapAsym(Arrays.copyOfRange(message, 0, 32), Cipher.SECRET_KEY, StandardAlgorithms.SYMMETRIC_CIPHER);

        Cipher cipher = Cipher.getInstance(StandardAlgorithms.SYMMETRIC_TRANSFORMATION); // NOSONAR: Insecure padding (it's a stream cipher ffs)
        cipher.init(Cipher.DECRYPT_MODE, messageSecret, new IvParameterSpec(iv));
        return cipher.doFinal(Arrays.copyOfRange(message, StandardAlgorithms.CHACHA20_KEY_LENGTH, message.length));

    }

    public byte[] wrapAsym(Key toWrap) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {

        Cipher wrap = Cipher.getInstance(keys.getPublic().getAlgorithm());
        wrap.init(Cipher.WRAP_MODE, keys.getPublic());
        return wrap.wrap(toWrap);

    }

    public byte[] wrapSym(Key toWrap) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {

        Cipher wrap = Cipher.getInstance(secret.getAlgorithm());
        wrap.init(Cipher.WRAP_MODE, secret);
        return wrap.wrap(toWrap);

    }

    public Key unwrapAsym(byte[] wrappedKey, int type, String algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {

        Cipher unwrapper = Cipher.getInstance(StandardAlgorithms.ASYMMETRIC_WRAP);
        unwrapper.init(Cipher.UNWRAP_MODE, this.getKeys().getPrivate());
        return unwrapper.unwrap(wrappedKey, algorithm, type);

    }

    public Key unwrapSym(byte[] wrapped, int type, String algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {

        Cipher unwrap = Cipher.getInstance(secret.getAlgorithm());
        unwrap.init(Cipher.UNWRAP_MODE, secret);
        return unwrap.unwrap(wrapped, algorithm, type);

    }

    public void setSecret(SecretKey encryptionKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
        secret = encryptionKey;
        secretSpec = (SecretKeySpec) SecretKeyFactory.getInstance(encryptionKey.getAlgorithm()).getKeySpec(encryptionKey, SecretKeySpec.class);
    }

}
