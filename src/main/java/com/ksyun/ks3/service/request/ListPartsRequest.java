package com.ksyun.ks3.service.request;

import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;
import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.between;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月23日 下午2:45:35
 * 
 * @description 列出某个uploadid下已上传的块
 **/
public class ListPartsRequest extends Ks3WebServiceRequest{
	private String bucket;
	private String key;
	/**
	 * 由init multipart upload 获取到的upload id
	 */
	private String uploadId;
	/**
	 * 列出的最大数，1-1000
	 */
	private Integer maxParts = 1000;
	/**
	 * 游标，将列出partNumber比该值大的part
	 */
	private Integer partNumberMarker = -1;
	/**
	 * 要求Ks3服务器对返回结果的objectkey进行编码
	 */
	private String encodingType;
	public ListPartsRequest(String bucketname,String objectkey,String uploadId)
	{
		this.bucket = bucketname;
		this.key =  objectkey;
		this.uploadId = uploadId;
	}

	@Override
	public void validateParams() throws IllegalArgumentException {
		if(StringUtils.isBlank(this.bucket))
			throw notNull("bucketname");
		if(StringUtils.isBlank(this.key))
			throw notNull("objectkey");
		if(StringUtils.isBlank(this.uploadId))
			throw notNull("uploadId");
		if(this.maxParts!=null&&(this.maxParts>1000||this.maxParts<1))
			throw between("maxParts",String.valueOf(this.maxParts),"1","1000");
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

	public String getUploadId() {
		return uploadId;
	}
	public void setUploadId(String uploadId) {
		this.uploadId = uploadId;
	}
	public Integer getMaxParts() {
		return maxParts;
	}
	public void setMaxParts(Integer maxParts) {
		this.maxParts = maxParts;
	}
	public Integer getPartNumberMarker() {
		return partNumberMarker;
	}
	public void setPartNumberMarker(Integer partNumberMarker) {
		this.partNumberMarker = partNumberMarker;
	}
	public String getEncodingType() {
		return encodingType;
	}
	public void setEncodingType(String encodingType) {
		this.encodingType = encodingType;
	}
	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.GET);
		request.setBucket(bucket);
		request.setKey(key);
		if(this.maxParts!=null)
			request.addQueryParamIfNotNull("max-parts",String.valueOf(this.maxParts));
		request.addQueryParamIfNotNull("uploadId",this.uploadId);
		if(partNumberMarker!=null&&this.partNumberMarker>=0)
		{
			request.addQueryParamIfNotNull("part-number​-marker", String.valueOf(this.partNumberMarker));
		}
		request.addQueryParamIfNotNull("encoding-type",this.encodingType);
	}
}
