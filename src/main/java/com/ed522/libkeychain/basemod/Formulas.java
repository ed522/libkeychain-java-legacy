package com.ed522.libkeychain.basemod;

import com.ed522.libkeychain.message.Message;

public class Formulas {

	public static final int ALIAS_REQUEST_BASE = 6;

	private Formulas() {}

	public static int aliasRequest(Message msg) {
		
		// End result = log2(len) + BASE
		// or BASE bits if len == 0

		String[] needed = msg.getField("needed").getStringArray();

		if (needed.length == 0) return ALIAS_REQUEST_BASE;
		else {

			int len = needed.length;
			int log2Result = 0;
			while ((len >> 1) != 0) log2Result++;
			return log2Result;
			
		}

	}

}
