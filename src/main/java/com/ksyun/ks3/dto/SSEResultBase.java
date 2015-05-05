package com.ksyun.ks3.dto;
/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月13日 下午4:00:45
 * 
 * @description 
 **/
public class SSEResultBase extends Ks3Result implements ServerSideEncryptionResult{
	/**
	 * 指定的服务端加密算法
	 */
	private String sseAlgorithm;
	/**
	 * 当使用KMS是服务端加密使用的kms id
	 */
	private String sseKMSKeyId;
	/**
	 * 用户指定的服务端加密算法
	 */
    private String sseCustomerAlgorithm;
    /**
     * 用户指定的key的MD5值
     */
    private String sseCustomerKeyMD5;
	public String getSseAlgorithm() {
		return sseAlgorithm;
	}
	public void setSseAlgorithm(String sseAlgorithm) {
		this.sseAlgorithm = sseAlgorithm;
	}
	public String getSseKMSKeyId() {
		return sseKMSKeyId;
	}
	public void setSseKMSKeyId(String sseKMSKeyId) {
		this.sseKMSKeyId = sseKMSKeyId;
	}
	public String getSseCustomerAlgorithm() {
		return sseCustomerAlgorithm;
	}
	public void setSseCustomerAlgorithm(String sseCustomerAlgorithm) {
		this.sseCustomerAlgorithm = sseCustomerAlgorithm;
	}
	public String getSseCustomerKeyMD5() {
		return sseCustomerKeyMD5;
	}
	public void setSseCustomerKeyMD5(String sseCustomerKeyMD5) {
		this.sseCustomerKeyMD5 = sseCustomerKeyMD5;
	}
    
}
