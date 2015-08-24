package com.ksyun.ks3.dto;


/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月15日 下午1:43:03
 * 
 * @description 用户访问ks3服务需要的ak sk
 **/
public class Authorization {
	private String accessKeyId;
	private String accessKeySecret;
	public Authorization()
	{
		
	}
	public Authorization(String id,String secret)
	{
		this.accessKeyId = id;
		this.accessKeySecret = secret;
	}
	public String getAccessKeyId() {
		return accessKeyId;
	}
	public void setAccessKeyId(String accessKeyId) {
		this.accessKeyId = accessKeyId;
	}
	public String getAccessKeySecret() {
		return accessKeySecret;
	}
	public void setAccessKeySecret(String accessKeySecret) {
		this.accessKeySecret = accessKeySecret;
	}
}
