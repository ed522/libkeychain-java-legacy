package com.ed522.libkeychain.client;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.operator.OperatorCreationException;

import com.ed522.libkeychain.err.CorruptDataException;
import com.ed522.libkeychain.stores.keystore.Keystore;
import com.ed522.libkeychain.stores.keystore.KeystoreEntry;
import com.ed522.libkeychain.util.CryptoManager;
import com.ed522.libkeychain.util.Constants;

public class Client {

	private static final String INVALID_STATE = "Invalid state, some entries are missing (use force in the parameters to bypass)";

	public static record Parameters(boolean force) {}

	private static final int add(boolean a, boolean b, boolean c) {
		return (a ? 1 : 0) + (b ? 1 : 0) + (c ? 1 : 0);
	}

	private final Keystore store;
	private final CryptoManager cryptoManager;

	public Client(File keystoreFile, String alias, String password, ServerSpec server, Parameters params) throws IOException, GeneralSecurityException, OperatorCreationException {
		
		if (params == null) params = new Parameters(false);
		this.store = new Keystore(keystoreFile, password);

		int state = add(store.hasCertificate(alias), store.hasPrivate(alias), store.hasSecret(alias));

		if (state == 3) {
			cryptoManager = new CryptoManager(
				store.getPrivate(password),
				store.getCertificate(password),
				store.getSecret(password)
			);
		} else if (state != 0 && !params.force) {
			throw new CorruptDataException(INVALID_STATE);
		} else {
			// regenerate
			cryptoManager = new CryptoManager(alias);
			store.add(new KeystoreEntry(alias, cryptoManager.getKeys().getPrivate()));
			store.add(new KeystoreEntry(alias, cryptoManager.getCert()));
			store.add(new KeystoreEntry(alias, cryptoManager.getSecret()));
		}

	}
	
	public void connect(ServerSpec server) throws GeneralSecurityException, IOException {
		
		

	}

	private SSLSocket connectSocket(ServerSpec spec) throws GeneralSecurityException, IOException {
		
		KeyStore certsStore = KeyStore.getInstance(Constants.REGULAR_STORE_TYPE);
		certsStore.load(null, null); // this is temporary so no integrity checks needed
		certsStore.setCertificateEntry("server", spec.getCert());
		TrustManagerFactory tmFac = TrustManagerFactory.getInstance("PKIX");
		tmFac.init(certsStore);

		KeyStore ourKeys = KeyStore.getInstance(Constants.REGULAR_STORE_TYPE);
		ourKeys.load(null, null); // no integrity checks or confidentiality needed
		ourKeys.setKeyEntry("private", this.cryptoManager.getKeys().getPrivate(), null, new Certificate[] {this.cryptoManager.getCert()});
		ourKeys.setCertificateEntry("public", this.cryptoManager.getCert());
		KeyManagerFactory kmFac = KeyManagerFactory.getInstance("PKIX");
		kmFac.init(ourKeys, null);

		SSLContext context = SSLContext.getInstance("TLSv1.3");
		context.init(kmFac.getKeyManagers(), tmFac.getTrustManagers(), new SecureRandom());

		return (SSLSocket) context.getSocketFactory().createSocket(spec.getIp(), 42069);

	}

}
