package com.ksyun.ks3.service.response;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

import com.ksyun.ks3.dto.Ks3Result;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月17日 下午2:40:32
 * 
 * @description KS3 http response处理器
 **/
public interface Ks3WebServiceResponse<T>{
	public static int allStatueCode = -1;
	public T handleResponse(HttpRequest httpRequest, HttpResponse response);
	public HttpResponse getResponse();
	public String getRequestId();
	/**
	 * 
	 * @return 期望的 http status
	 */
	public int[] expectedStatus();
}
