package com.ed522.libkeychain;

import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.junit.Test;

import com.ed522.libkeychain.message.ProofCalculator;

public class ProofTest {
	
	public static final int FACTOR = 8;

	@Test
	public void testProofGen_validProof() throws NoSuchAlgorithmException {

		byte[] msg = new byte[32];
		new SecureRandom().nextBytes(msg);

		byte[] proof = ProofCalculator.calculate(msg, FACTOR);
		assertTrue(ProofCalculator.verify(msg, proof, FACTOR));

	}

}
