package com.ksyun.ks3;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月13日 下午8:55:51
 * 
 * @description 
 **/
public class AutoAbortInputStream extends FilterInputStream{
	private HttpRequest request;
	public AutoAbortInputStream(InputStream in,HttpRequest request) {
		super(in);
		this.request = request;
	}
	@Override
	public int read() throws IOException {
		int ch = super.read();
		if(ch==-1){
			this.abort();
		}
		return ch;
	}
	@Override
	public int read(byte b[], int off, int len) throws IOException{
		int i = super.read(b,off,len);
		if(i == -1)
			this.abort();
		return i;
	}
	public void abort(){
		if(this.request == null)
			return;
		if(this.request instanceof HttpRequestBase){
			((HttpRequestBase) request).abort();
		}
	}
	@Override
	public void close() throws IOException{
		super.close();
		this.abort();
	}
	public HttpRequest getRequest() {
		return request;
	}
	public void setRequest(HttpRequest request) {
		this.request = request;
	}
	
}
