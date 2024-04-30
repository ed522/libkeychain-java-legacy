package com.ed522.libkeychain.stores.aliasstore;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class CertificateEntry {
	
	private final String alias;
	private final Certificate cert;

	public String getAlias() {
		return alias;
	}
	public Certificate getCert() {
		return cert;
	}

	public static CertificateEntry parse(byte[] value) throws CertificateException {

		ByteBuffer buf = ByteBuffer.wrap(value);
		
		int aliasLen = buf.getInt();
		byte[] tmp = new byte[aliasLen];
		String alias = new String(tmp);

		int certLen = buf.getInt();
		tmp = new byte[certLen];
		Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(tmp));

		return new CertificateEntry(alias, cert);

	}

	public CertificateEntry(String alias, Certificate cert) {
		this.alias = alias;
		this.cert = cert;
	}

	public byte[] encode() throws CertificateEncodingException {

		byte[] aliasBytes = alias.getBytes(StandardCharsets.UTF_8);
		byte[] certBytes = cert.getEncoded();
		ByteBuffer buf = ByteBuffer.allocate(8 + aliasBytes.length + certBytes.length);

		buf.putInt(aliasBytes.length);
		buf.put(aliasBytes);
		buf.putInt(certBytes.length);
		buf.put(certBytes);
		
		return buf.array();

	}

}
