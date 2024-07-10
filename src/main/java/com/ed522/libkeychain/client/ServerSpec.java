package com.ed522.libkeychain.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.crypto.ShortBufferException;

public class ServerSpec {
	
	/*
	 * ServerSpec contains the relevant info to connect to the server (certificate, IP etc.).
	 * Structure:
	 * File
	 * 	PARAMS: 1B 			// Parameters and miscellenia (reserved for future use)
	 * 	IP: 4B				// IP addresses are 4 bytes
	 * 	CERTLEN: 4B
	 * 	CERT: <CERTLEN>B 	// Certificate built in, CERTLEN bytes long
	 */

	// unused but reserved
	private final byte params;
	private final String ip;
	private final Certificate cert;

	public static final ServerSpec parse(File file) throws ShortBufferException, CertificateException, IOException {
		try (FileInputStream f = new FileInputStream(file)) {
			return parse(f.readAllBytes());
		}
	}

	public static final ServerSpec parse(byte[] val) throws ShortBufferException, CertificateException {

		if (val.length < 21) throw new ShortBufferException("Not enough data: less than enough for headers (needs 21 at least)");
		ByteBuffer buf = ByteBuffer.wrap(val);
		byte params = buf.get();

		byte[] ipRaw = new byte[4];
		buf.get(ipRaw);
		StringBuilder str = new StringBuilder();

		str.append(Byte.toString(ipRaw[0]));
		str.append(".");
		str.append(Byte.toString(ipRaw[1]));
		str.append(".");
		str.append(Byte.toString(ipRaw[2]));
		str.append(".");
		str.append(Byte.toString(ipRaw[3]));

		String ip = str.toString();

		int certLen = buf.getInt();

		if (val.length < 21 + certLen) throw new ShortBufferException("Not enough data: less than enough for the certificate");

		byte[] certBytes = new byte[certLen];
		buf.get(certBytes);
		Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certBytes));
		
		return new ServerSpec(params, ip, cert);

	}

	public ServerSpec(byte params, String ip, Certificate cert) {
		this.params = params;
		this.ip = ip;
		this.cert = cert;
	}

	public byte getParams() {
		return params;
	}
	public String getIp() {
		return ip;
	}
	public Certificate getCert() {
		return cert;
	}

}
