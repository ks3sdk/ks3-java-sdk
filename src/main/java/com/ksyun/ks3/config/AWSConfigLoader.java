package com.ksyun.ks3.config;
/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月13日 上午11:50:28
 * 
 * @description 
 **/
public class AWSConfigLoader implements ConfigLoader{
	public ClientConfig load(ClientConfig config) {
		config.set(ClientConfig.END_POINT,"s3.amazonaws.com");
		config.set(ClientConfig.CLIENT_SIGNER,"com.ksyun.ks3.signer.DefaultSigner");
		config.set(ClientConfig.CLIENT_URLFORMAT, "0");
		config.set(ClientConfig.CDN_END_POINT, "s3.amazonaws.com");
		config.set(ClientConfig.HEADER_PREFIX, "x-amz-");
		config.set(ClientConfig.USER_META_PREFIX,"x-amz-meta-");
		config.set(ClientConfig.GRANTEE_ALLUSER,"http://acs.amazonaws.com/groups/global/AllUsers");
		config.set(ClientConfig.AUTH_HEADER_PREFIX, "AWS");
		config.set(ClientConfig.HTTP_PROTOCOL,"https");
		return config;
	}
}
