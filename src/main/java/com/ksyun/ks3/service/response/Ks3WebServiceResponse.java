package com.ksyun.ks3.service.response;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.xml.sax.helpers.DefaultHandler;

import com.ksyun.ks3.dto.Ks3Result;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.http.HttpHeaders;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月17日 下午2:40:32
 * 
 * @description KS3 http response处理器
 **/
public abstract class Ks3WebServiceResponse<T> extends DefaultHandler{
	private HttpRequest httpRequest;
	private HttpResponse httpResponse;
	protected abstract T abstractHandleResponse();
	public T handleResponse(){
		T ret = abstractHandleResponse();
		if(ret instanceof Ks3Result){
			((Ks3Result)ret).setRequestId(this.getRequestId());
			((Ks3Result)ret).setOriginRequest(httpRequest);
			((Ks3Result)ret).setOriginResponse(httpResponse);
		}
		return ret;
	}
	public HttpRequest getHttpRequest() {
		return httpRequest;
	}

	public void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}
	protected Header[] getHeaders(String key)
	{
		return this.httpResponse.getHeaders(key);
	}
	protected String getHeader(String key)
	{
		Header[] headers = getHeaders(key);
		if(headers.length>0)
		{
			return headers[0].getValue();
		}
		return "";
	}
	protected InputStream getContent()
	{
		try {
			return this.httpResponse.getEntity().getContent();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Ks3ClientException("无法读取http response的body("+e+")",e);
		}
	}
	public String getRequestId()
	{
		return this.getHeader(HttpHeaders.RequestId.toString());
	}
	protected void closeRequestInputStream() throws IOException{
		if(this.httpRequest == null)
			return;
		HttpEntity entity = null;
		if(httpRequest instanceof HttpPut){
			entity = ((HttpPut)httpRequest).getEntity();
		}else if(httpRequest instanceof HttpPost){
			entity = ((HttpPost)httpRequest).getEntity();
		}
		if(entity != null){
			InputStream input = entity.getContent();
			if(input!=null)
				input.close();
		}
	}
	protected void closeResponseInputStream() throws IOException{
		if(this.httpResponse ==null)
			return;
		HttpEntity entity = this.httpResponse.getEntity();
		if(entity != null){
			InputStream input = null;
			try{
				input = entity.getContent();
			}catch(IllegalStateException e){
				input = null;
			}
			if(input != null)
				input.close();
		}
	}
	protected void abortRequest(){
		if(this.httpRequest!=null&&this.httpRequest instanceof HttpRequestBase)
			((HttpRequestBase) this.httpRequest).abort();
	}
	/**
	 * 
	 * @return 期望的 http status
	 */
	public abstract int[] expectedStatus();
	public void onFinally(){
		try {
			closeRequestInputStream();
		} catch (IOException e) {
			LogFactory.getLog(this.getClass()).error("handle response on finally close request inputstream error ,"+e.getMessage());
		}
		try {
			closeResponseInputStream();
		} catch (IOException e) {
			LogFactory.getLog(this.getClass()).error("handle response on finally close response inputstream error ,"+e.getMessage());
		}
		abortRequest();
	}
}
