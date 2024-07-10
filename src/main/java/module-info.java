module com.ed522.libkeychain {
    requires java.xml;
    requires org.bouncycastle.pkix;
    requires org.bouncycastle.provider;
	requires java.base;

    exports com.ed522.libkeychain.stores;
    exports com.ed522.libkeychain.stores.aliasstore;
    exports com.ed522.libkeychain.stores.keystore;
    exports com.ed522.libkeychain.transaction;
    exports com.ed522.libkeychain.nametable;
    exports com.ed522.libkeychain.message;
    exports com.ed522.libkeychain.client;
    exports com.ed522.libkeychain.server;
    exports com.ed522.libkeychain.util;
    exports com.ed522.libkeychain.err;
}
