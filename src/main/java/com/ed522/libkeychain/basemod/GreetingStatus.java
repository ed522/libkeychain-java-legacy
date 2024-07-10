package com.ed522.libkeychain.basemod;

import java.security.cert.Certificate;

public record GreetingStatus(
    boolean trust,
    boolean success,
    Certificate clientCert,
    Certificate serverCert,
    String clientAlias,
    String[] extGroups,
    String[] extNames,
    boolean enrolled
) {
    // TODO hashCode and equals
}