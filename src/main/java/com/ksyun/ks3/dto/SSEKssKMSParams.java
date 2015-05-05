package com.ksyun.ks3.dto;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月13日 下午2:34:55
 * 
 * @description 使用KMS时进行服务端加密时需要的参数
 **/
public class SSEKssKMSParams {
	private String keyId;
	/**
	 * 使用默认的KMS key
	 */
	public SSEKssKMSParams(){
		
	}
	/**
	 * 使用指定的KMS key
	 * @param keyId
	 */
	public SSEKssKMSParams(String keyId){
		this.setKeyId(keyId);
	}
	public String getKeyId() {
		return keyId;
	}
	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}
	public String toSting(){
		return StringUtils.object2string(this);
	}
}
