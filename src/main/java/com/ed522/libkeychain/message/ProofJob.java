package com.ed522.libkeychain.message;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

public class ProofJob extends Thread {

	private final int factor;
	private final int mod8;
	private final BigInteger start;
	private final BigInteger end;
	private final byte[] data;
	private final Function<byte[], ?> onComplete;

	public ProofJob(Function<byte[], ?> onComplete, byte[] data, int factor, byte[] start, byte[] end) {
		this.onComplete = onComplete;
		this.data = data;
		this.factor = factor;
		this.mod8 = factor % 8;
		this.start = new BigInteger(start);
		this.end = new BigInteger(end);
	}

	@Override
	public void run() {
		try {
			
			MessageDigest digest = MessageDigest.getInstance("SHA256");

			for (BigInteger i = start; i.compareTo(end) < 1; i = i.add(BigInteger.ONE)) 
				if (test(data, i.toByteArray(), digest)) {
					onComplete.apply(i.toByteArray());
					return;
				}

		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}

	}

	public boolean test(byte[] value, byte[] proof, MessageDigest digest) {

		digest.update(value);
		digest.update(proof);
		byte[] result = digest.digest();

		int bytes = (factor - mod8) / 8;

		for (int i = 0; i < bytes; i++) {

			if (result[i] != 0) return false;

		}

		byte last = (byte) (-1 >> mod8);
		return (result[bytes + 1] & last) == 0;

	}

}
