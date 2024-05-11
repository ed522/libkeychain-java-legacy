package com.ed522.libkeychain.message;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

import com.ed522.libkeychain.util.Constants;

public class ProofJob extends Thread {

	// optimizations
	private final int mod8;
	private final int bytes;
	private final byte lastBits;

	private final BigInteger start;
	private final BigInteger end;
	private final byte[] data;
	private final Function<byte[], ?> onComplete;
	private boolean stopped = false;

	public ProofJob(Function<byte[], ?> onComplete, byte[] data, int factor, byte[] start, byte[] end) {
		
		this.onComplete = onComplete;
		this.data = data;
		this.start = new BigInteger(start);
		this.end = new BigInteger(end);
		
		// optimizations
		this.mod8 = factor % 8;
		this.bytes = (factor - mod8) / 8;
		this.lastBits = (byte) (-1 >> mod8);

	}

	public void end() {
		this.stopped = true;
	}

	@Override
	public void run() {

		try {
			
			MessageDigest digest = MessageDigest.getInstance(Constants.HASH_NAME);
			for (BigInteger i = start; i.compareTo(end) < 1; i = i.add(BigInteger.ONE)) {
			
				if (this.stopped)
					return;
			
				if (this.test(i.toByteArray(), digest)) {
					if (onComplete != null) onComplete.apply(i.toByteArray());
					return;
				}
			
			}

		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}

	}

	public boolean test(byte[] proof, MessageDigest digest) {

		digest.update(this.data);
		digest.update(proof);
		byte[] result = digest.digest();

		for (int i = 0; i < bytes; i++) {
			if (result[i] != 0) return false;
		}

		return (result[bytes + 1] & lastBits) == 0;

	}

}
