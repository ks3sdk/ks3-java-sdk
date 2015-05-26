package com.ksyun.ks3.http.internal;
/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年5月26日 上午11:20:05
 * 
 * @description 
 **/
enum TLSProtocol {
    TLSv1_2("TLSv1.2"), // most secure/preferred
    TLSv1_1("TLSv1.1"),
    TLSv1("TLSv1"),
    TLS("TLS"),         // least secure/preferred, but acceptable
    ;
    private final String protocolName;

    private TLSProtocol(String protocolName) {
        this.protocolName = protocolName;
    }

    /**
     * Returns the corresponding TLS protocol name as per the <a href=
     * "http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#jssenames"
     * >JSSE Standard Names</a>
     */
    String getProtocolName() {
        return protocolName;
    }
}