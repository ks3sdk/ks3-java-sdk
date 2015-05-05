package com.ksyun.ks3.service.request;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.StringUtils;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月23日 下午2:17:56
 * 
 * @description 取消分块上传操作
 **/
public class AbortMultipartUploadRequest extends Ks3WebServiceRequest{
	private String bucket;
	private String key;
	/**
	 * 通过Init Multipart Upload 初始化得到的uploadId
	 */
	private String uploadId;
	/**
	 * 
	 * @param bucketname
	 * @param objectkey
	 * @param uploadId
	 */
	public AbortMultipartUploadRequest(String bucketname,String objectkey,String uploadId)
	{
		this.bucket = bucketname;
		this.key = objectkey;
		this.setUploadId(uploadId);
	}

	@Override
	public void validateParams()  {
		if(StringUtils.isBlank(this.bucket))
			throw notNull("bucketname");
		if(StringUtils.isBlank(this.key))
			throw notNull("objectKey");
		if(StringUtils.isBlank(this.uploadId))
			throw notNull("uploadId");
	}
	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.DELETE);
		request.setBucket(bucket);
		request.setKey(key);
		request.addQueryParam("uploadId",this.uploadId);
	}
	/**
	 * 通过Init Multipart Upload 初始化得到的uploadId
	 */
	public String getUploadId() {
		return uploadId;
	}
	/**
	 * 通过Init Multipart Upload 初始化得到的uploadId
	 */
	public void setUploadId(String uploadId) {
		this.uploadId = uploadId;
	}

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
	
}
