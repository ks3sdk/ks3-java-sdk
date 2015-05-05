package com.ksyun.ks3.service.request;

import java.io.ByteArrayInputStream;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import com.ksyun.ks3.MD5DigestCalculatingInputStream;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.Md5Utils;
import com.ksyun.ks3.utils.StringUtils;
import com.ksyun.ks3.utils.XmlWriter;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月10日 下午2:44:52
 * 
 * @description 批量删除object的请求
 **/
public class DeleteMultipleObjectsRequest extends Ks3WebServiceRequest{
	private String bucket;
	/**
	 * 要删除的object key
	 */
	private String[] keys = new String[]{};
	public DeleteMultipleObjectsRequest(String bucketName,List<String> keys)
	{
		this.bucket = bucketName;
		this.keys = keys.toArray(this.keys);
	}
	public DeleteMultipleObjectsRequest(String bucketName,String[] keys)
	{
		this.bucket = bucketName;
		this.keys = keys;
	}

	@Override
	public void validateParams() throws IllegalArgumentException {
		if(StringUtils.isBlank(this.bucket))
			throw notNull("bucketname");
		if(this.keys == null||this.keys.length==0)
			throw notNull("keys");
	}
	public String[] getKeys() {
		return keys;
	}
	/**
	 * 设置要删除的object keys
	 * @param keys
	 */
	public void setKeys(String[] keys) {
		this.keys = keys;
	}
	
	public String getBucket() {
		return bucket;
	}
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.POST);
		request.setBucket(bucket);
		request.getQueryParams().put("delete","");
		XmlWriter writer = new XmlWriter();
		writer.start("Delete");
		for(int i =0;i<this.keys.length;i++)
		{
			writer.start("Object").start("Key").value(keys[i]).end().end();
		}
		writer.end();
		String xml = writer.toString();
		request.addHeader(HttpHeaders.ContentMD5, Md5Utils.md5AsBase64(xml.getBytes()));
		request.setContent(new ByteArrayInputStream(xml.getBytes()));
	}
	
}
