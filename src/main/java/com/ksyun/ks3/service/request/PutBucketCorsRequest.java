package com.ksyun.ks3.service.request;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import com.ksyun.ks3.MD5DigestCalculatingInputStream;
import com.ksyun.ks3.config.Constants;
import com.ksyun.ks3.dto.BucketCorsConfiguration;
import com.ksyun.ks3.dto.CorsRule;
import com.ksyun.ks3.dto.CorsRule.AllowedMethods;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.Md5Utils;
import com.ksyun.ks3.utils.StringUtils;
import com.ksyun.ks3.utils.XmlWriter;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;
import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.between;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年12月31日 上午10:48:25
 * 
 * @description 设置bucket的跨域资源共享
 **/
public class PutBucketCorsRequest extends Ks3WebServiceRequest{
	private String bucket;
	//bucket跨域资源共享规则配置
	private BucketCorsConfiguration bucketCorsConfiguration;
	/**
	 * 
	 * @param bucketName
	 * @param bucketCorsConfiguration {@link BucketCorsConfiguration}
	 */
	public PutBucketCorsRequest(String bucketName,BucketCorsConfiguration bucketCorsConfiguration){
		this.bucket = bucketName;
		this.setBucketCorsConfiguration(bucketCorsConfiguration);
	}

	@Override
	public void validateParams() throws IllegalArgumentException {
		if(StringUtils.isBlank(this.bucket))
			throw notNull("bucketName");
		if(bucketCorsConfiguration==null)
			throw notNull("bucketCorsConfiguration");
		if(bucketCorsConfiguration.getRules()==null||bucketCorsConfiguration.getRules().size()==0)
			throw notNull("bucketCorsConfiguration.rules");
		if(bucketCorsConfiguration.getRules().size()>Constants.corsMaxRules)
			throw between("bucketCorsConfiguration.rules.size()",String.valueOf(bucketCorsConfiguration.getRules().size()),"0",String.valueOf(Constants.corsMaxRules));
		List<CorsRule> rules = bucketCorsConfiguration.getRules();
		for(CorsRule rule:rules){
			if(rule.getAllowedMethods()==null||rule.getAllowedMethods().size()==0){
				throw notNull("bucketCorsConfiguration.rules.allowedMethods");
			}
			if(rule.getAllowedOrigins()==null||rule.getAllowedOrigins().size()==0){
				throw notNull("bucketCorsConfiguration.rules.allowedOrigins");
			}
		}
		
	}
	public BucketCorsConfiguration getBucketCorsConfiguration() {
		return bucketCorsConfiguration;
	}
	public void setBucketCorsConfiguration(
			BucketCorsConfiguration bucketCorsConfiguration) {
		this.bucketCorsConfiguration = bucketCorsConfiguration;
	}
	
	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.PUT);
		request.setBucket(bucket);
		request.addQueryParam("cors","");
		XmlWriter writer = new XmlWriter();
		writer.startWithNs("CORSConfiguration");
		List<CorsRule> rules = bucketCorsConfiguration.getRules();
		for(CorsRule rule : rules){
			writer.start("CORSRule");
			for(AllowedMethods method : rule.getAllowedMethods()){
				writer.start("AllowedMethod").value(method.toString()).end();
			}
			for(String orgin:rule.getAllowedOrigins()){
				writer.start("AllowedOrigin").value(orgin).end();
			}
			if(rule.getMaxAgeSeconds()>0){
				writer.start("MaxAgeSeconds").value(rule.getMaxAgeSeconds()).end();
			}
			if(rule.getExposedHeaders()!=null){
				for(String header : rule.getExposedHeaders()){
					writer.start("ExposeHeader").value(header).end();
				}
			}
			if(rule.getAllowedHeaders()!=null){
				for(String header:rule.getAllowedHeaders()){
					writer.start("AllowedHeader").value(header).end();
				}
			}
			writer.end();
		}
		writer.end();
		String xml = writer.toString();
		request.addHeader(HttpHeaders.ContentMD5, Md5Utils.md5AsBase64(xml.getBytes()));
		request.setContent(new ByteArrayInputStream(xml.getBytes()));
	}
}
