package com.ed522.libkeychain.err;

/**
 * Indicates an illegal state that is the result of a
 * programming error.
 * 
 * This should not be caught as it may result in an
 * inconsistent state.
 * 
 * This is intended for states that should never be possible
 * except for a programming error.
 */
public class IllegalStateError extends Error {
	public IllegalStateError() {
		super();
	}
	public IllegalStateError(String msg) {
		super(msg);
	}
	public IllegalStateError(Throwable cause) {
		super(cause);
	}
	public IllegalStateError(String msg, Throwable cause) {
		super(msg, cause);
	}
}
