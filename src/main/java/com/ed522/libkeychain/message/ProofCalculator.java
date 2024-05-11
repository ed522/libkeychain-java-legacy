package com.ed522.libkeychain.message;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.ed522.libkeychain.err.IllegalStateError;
import com.ed522.libkeychain.util.Base64Coder;
import com.ed522.libkeychain.util.Constants;

public class ProofCalculator {

	private static final BigInteger[] limits;
	private static final int THREADS;
	static {
		THREADS = Runtime.getRuntime().availableProcessors(); // includes virt cores

		// calculate start and end
		byte[] buf = new byte[Constants.HASH_LENGTH_BYTES];
		Arrays.fill(buf, (byte) 0xFF);
		BigInteger globalEnd = new BigInteger(buf);

		limits = new BigInteger[THREADS + 1];
		for (int i = 0; i < limits.length; i++) {
			limits[i] = globalEnd.divide(BigInteger.valueOf(THREADS)).parallelMultiply(BigInteger.valueOf(i));
		}
	}
	
	private ProofCalculator() {}
	
	public static final byte[] calculate(byte[] message, int factor) throws NoSuchAlgorithmException {

		ProofJob[] jobs = new ProofJob[THREADS];
		byte[] proof = new byte[Constants.HASH_LENGTH_BYTES];		
		for (int i = 0; i < jobs.length; i++) {

			jobs[i] = new ProofJob((byte[] result) -> {
				System.arraycopy(result, 0, proof, 0, proof.length);
				for (ProofJob job : jobs) job.end();
				return null;
			}, message, factor, limits[i].toByteArray(), limits[i + 1].toByteArray());
			jobs[i].setName("ProofWorker-" + i);

		}

		for (ProofJob job : jobs)
			try {
				job.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

		if (!verify(message, proof, factor))
			throw new IllegalStateError("Generated proof is not valid: " + Base64Coder.byteToB64(proof));

		return proof;

	}

	public static final boolean verify(byte[] message, byte[] proof, int factor) throws NoSuchAlgorithmException {
		return new ProofJob(null, message, factor, new byte[1], new byte[1]).test(proof, MessageDigest.getInstance(Constants.HASH_NAME));
	}

}
