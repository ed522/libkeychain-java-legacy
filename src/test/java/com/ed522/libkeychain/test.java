package com.ed522.libkeychain;

import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class test {
    
    @org.junit.Test
    public void main() throws FileNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException {
        Security.addProvider(new BouncyCastleProvider());
        String[] modes = new String[] {"NONE", "CCM", "CFB", "CTR", "CTS", "ECB", "GCM", "KW", "KWP", "OFB", "PCBC"};
        String[] paddings = new String[] {"NoPadding", "ISO10126Padding", "OAEPPadding", "PKCS1Padding", "PKCS5Padding", "SSL3Padding"};
        for (String mode : modes) {
            for (String padding : paddings) {
                try {
                    Cipher.getInstance("ECIES/" + mode + "/" + padding);
                    System.out.println("Exists: " + mode + "/" + padding);
                } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                    System.out.println("No " + mode + ", error = " + e.getMessage());
                }
            }
        }
    }

}
