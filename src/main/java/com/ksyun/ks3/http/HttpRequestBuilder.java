package com.ksyun.ks3.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.params.CoreProtocolPNames;

import com.ksyun.ks3.MD5DigestCalculatingInputStream;
import com.ksyun.ks3.RepeatableFileInputStream;
import com.ksyun.ks3.RepeatableInputStream;
import com.ksyun.ks3.config.ClientConfig;
import com.ksyun.ks3.config.Constants;
import com.ksyun.ks3.dto.Authorization;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.service.Ks3ClientConfig;
import com.ksyun.ks3.service.Ks3ClientConfig.PROTOCOL;
import com.ksyun.ks3.service.request.Ks3WebServiceRequest;
import com.ksyun.ks3.service.request.SSECustomerKeyRequest;
import com.ksyun.ks3.signer.Signer;
import com.ksyun.ks3.utils.HttpUtils;
import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月17日 下午5:16:35
 * 
 * @description 
 **/
public class HttpRequestBuilder {
	public static HttpRequestBase build(Ks3WebServiceRequest ks3Request,Request request,Authorization auth,Ks3ClientConfig ks3config){	
		ks3Request.validateParams();
		request.getHeaders().putAll(ks3Request.getRequestConfig().getExtendHeaders());
		ks3Request.buildRequest(request);
		request.addHeaderIfNotContains(HttpHeaders.UserAgent.toString(),ks3Request.getRequestConfig().getUserAgent());
		request.addHeaderIfNotContains(HttpHeaders.ContentType.toString(),"application/xml");
		
		String endpoint0 = ks3Request.getRequestConfig().getEndpoint();
		if(StringUtils.isBlank(endpoint0)){
			endpoint0 = ks3config.getEndpoint();
			if(StringUtils.isBlank(endpoint0))
				endpoint0 = ClientConfig.getConfig().getStr(ClientConfig.END_POINT);
		}
		request.setEndpoint(endpoint0);
		//sign request
		try {
			String signerString = ClientConfig.getConfig().getStr(
					ClientConfig.CLIENT_SIGNER);
			Signer signer = (Signer) Class.forName(signerString).newInstance();
			signer.sign(auth, request);
		} catch (Exception e) {
			throw new Ks3ClientException(e);
		}
		//用户自己指定的签名
		String userAuth = ks3Request.getRequestConfig().getExtendHeaders().get(HttpHeaders.Authorization.toString());
		if(!StringUtils.isBlank(userAuth)){
			request.addHeader(HttpHeaders.Authorization, userAuth);
		}
		//build http request
		HttpMethod method = request.getMethod();
		HttpRequestBase httpRequest = null;
		//wrap content
		if(request.getContent()!=null&&!(request.getContent() instanceof RepeatableInputStream)&&!(request.getContent() instanceof RepeatableFileInputStream))
			request.setContent(new RepeatableInputStream(request.getContent(),Constants.DEFAULT_STREAM_BUFFER_SIZE));
		//cal md5 in client
		if (!skipMD5Check(ks3Request, request))
			if (!(request.getContent() instanceof MD5DigestCalculatingInputStream))
				request.setContent(new MD5DigestCalculatingInputStream(request.getContent()));
		//get request url
		String url = "";
		String bucket = request.getBucket();
		String key = request.getKey();
		String endpoint = request.getEndpoint();
		String encodedParams = HttpUtils.encodeParams(request.getQueryParams());
		key = HttpUtils.urlEncode(key, true);
		int format = ClientConfig.getConfig().getInt(
				ClientConfig.CLIENT_URLFORMAT);
		Boolean format0 = ks3config.getPathStyleAccess();
		if(format0 !=null){
			format = format0?1:0;
		}
		
		String protocol = ClientConfig.getConfig().getStr(ClientConfig.HTTP_PROTOCOL);
		PROTOCOL spePro = ks3config.getProtocol();
		if(spePro!=null)
			protocol = spePro.toString();
		if(StringUtils.isBlank(protocol))
			protocol ="http";
		if (format == 0) {
			url = new StringBuffer(protocol+"://")
					.append(StringUtils.isBlank(bucket) ? "" : bucket
							+ ".").append(endpoint)
					.append(StringUtils.isBlank(key) ? "" :"/"+ key)
					.toString();
		} else {
			url = new StringBuffer(protocol+"://")
					.append(endpoint)
					.append(StringUtils.isBlank(bucket) ? "" :"/" +bucket)
					.append(StringUtils.isBlank(key) ? "" : "/"+key)
					.toString();
		}
		if (!StringUtils.isBlank(encodedParams))
			url += "?" + encodedParams;
		//init http request
		InputStream requestBody = request.getContent();
		if (method == HttpMethod.POST) {
			HttpPost postMethod = new HttpPost(url);
			if (requestBody != null ){
				String length = request.getHeaders().get(
						HttpHeaders.ContentLength.toString());
				HttpEntity entity = new RepeatableInputStreamRequestEntity(
						requestBody, length);
				if(!StringUtils.checkLong(length)||Long.parseLong(length)<0){
					try {
						entity = new BufferedHttpEntity(entity);
					} catch (IOException e) {
						e.printStackTrace();
						throw new Ks3ClientException(
							"初始化Http Request出错(" + e + ")", e);
					}
				}
				postMethod.setEntity(entity);
			}
			httpRequest = postMethod;
		} else if (method == HttpMethod.GET) {
			HttpGet getMethod = new HttpGet(url);
			httpRequest = getMethod;
		} else if (method == HttpMethod.PUT) {
			HttpPut putMethod = new HttpPut(url);
			httpRequest = putMethod;

			putMethod.getParams().setParameter(
					CoreProtocolPNames.USE_EXPECT_CONTINUE, true);
			if (requestBody != null) {
				String length = request.getHeaders()
						.get(HttpHeaders.ContentLength.toString());
				HttpEntity entity = null;
				if (length == null||length.trim().equals("0")) {
					try {
						entity = new RepeatableInputStreamRequestEntity(
								requestBody, "-1");
						entity = new BufferedHttpEntity(entity);
					} catch (IOException e) {
						e.printStackTrace();
						throw new Ks3ClientException("初始化Http Request出错(" + e
								+ ")", e);
					}
				} else {
					entity = new RepeatableInputStreamRequestEntity(
							requestBody, length);
				}
				putMethod.setEntity(entity);
			}
		} else if (method == HttpMethod.DELETE) {
			HttpDelete deleteMethod = new HttpDelete(url);
			httpRequest = deleteMethod;
		} else if (method == HttpMethod.HEAD) {
			HttpHead headMethod = new HttpHead(url);
			httpRequest = headMethod;
		} else {
			// 永远不会到这儿
			throw new Ks3ClientException("Unknow http method : "
					+ method);
		}
		//add headers
		for (Entry<String, String> aHeader : request.getHeaders().entrySet()) {
			if (!httpRequest.containsHeader(aHeader.getKey()))
				httpRequest.setHeader(aHeader.getKey(), aHeader.getValue());
		}
		
		httpRequest.removeHeaders(HttpHeaders.ContentLength.toString());
		return httpRequest;
	}
	private static boolean skipMD5Check(Ks3WebServiceRequest wsReq,Request req){
		if(wsReq instanceof SSECustomerKeyRequest){
			if(((SSECustomerKeyRequest)wsReq).getSseCustomerKey()!=null)
				return true;
			if(req.getContent() == null)
				return true;
			if(!StringUtils.isBlank(req.getHeaders().get(HttpHeaders.ContentMD5.toString()))){
				return true;
			}
			return false;
		}else{
			return true;
		}
	}
}
