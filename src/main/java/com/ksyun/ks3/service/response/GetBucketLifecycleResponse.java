package com.ksyun.ks3.service.response;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.ksyun.ks3.dto.BucketLifecycleConfiguration;
import com.ksyun.ks3.dto.BucketLifecycleConfiguration.Rule;
import com.ksyun.ks3.dto.BucketLifecycleConfiguration.Status;
import com.ksyun.ks3.utils.DateUtils;


public class GetBucketLifecycleResponse extends
		Ks3WebServiceXmlResponse<BucketLifecycleConfiguration> {

	private Rule rule = null;
	private List<Rule> rules = new ArrayList<Rule>();

	@Override
	public void startDocument() throws SAXException {
		result = new BucketLifecycleConfiguration();
	}

	@Override
	public void startEle(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		String tag = getTag();
		if ("Rule".equals(tag)) {
			rule = new Rule();
		}
	}

	@Override
	public void endEle(String uri, String localName, String qName)
			throws SAXException {
		String tag = getTag();
		if ("Rule".equals(tag)) {
			rules.add(rule);
		} else if ("LifecycleConfiguration".equals(tag)) {
			result.setRules(rules);
		}
	}

	@Override
	public void string(String s) {
		String tag = getTag();
		if ("ID".equals(tag)) {
			rule.setId(s);
		} else if ("Prefix".equals(tag)) {
			rule.setPrefix(s);
		} else if ("Status".equals(tag)) {
			rule.setStatus(Status.str2Status(s));
		} else if ("Date".equals(tag)) {
			rule.setExpirationDate(DateUtils.convertStr2Date(s));
		} else if ("Days".equals(tag)) {
			rule.setExpirationInDays(Integer.valueOf(s));
		}
	}

	@Override
	public void preHandle() {

	}

	public int[] expectedStatus() {
		return new int[] { 200 };
	}
}
