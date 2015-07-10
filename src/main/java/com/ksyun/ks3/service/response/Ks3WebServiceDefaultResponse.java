package com.ksyun.ks3.service.response;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.http.HttpHeaders;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月20日 下午5:13:22
 * 
 * @description 
 **/
public abstract class Ks3WebServiceDefaultResponse<T> extends Ks3WebServiceResponse<T>{
	protected T result = null;
	protected T abstractHandleResponse() {
		preHandle();
		return result;
	}
	public abstract void preHandle();
}
