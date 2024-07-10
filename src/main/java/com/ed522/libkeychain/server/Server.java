package com.ed522.libkeychain.server;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManagerFactory;

import com.ed522.libkeychain.client.ServerSpec;
import com.ed522.libkeychain.stores.aliasstore.AliasStore;
import com.ed522.libkeychain.stores.keystore.Keystore;
import com.ed522.libkeychain.util.Constants;
import com.ed522.libkeychain.util.CryptoManager;

public class Server {

    public record ServerParameters(boolean trust) {}
    public class ServerAccessor {

        private Server parent;

        private ServerAccessor(Server parent) {
            this.parent = parent;
        }

        public CryptoManager getCryptoManager() {
            return parent.cryptoManager;
        }
        public Keystore getKeystore() {
            return parent.ourStore;
        }
        public AliasStore getAliasStore() {
            return parent.aliasStore;
        }
        public ServerParameters getParams() {
            return parent.params;
        }

    }

    private CryptoManager cryptoManager;
    private AliasStore aliasStore;
    private Keystore ourStore;

    private SSLServerSocket sock;

    private List<ServerInstance> instances = new ArrayList<>();
    private ServerParameters params;


    public static void writeNewServerSpec(String ip, Certificate certificate, File file) throws IOException, CertificateEncodingException {
        
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
            
            out.writeByte(0);
            
            byte[] ipBytes = new byte[4];
            String[] ipRaw = ip.split("\\." /* regex "." is any char */ );
            if (ipRaw.length != 4) throw new IllegalArgumentException("Malformed IP");
            else for (int i = 0; i < ipRaw.length; i++) {
                int raw = Integer.valueOf(ipRaw[i], 10).intValue();
                if (raw > 255) throw new NumberFormatException("Illegal IP: segment over 255");
                else ipBytes[i] = (byte) raw;
            }
            out.write(ipBytes);

            byte[] certEncoded = certificate.getEncoded();
            out.writeInt(certEncoded.length);
            out.write(certEncoded);

        }

    }

    public static boolean registerIfNotExists(String alias, Certificate cert, File store) {

        AliasStore aliasStore = new AliasStore(store, alias);
        if (aliasStore.)

    }


    public Server(File keystoreFile, File aliasStoreFile, ServerParameters params) {
        this.params = params;
        
    }

    public void startSynchronous() throws IOException {

        while (true) {
            instances.add(new ServerInstance(new ServerAccessor(this), this.sock.accept()));
        }

    }

    public Thread startAsync() {
        return new Thread(() -> {
            try {
                this.startSynchronous();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private SSLServerSocket getListener(ServerSpec spec) throws GeneralSecurityException, IOException {
		
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

		return (SSLServerSocket) context.getServerSocketFactory().createServerSocket(Constants.PORT);

	}

}
