package com.ksyun.ks3.dto;
/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月13日 下午4:05:39
 * 
 * @description 
 **/
public interface ServerSideEncryptionResult {
	/**
	 * 指定的服务端加密算法
	 */
	public String getSseAlgorithm();
	/**
	 * 指定的服务端加密算法
	 */
	public void setSseAlgorithm(String sseAlgorithm);
	/**
	 * 当使用KMS是服务端加密使用的kms id
	 */
	public String getSseKMSKeyId();
	/**
	 * 当使用KMS是服务端加密使用的kms id
	 */
	public void setSseKMSKeyId(String sseKMSKeyId);
	/**
	 * 用户指定的服务端加密算法
	 */
	public String getSseCustomerAlgorithm() ;
	/**
	 * 用户指定的服务端加密算法
	 */
	public void setSseCustomerAlgorithm(String sseCustomerAlgorithm) ;
	 /**
     * 用户指定的key的MD5值
     */
	public String getSseCustomerKeyMD5();
	 /**
     * 用户指定的key的MD5值
     */
	public void setSseCustomerKeyMD5(String sseCustomerKeyMD5) ;
}
