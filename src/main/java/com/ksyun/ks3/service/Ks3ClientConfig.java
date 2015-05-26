package com.ksyun.ks3.service;
/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年5月14日 下午5:00:38
 * 
 * @description 每个client的单独配置，将会覆盖全局的配置
 **/
public class Ks3ClientConfig {
	public static enum PROTOCOL{
		http,https
	}
	private String endpoint;
	private PROTOCOL protocol = null;
	private Boolean pathStyleAccess = null;
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public PROTOCOL getProtocol() {
		return protocol;
	}
	public void setProtocol(PROTOCOL protocol) {
		this.protocol = protocol;
	}
	public Boolean getPathStyleAccess() {
		return pathStyleAccess;
	}
	public void setPathStyleAccess(Boolean pathStyleAccess) {
		this.pathStyleAccess = pathStyleAccess;
	}
}
