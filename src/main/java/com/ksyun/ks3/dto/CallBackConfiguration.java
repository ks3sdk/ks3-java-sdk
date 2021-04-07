package com.ksyun.ks3.dto;

import java.util.HashMap;
import java.util.Map;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年2月2日 上午11:57:02
 * 
 * @description PUT Object 和  complete mulitipart upload时设置的callback
 **/
public class CallBackConfiguration {
	public static enum MagicVariables{
		bucket,/**文件上传的Bucket*/
		key,/**文件的名称*/
		etag,/**文件Md5值经过base64处理*/
		encodedKey,/**文件的经过url编码的key*/
		objectSize,/**文件大小*/
		mimeType,/**文件类型*/
		createTime/**文件创建时间.Unix时间戳表示，1420629372，精确到秒*/
	}
	/**
	 * 回调地址
	 */
	private String callBackUrl;

	private Boolean callBackAuth = false;
	/**
	 *KS3 服务器回调时body中带的魔法参数
	 */
	private Map<String,MagicVariables> bodyMagicVariables = new HashMap<String,MagicVariables>();
	/**
	 * KS3 服务器回调时body中带的自定义参数
	 */
	private Map<String,String> bodyKssVariables = new HashMap<String,String>();
	public String getCallBackUrl() {
		return callBackUrl;
	}
	public Boolean getCallBackAuth() {return callBackAuth;}
	public void setCallBackUrl(String callBackUrl) {
		this.callBackUrl = callBackUrl;
	}
	public void setCallBackAuth(Boolean callBackAuth){this.callBackAuth = callBackAuth;}
	
	public Map<String, MagicVariables> getBodyMagicVariables() {
		return bodyMagicVariables;
	}
	public void setBodyMagicVariables(Map<String, MagicVariables> bodyMagicVariables) {
		this.bodyMagicVariables = bodyMagicVariables;
	}
	public Map<String, String> getBodyKssVariables() {
		return bodyKssVariables;
	}
	public void setBodyKssVariables(Map<String, String> bodyKssVariables) {
		this.bodyKssVariables = bodyKssVariables;
	}	
	public String toString()
	{
		return StringUtils.object2string(this);
	}
}
