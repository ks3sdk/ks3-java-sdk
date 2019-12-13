package com.ksyun.ks3.service.request;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.between;
import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.ksyun.ks3.config.Constants;
import com.ksyun.ks3.dto.BucketLifecycleConfiguration;
import com.ksyun.ks3.dto.BucketLifecycleConfiguration.Rule;
import com.ksyun.ks3.dto.BucketLifecycleConfiguration.Transition;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.DateUtils;
import com.ksyun.ks3.utils.Md5Utils;
import com.ksyun.ks3.utils.StringUtils;
import com.ksyun.ks3.utils.XmlWriter;

public class PutBucketLifecycleRequest extends Ks3WebServiceRequest {
	private String bucketName;
	private BucketLifecycleConfiguration lifecycleConfiguration;

	public PutBucketLifecycleRequest(String bucketName,
			BucketLifecycleConfiguration lifecycleConfiguration) {
		this.bucketName = bucketName;
		this.lifecycleConfiguration = lifecycleConfiguration;
	}

	public String getBucketName() {
		return this.bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}


	public BucketLifecycleConfiguration getLifecycleConfiguration() {
		return this.lifecycleConfiguration;
	}

	public void setLifecycleConfiguration(BucketLifecycleConfiguration lifecycleConfiguration) {
		this.lifecycleConfiguration = lifecycleConfiguration;
	}


	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.PUT);
		request.setBucket(bucketName);
		request.addQueryParam("lifecycle","");
		XmlWriter writer = new XmlWriter();
		writer.start("LifecycleConfiguration");
		List<Rule> rules = lifecycleConfiguration.getRules();
		for(Rule rule : rules){
			writer.start("Rule");
			writer.start("ID").value(rule.getId()).end();
			//Filter
			writer.start("Filter");
			writer.start("Prefix").value(rule.getPrefix()).end();
			writer.end();
			//Status
			writer.start("Status").value(rule.getStatus().status2Str()).end();
			
			if (rule.getExpirationDate() != null || (rule.getExpirationInDays() != null &&rule.getExpirationInDays() > 0)) {
				//Expiration
				writer.start("Expiration");
				if (rule.getExpirationDate() != null) {
					//timezone?
					writer.start("Date").value(getISO8601Timestamp(rule.getExpirationDate())).end();
				} else {
					if (rule.getExpirationInDays() > 0) {
						writer.start("Days").value(rule.getExpirationInDays()).end();
					}
				}
				writer.end();
			}
			if(rule.getStorageTransitions() != null 
					&& rule.getStorageTransitions().size()>0){
				for(Transition trans : rule.getStorageTransitions()){
					writer.start("Transition");
					writer.start("StorageClass").value(trans.getStorageClass().toString()).end();
					if(trans.getTransDate() != null){
						//timezone?
						writer.start("Date").value(getISO8601Timestamp(trans.getTransDate())).end();
					}
					else{
						if(trans.getTransDays() > 0){
						 writer.start("Days").value(trans.getTransDays()).end();
						}
					}
					writer.end();
				}
				
			}
			
			writer.end();
		}
		writer.end();
		String xml = writer.toString();
		request.addHeader(HttpHeaders.ContentMD5, Md5Utils.md5AsBase64(xml.getBytes()));
		request.setContent(new ByteArrayInputStream(xml.getBytes()));

	}

	@Override
	public void validateParams() {
		if(StringUtils.isBlank(this.bucketName))
			throw notNull("bucketName");
		if(lifecycleConfiguration == null)
			throw notNull("lifecycleConfiguration");
		if(lifecycleConfiguration.getRules() == null||lifecycleConfiguration.getRules().size() == 0)
			throw notNull("bucketLifecycleConfiguration.rules");
		if(lifecycleConfiguration.getRules().size() > Constants.lifecycleMaxRules)
			throw between("lifecycleConfiguration.rules.size()",String.valueOf(lifecycleConfiguration.getRules().size()),"0",String.valueOf(Constants.lifecycleMaxRules));
		List<Rule> rules = lifecycleConfiguration.getRules();
		for(Rule rule : rules){
			rule.validate();
		}
		

	}
	
	
	public static String getISO8601Timestamp(Date date){  
//		return ISO8601Utils.format(DateUtils.truncateToDay(date), false, TimeZone.getTimeZone("Asia/Shanghai"));
		return ISO8601Utils.format(DateUtils.truncateToDay(date));
	} 
	
}