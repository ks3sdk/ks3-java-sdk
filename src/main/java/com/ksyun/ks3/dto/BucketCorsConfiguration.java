package com.ksyun.ks3.dto;

import java.util.ArrayList;
import java.util.List;

import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年12月31日 上午10:56:58
 * 
 * @description bucket的跨域资源共享配置
 **/
public class BucketCorsConfiguration extends Ks3Result{
	//一系列的跨域资源共享规则
	private List<CorsRule> rules = new ArrayList<CorsRule>();

	@Override
	public String toString(){
		return StringUtils.object2string(this);
	}
	public List<CorsRule> getRules() {
		return rules;
	}

	public void setRules(List<CorsRule> rules) {
		this.rules = rules;
	}
	public void addRule(CorsRule rule){
		if(rules==null)
			rules = new ArrayList<CorsRule>();
		rules.add(rule);
	}
	public void addRule(List<CorsRule> rule){
		if(rules==null)
			rules = new ArrayList<CorsRule>();
		rules.addAll(rule);
	}
}
