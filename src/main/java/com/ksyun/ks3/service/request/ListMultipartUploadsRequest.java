package com.ksyun.ks3.service.request;

import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;
import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.between;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月16日
 * 
 * @description 列出bucket下未进行complete或abort的分块上传.该分块上传的块依然占有用户的存储空间，
 *              可以隔一段时间将bucket下未complete或abort的分块上传进行complete或abort操作
 **/
public class ListMultipartUploadsRequest extends Ks3WebServiceRequest {
	private String bucket;
	/**
	 * prefix和delimiter详解
	 * <p>
	 * commonPrefix由prefix和delimiter确定，以prefix开头的object
	 * key,在prefix之后第一次出现delimiter的位置之前（包含delimiter）的子字符串将存在于commonPrefixes中
	 * </p>
	 * <p>
	 * 比如有一下几个个分块上传
	 * </p>
	 * <p>
	 * aaaa/bbb/ddd.txt
	 * </p>
	 * <p>
	 * aaaa/ccc/eee.txt
	 * </p>
	 * <p>
	 * ssss/eee/fff.txt
	 * </p>
	 * <p>
	 * prefix为空 delimiter为/ 则commonPrefix 为 aaaa/和ssss/ 返回的uploads为空
	 * </p>
	 * <p>
	 * prefix为aaaa/ delimiter为/ 则commonPrefix 为 aaaa/bbb/和aaaa/ccc/ 返回的uploads为空
	 * </p>
	 * <p>
	 * prefix为ssss/ delimiter为/ 则commonPrefix 为 aaaa/eee/ 返回的uploads为空
	 * </p>
	 * <p>
	 * prefix为空 delimiter为空 则commonPrefix 为空
	 * 返回的uploads为aaaa/bbb/ddd.txt、aaaa/ccc/eee.txt、ssss/eee/fff.txt
	 * </p>
	 * <p>
	 * prefix为aaaa/ delimiter为空 则commonPrefix 为空
	 * 返回的uploads为aaaa/bbb/ddd.txt、aaaa/ccc/eee.txt
	 * </p>
	 * <p>
	 * prefix为ssss/ delimiter为空 则commonPrefix 为空 返回的uploads为ssss/eee/fff.txt
	 * </p>
	 * <p>
	 * 由于分布式文件存储系统中没有文件夹结构，所以用delimiter和prefix模拟文件夹结构,可以把prefix看成当前在哪个文件夹下，
	 * delimiter为文件夹分隔符，commonprefix为当前文件夹下的子文件夹
	 * </p>
	 */
	private String delimiter;
	/**
	 * <p>
	 * keyMarker为空，uploadIdMarker不为空
	 * <P>
	 * <p>
	 * 无意义
	 * </p>
	 * <p>
	 * keyMarker不为空，uploadIdMarker不为空
	 * <P>
	 * <p>
	 * 列出分块上传object key为keyMarker，且upload id 字典排序大于uploadIdMarker的结果
	 * </p>
	 * <p>
	 * keyMarker不为空，uploadIdMarker为空
	 * <P>
	 * <p>
	 * 列出分块上传object key字典排序大于keyMarker的结果
	 * </p>
	 */
	private String keyMarker;
	private String uploadIdMarker;
	/**
	 * 设置返回结果数，1-1000
	 */
	private Integer maxUploads;

	private String prefix;
	/**
	 * 要求ks3服务器对返回的结果中object key进行编码
	 */
	private String encodingType;

	public ListMultipartUploadsRequest(String bucketName) {
		this.bucket = bucketName;
	}

	public ListMultipartUploadsRequest(String bucketName, String prefix) {
		this.bucket = bucketName;
		this.setPrefix(prefix);
	}

	public ListMultipartUploadsRequest(String bucketName, String prefix,
			String keyMarker, String uploadIdMarker) {
		this.bucket = bucketName;
		this.setPrefix(prefix);
		this.setKeyMarker(keyMarker);
		this.setUploadIdMarker(uploadIdMarker);
	}

	@Override
	public void validateParams() throws IllegalArgumentException {
		if (StringUtils.isBlank(this.bucket))
			throw notNull("bucketName");
		if (this.maxUploads != null
				&& (this.maxUploads > 1000 || this.maxUploads < 1))
			throw between(
					"maxUploads",String.valueOf(this.maxUploads),"1","1000");
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getKeyMarker() {
		return keyMarker;
	}

	public void setKeyMarker(String keyMarker) {
		this.keyMarker = keyMarker;
	}

	public String getUploadIdMarker() {
		return uploadIdMarker;
	}

	public void setUploadIdMarker(String uploadIdMarker) {
		this.uploadIdMarker = uploadIdMarker;
	}

	public Integer getMaxUploads() {
		return maxUploads;
	}

	public void setMaxUploads(Integer maxUploads) {
		this.maxUploads = maxUploads;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
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
		request.getQueryParams().put("uploads", null);
		request.addQueryParamIfNotNull("prefix", prefix);
		request.addQueryParamIfNotNull("key-marker", this.keyMarker);
		request.addQueryParamIfNotNull("upload-id-​marker", this.uploadIdMarker);
		request.addQueryParamIfNotNull("delimiter", delimiter);
		if (this.maxUploads != null)
			request.addQueryParamIfNotNull("max-uploads", String.valueOf(maxUploads));
		if (!StringUtils.isBlank(this.encodingType))
			request.addQueryParamIfNotNull("encoding-type", this.encodingType);
	}
}
