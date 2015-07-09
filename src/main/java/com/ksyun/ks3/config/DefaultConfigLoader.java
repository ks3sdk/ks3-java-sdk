package com.ksyun.ks3.config;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月14日 下午8:09:45
 * 
 * @description 默认的配置加载器
 **/
public class DefaultConfigLoader implements ConfigLoader{
	public ClientConfig load(ClientConfig config) {
		config.set(ClientConfig.CONNECTION_TIMEOUT, "50000");
		config.set(ClientConfig.SOCKET_TIMEOUT,"50000");
		config.set(ClientConfig.SOCKET_SEND_BUFFER_SIZE_HINT,"8192");
		config.set(ClientConfig.SOCKET_RECEIVE_BUFFER_SIZE_HINT,"8192");
		config.set(ClientConfig.MAX_RETRY,"5");
		config.set(ClientConfig.CONNECTION_TTL,"-1");
		config.set(ClientConfig.MAX_CONNECTIONS,"50");
		config.set(ClientConfig.PROXY_HOST,null);
		config.set(ClientConfig.PROXY_PORT,"-1");
		config.set(ClientConfig.PROXY_DAMAIN,null);
		config.set(ClientConfig.PROXY_PASSWORD,null);
		config.set(ClientConfig.PROXY_USER_NAME,null);
		config.set(ClientConfig.PROXY_WORKSTATION,null);
		config.set(ClientConfig.IS_PREEMPTIVE_BASIC_PROXY_AUTH,"false");
		config.set(ClientConfig.END_POINT,"kssws.ks-cdn.com");
		config.set(ClientConfig.CLIENT_SIGNER,"com.ksyun.ks3.signer.DefaultSigner");
		config.set(ClientConfig.CLIENT_URLFORMAT, "0");
		config.set(ClientConfig.HEADER_PREFIX, "x-kss-");
		config.set(ClientConfig.USER_META_PREFIX,"x-kss-meta-");
		config.set(ClientConfig.GRANTEE_ALLUSER,"http://acs.ksyun.com/groups/global/AllUsers");
		config.set(ClientConfig.AUTH_HEADER_PREFIX, "KSS");
		config.set(ClientConfig.HTTP_PROTOCOL,"http");
		return config;
	}

}
