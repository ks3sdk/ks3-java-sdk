package com.ksyun.ks3.dto;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月23日 上午10:30:46
 * 
 * @description 初始化分块上传的返回结果
 **/
public class InitiateMultipartUploadResult extends SSEResultBase{
	/**
	 * 目标bucket
	 */
	private String bucket;
	/**
	 * 最后complete multipart upload时生成的object的object key
	 */
	private String key;
	/**
	 * upload id
	 * 用于之后的upload part、complete multipart upload、list parts 、abort multipart upload
	 */
	private String uploadId;
	public String getBucket() {
		return bucket;
	}
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getUploadId() {
		return uploadId;
	}
	public void setUploadId(String uploadId) {
		this.uploadId = uploadId;
	}
	public String toString()
	{
		return StringUtils.object2string(this);
	}
}
