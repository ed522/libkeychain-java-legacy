package com.ed522.libkeychain;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

public class ResumeTest {
	
	@Test
	public void cbcResumeTest() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		
		byte[] rng = new byte[32];
		byte[] iv = new byte[16];
		new SecureRandom().nextBytes(rng);
		new SecureRandom().nextBytes(iv);

		Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
		c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(rng, "AES"));

	}

}
