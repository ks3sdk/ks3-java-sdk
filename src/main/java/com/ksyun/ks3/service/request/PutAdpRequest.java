package com.ksyun.ks3.service.request;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.ksyun.ks3.dto.Adp;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.Base64;
import com.ksyun.ks3.utils.HttpUtils;
import com.ksyun.ks3.utils.StringUtils;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年2月2日 下午4:30:13
 * 
 * @description 添加数据处理任务
 **/
public class PutAdpRequest extends Ks3WebServiceRequest{
	private String bucket;
	private String key;
	/**
	 * 要进行的处理任务
	 */
	private List<Adp> adps = new ArrayList<Adp>();
	/**
	 * 数据处理任务完成后通知的url
	 */
	private String notifyURL;
	/**
	 * 
	 * @param bucketName 要处理的数据所在bucket
	 * @param key 要处理的数据的key
	 */
	public PutAdpRequest(String bucketName,String key){
		this.bucket = bucketName;
		this.key = key;
	}
	/**
	 * 
	 * @param bucketName 要处理的数据所在bucket
	 * @param key 要处理的数据的key
	 * @param fops 数据处理指令
	 */
	public PutAdpRequest(String bucketName,String key,List<Adp> adps){
		this(bucketName,key);
		this.setAdps(adps);
	}

	@Override
	public void validateParams() throws IllegalArgumentException {
		if(StringUtils.isBlank(this.bucket))
			throw notNull("bucketname");
		if(StringUtils.isBlank(this.key))
			throw notNull("objectkey");
		if(adps==null){
			throw notNull("adps");
		}else{
			for(Adp adp : adps){
				if(StringUtils.isBlank(adp.getCommand())){
					throw notNull("adps.command");
				}
			}
		}
		if(StringUtils.isBlank(notifyURL))
			throw notNull("notifyURL");
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
	public List<Adp> getAdps() {
		return adps;
	}

	public void setAdps(List<Adp> adps) {
		this.adps = adps;
	}

	public String getNotifyURL() {
		return notifyURL;
	}

	public void setNotifyURL(String notifyURL) {
		this.notifyURL = notifyURL;
	}
	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.PUT);
		request.setBucket(bucket);
		request.setKey(key);
		request.addQueryParam("adp", "");
		request.addHeader(HttpHeaders.AsynchronousProcessingList, URLEncoder.encode(HttpUtils.convertAdps2String(adps)));
		if(!StringUtils.isBlank(notifyURL))
			request.addHeader(HttpHeaders.NotifyURL, HttpUtils.urlEncode(notifyURL,false));
	}

}
