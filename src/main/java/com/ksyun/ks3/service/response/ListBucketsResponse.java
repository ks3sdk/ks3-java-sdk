package com.ksyun.ks3.service.response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.ksyun.ks3.dto.Bucket;
import com.ksyun.ks3.dto.Owner;
import com.ksyun.ks3.service.common.BucketType;
import com.ksyun.ks3.utils.DateUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月14日 下午7:49:24
 * 
 * @description 
 **/
public class ListBucketsResponse extends Ks3WebServiceXmlResponse<List<Bucket>>{
	private Owner owner;
	private Bucket bucket;
	@Override
	public void startDocument() throws SAXException {
		result = new ArrayList<Bucket>();
	}
	@Override
	public void startEle(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if("Owner".equalsIgnoreCase(this.getTag()))
		{
			owner = new Owner();
		}
		if("Bucket".equalsIgnoreCase(this.getTag()))
		{
			bucket = new Bucket();
		}
	}
	@Override
	public void endEle(String uri, String localName, String qName)
			throws SAXException {
		if("Bucket".equalsIgnoreCase(getTag()))
		{
			bucket.setOwner(owner);
			result.add(bucket);
		}
	}
	@Override
	public void string(String s) {
		if("Owner".equalsIgnoreCase(getTag(1))&&"ID".equalsIgnoreCase(getTag()))
		{
			owner.setId(s);
		}
		if("Owner".equalsIgnoreCase(getTag(1))&&"DisplayName".equalsIgnoreCase(getTag()))
		{
			owner.setDisplayName(s);
		}
		if("Bucket".equalsIgnoreCase(getTag(1)))
		{
			if("Name".equalsIgnoreCase(getTag()))
			{
				bucket.setName(s);
			}
			if("Region".equalsIgnoreCase(getTag()))
			{
				bucket.setRegion(s);
			}
			if("Type".equalsIgnoreCase(getTag()))
			{
				bucket.setType(BucketType.fromValue(s));
			}
			if("CreationDate".equalsIgnoreCase(getTag()))
			{
				bucket.setCreationDate((DateUtils.convertStr2Date(s)));
			}
		}
	}
	@Override
	public void preHandle() {
	}
	public int[] expectedStatus() {
		// TODO Auto-generated method stub
		return new int[]{200};
	}
}
