package com.ksyun.ks3.service.response;

import java.util.ArrayList;
import java.util.List;

import com.ksyun.ks3.dto.ObjectTag;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.ksyun.ks3.dto.BucketLifecycleConfiguration;
import com.ksyun.ks3.dto.BucketLifecycleConfiguration.Rule;
import com.ksyun.ks3.dto.BucketLifecycleConfiguration.Status;
import com.ksyun.ks3.dto.BucketLifecycleConfiguration.Transition;
import com.ksyun.ks3.service.common.StorageClass;
import com.ksyun.ks3.utils.DateUtils;


public class GetBucketLifecycleResponse extends
		Ks3WebServiceXmlResponse<BucketLifecycleConfiguration> {

	private Rule rule = null;
	private List<Rule> rules = new ArrayList<Rule>();
	private List<Transition> storageTransList;
	private Transition transition = null;
	private List<ObjectTag> tagSet;
	private ObjectTag tagging;

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
			storageTransList = new ArrayList<Transition>();
			tagSet = new ArrayList<ObjectTag>();
		}else if ("Transition".equals(tag)){
			transition = new Transition();
		} else if ("Tag".equals(tag)) {
			tagging = new ObjectTag();
		}
	}

	@Override
	public void endEle(String uri, String localName, String qName)
			throws SAXException {
		String tag = getTag();
		if ("Rule".equals(tag)) {
			if(storageTransList != null && storageTransList.size()>0){
				rule.setStorageTransitions(storageTransList);
			}
			if (tagSet != null && tagSet.size()>0) {
				rule.setTagSet(tagSet);
			}
			rules.add(rule);
		} else if ("LifecycleConfiguration".equals(tag)) {
			result.setRules(rules);
		}else if ("Transition".equals(tag)){
			storageTransList.add(transition);
		} else if ("Tag".equals(tag)) {
			if (tagSet != null) {
				tagSet.add(tagging);
			}
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
		}else if("Expiration".equals(getTag(1))){
			if ("Date".equals(tag)) {
				rule.setExpirationDate(DateUtils.convertStr2Date(s));
			} else if ("Days".equals(tag)) {
				rule.setExpirationInDays(Integer.valueOf(s));
			}
		}
		else if("Transition".equals(getTag(1))){
			if ("Date".equals(tag)) {
				transition.setTransDate(DateUtils.convertStr2Date(s));
			} else if ("Days".equals(tag)) {
				transition.setTransDays(Integer.valueOf(s));
			}
			else if ("StorageClass".equals(tag)) {
				transition.setStorageClass(StorageClass.fromValue(s));
			}
		} else if ("Tag".equals(getTag(1))) {
			if ("Key".equals(tag)) {
				tagging.setKey(s);
			} else if ("Value".equals(tag)) {
				tagging.setValue(s);
			}
		}
		
	}

	@Override
	public void preHandle() {

	}

	public int[] expectedStatus() {
		return new int[] { 200 };
	}
}
