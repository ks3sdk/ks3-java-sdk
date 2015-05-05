package com.ksyun.ks3.dto;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpRequest;

import com.ksyun.ks3.AutoAbortInputStream;
import com.ksyun.ks3.utils.StringUtils;
/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月16日 下午3:54:01
 * 
 * @description 存放object信息
 **/
public class Ks3Object  implements Closeable{
	/**
	 * object key
	 */
	private String key = null;
	/**
	 * 存放该object的bucket
	 */
	private String bucketName = null;
	/**
	 * object 元数据
	 */
	private ObjectMetadata objectMetadata;
	/**
	 * object 数据
	 */
	private AutoAbortInputStream objectContent;
	/**
	 * 暂不支持
	 */
	private String redirectLocation;
	
	@Override
	public String toString()
	{
		return StringUtils.object2string(this);
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public AutoAbortInputStream getObjectContent() {
		return objectContent;
	}

	public void setObjectContent(AutoAbortInputStream objectContent) {
		this.objectContent = objectContent;
	}
	public void setObjectContent(InputStream inputStream){
		AutoAbortInputStream pre = this.getObjectContent();
		HttpRequest request = null;
		if(pre!=null)
			request = pre.getRequest();
		this.setObjectContent(new AutoAbortInputStream(inputStream,request));
	}

	public void close() throws IOException {
		if(objectContent!=null)
		{
			objectContent.close();
		}
	}

	public ObjectMetadata getObjectMetadata() {
		return objectMetadata;
	}

	public void setObjectMetadata(ObjectMetadata objectMetadata) {
		this.objectMetadata = objectMetadata;
	}

	public String getRedirectLocation() {
		return redirectLocation;
	}

	public void setRedirectLocation(String redirectLocation) {
		this.redirectLocation = redirectLocation;
	}

}
