package com.ksyun.ks3.dto;

import javax.crypto.SecretKey;

import com.ksyun.ks3.config.Constants;
import com.ksyun.ks3.utils.Base64;
import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月13日 下午1:54:56
 * 
 * @description 使用客户提供加密key的方式在服务端加密数据
 **/
public class SSECustomerKey {
	private final String base64EncodedKey;
    private String base64EncodedMd5;
    private String algorithm;
    public SSECustomerKey(String base64EncodedKey) {
        if (base64EncodedKey == null || base64EncodedKey.length() == 0)
            throw new IllegalArgumentException("Encryption key must be specified");

        // Default to AES-256 encryption
        this.algorithm = Constants.defaultAlgm;
        this.base64EncodedKey = base64EncodedKey;
    }
    public SSECustomerKey(byte[] rawKeyMaterial) {
        if (rawKeyMaterial == null || rawKeyMaterial.length == 0)
            throw new IllegalArgumentException("Encryption key must be specified");

        // Default to AES-256 encryption
        this.algorithm = Constants.defaultAlgm;
        this.base64EncodedKey = Base64.encodeAsString(rawKeyMaterial);
    }
    public SSECustomerKey(SecretKey key) {
        if (key == null)
            throw new IllegalArgumentException("Encryption key must be specified");

        // Default to AES-256 encryption
        this.algorithm = Constants.defaultAlgm;
        this.base64EncodedKey = Base64.encodeAsString(key.getEncoded());
    }
	public String getBase64EncodedMd5() {
		return base64EncodedMd5;
	}
	public void setBase64EncodedMd5(String base64EncodedMd5) {
		this.base64EncodedMd5 = base64EncodedMd5;
	}
	public String getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	public String getBase64EncodedKey() {
		return base64EncodedKey;
	}
    public String toString(){
    	return StringUtils.object2string(this);
    }
}
