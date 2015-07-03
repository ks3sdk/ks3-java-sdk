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
 * @date 2014年11月13日 下午9:21:57
 * 
 * @description 和{@link Ks3WebServiceDefaultResponse}区别 http request不立即释放
 **/
public abstract class Ks3WebServiceStreamResponse<T> extends Ks3WebServiceResponse<T>{

	protected T result = null;

	protected T abstractHandleResponse() {
		preHandle();
		return result;
	}
	public abstract void preHandle();
	public void onFinally(){
		try {
			closeRequestInputStream();
		} catch (IOException e) {
			LogFactory.getLog(this.getClass()).error("handle response on finally close request inputstream error ,"+e.getMessage());
		}
	}
}
