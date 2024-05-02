package com.ed522.libkeychain.stores.aliasstore;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Base64;

public class CertificateEntry {
	
	private final String name;
	private final Certificate cert;

	public String getName() {
		return name;
	}
	public Certificate getCertificate() {
		return cert;
	}

	public static CertificateEntry parse(byte[] value) throws CertificateException {

		ByteBuffer buf = ByteBuffer.wrap(value);
		
		int nameLen = buf.getInt();
		byte[] tmp = new byte[nameLen];
		buf.get(tmp);
		String name = new String(tmp);

		int certLen = buf.getInt();
		tmp = new byte[certLen];
		buf.get(tmp);
		Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(tmp));

		return new CertificateEntry(name, cert);

	}

	public CertificateEntry(String name, Certificate cert) {
		this.name = name;
		this.cert = cert;
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("cert[name=");
        builder.append(name);
        builder.append(",fingerprint=");

        try {
            builder.append(Base64.getEncoder().encodeToString(
				MessageDigest.getInstance("SHA256").digest(
					this.getCertificate().getEncoded()
				)
			));
        } catch (CertificateEncodingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }

        builder.append("]");
        return builder.toString();

    }

	public boolean equals(Object other) {
        if (other instanceof CertificateEntry entry) {
            try {
                return 
					entry.getName().equals(this.getName()) && 
					Arrays.equals(
						entry.getCertificate().getEncoded(),
						this.getCertificate().getEncoded()
					);
            } catch (CertificateEncodingException e) {
                throw new IllegalStateException(e);
            }
        } else return false;
    }

    public int hashCode() {

		try {
			return Arrays.hashCode(this.getCertificate().getEncoded()) * this.name.hashCode();
		} catch (CertificateEncodingException e) {
			throw new IllegalStateException(e);
		}

    }

	public byte[] encode() throws CertificateEncodingException {

		byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
		byte[] certBytes = cert.getEncoded();
		ByteBuffer buf = ByteBuffer.allocate(8 + nameBytes.length + certBytes.length);

		buf.putInt(nameBytes.length);
		buf.put(nameBytes);
		buf.putInt(certBytes.length);
		buf.put(certBytes);
		
		return buf.array();

	}

}
