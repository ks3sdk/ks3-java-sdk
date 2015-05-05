package com.ksyun.ks3.dto;

import java.util.ArrayList;
import java.util.List;

import com.ksyun.ks3.utils.StringUtils;


/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月16日 下午3:26:00
 * 
 * @description GET Bucket(List objects)的返回结果
 **/
public class ObjectListing extends Ks3Result{
    private List<Ks3ObjectSummary> objectSummaries = new ArrayList<Ks3ObjectSummary>();
	/**
	 * 可以理解为文件夹
	 * prefix和delimiter决定结果中的commonPrefix
	 * <p>由prefix和delimiter确定，以prefix开头的object key,在prefix之后第一次出现delimiter的位置之前（包含delimiter）的子字符串将存在于commonPrefixes中</p>
	 * <p>比如有一下两个object key</p>
	 * <p>aaaa/bbb/ddd.txt</p>
	 * <p>aaaa/ccc/eee.txt</p>
	 * <p>ssss/eee/fff.txt</p>
	 * <p>prefix为空 delimiter为/ 则commonPrefix 为 aaaa/和ssss/</p>
	 * <p>prefix为aaaa/  delimiter为/ 则commonPrefix 为 aaaa/bbb/和aaaa/ccc/</p>
	 * <p>prefix为ssss/  delimiter为/ 则commonPrefix 为 aaaa/eee/</p>
	 */
    private List<String> commonPrefixes = new ArrayList<String>();
    private String bucketName;
    /**
     * 若isTruncated为true,则nextMarker可以作为下次请求的marker
     */
    private String nextMarker;
    /**
     * 如果结果被全部列出来则为false
     */
    private boolean isTruncated;
    private String prefix;
    /**
     * 即游标，将列出排在游标之后的object
     */
    private String marker;
    private int maxKeys;
    private String delimiter;
    /**
     * ks3服务器对返回的xml中object key的编码方式
     */
    private String encodingType;
    @Override
    public String toString()
    {
    	return StringUtils.object2string(this);
    }

    public List<Ks3ObjectSummary> getObjectSummaries() {
        return objectSummaries;
    }
    public void setObjectSummaries(List<Ks3ObjectSummary> objs) {
       this.objectSummaries = objs;
    }
    public List<String> getCommonPrefixes() {
        return commonPrefixes;
    }
    public void setCommonPrefixes(List<String> commonPrefixes) {
        this.commonPrefixes = commonPrefixes;
    }
    /**
     * 即游标，将列出排在游标之后的object
     */
    public String getNextMarker() {
        return nextMarker;
    }
    public void setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
    }
    public String getBucketName() {
        return bucketName;
    }
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    public String getPrefix() {
        return prefix;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    public String getMarker() {
        return marker;
    }
    public void setMarker(String marker) {
        this.marker = marker;
    }
    public int getMaxKeys() {
        return maxKeys;
    }
    public void setMaxKeys(int maxKeys) {
        this.maxKeys = maxKeys;
    }
    public String getDelimiter() {
        return delimiter;
    }
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }
    /**
     * isTruncated为true时表示之后还有object
     */
    public boolean isTruncated() {
        return isTruncated;
    }
    public void setTruncated(boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

	public String getEncodingType() {
		return encodingType;
	}

	public void setEncodingType(String encodingType) {
		this.encodingType = encodingType;
	}

}
