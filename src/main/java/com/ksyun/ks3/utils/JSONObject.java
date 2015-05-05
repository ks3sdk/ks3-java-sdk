package com.ksyun.ks3.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月8日 下午4:56:01
 * 
 * @description 
 **/
public class JSONObject {
	@SuppressWarnings("rawtypes")
	private Map map = new HashMap();
	public JSONObject(){
		
	}
	public JSONObject(String json){
		this.map = Jackson.fromJsonString(json, Map.class);
	}
	public JSONObject(Map map){
		this.map = map;
	}
	public String getString(String key){
		return String.valueOf(map.get(key));
	}
	public String tryGetString(String key){
		if(!map.containsKey(key))
			return null;
		return getString(key);
	}
	public Iterator<String> keys(){
		return map.keySet().iterator();
	}
	public void put(String key,String value){
		this.map.put(key, value);
	}
	public void put(String key,Map value){
		this.map.put(key, value);
	}
	public String toString(){
		return Jackson.toJsonString(map);
	}
}
