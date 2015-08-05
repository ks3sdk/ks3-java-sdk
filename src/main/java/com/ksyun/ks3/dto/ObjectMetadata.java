package com.ksyun.ks3.dto;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ksyun.ks3.config.Constants;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月16日 下午3:54:46
 * 
 * @description object元数据
 **/
public class ObjectMetadata implements ServerSideEncryptionResult{
	private String usermeta_prefix =  Constants.userMetaPrefix;
	/**
	 * 用户自定义的元数据
	 */
	private Map<String, String> userMetadata = new HashMap<String,String>();
	private Map<String, Object> metadata = new HashMap<String,Object>();
	/**
	 * Http Expires,metadat中预留了一个bucket lifecycle的Expires，所以把它放在了外面
	 */
	private Date httpExpiresDate;
	@Override
	public String toString()
	{
		return StringUtils.object2string(this);
	}
	
	public void setUserMeta(String key,String value)
	{
		this.userMetadata.put(key.startsWith(usermeta_prefix)?key:(usermeta_prefix+key), value);
	}
	public String getUserMeta(String key)
	{
		return userMetadata.get(key.startsWith(usermeta_prefix)?key:(usermeta_prefix+key));
	}
	public boolean containsUserMeta(String key){
		return userMetadata.containsKey(key.startsWith(usermeta_prefix)?key:(usermeta_prefix+key));
	}
	public Map<String, String> getAllUserMeta()
	{
		return this.userMetadata;
	}
	public Map<String, Object> getRawMetadata() {
        return Collections.unmodifiableMap(new HashMap<String,Object>(metadata));
    }
	/**仅供内部使用
	 * @param key
	 * @param value
	 */
    public void setHeader(String key, Object value) {
        metadata.put(key, value);
    }
    public Date getLastModified() {
        return (Date)metadata.get(HttpHeaders.LastModified.toString());
    }
    /**
     * 仅供内部使用
     * @param lastModified
     */
    public void setLastModified(Date lastModified) {
        metadata.put(HttpHeaders.LastModified.toString(), lastModified);
    }
    public long getContentLength() {
        Long contentLength = (Long) metadata.get(HttpHeaders.ContentLength.toString());

        if (contentLength == null) return 0;
        return contentLength;
    }
    /**分块下载时获取文件的总大小*/
    public long getInstanceLength() {
        // See Content-Range in
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
        String contentRange = (String)metadata.get(HttpHeaders.ContentRange.toString());
        if (contentRange != null) {
            int pos = contentRange.lastIndexOf("/");
            if (pos >= 0)
                return Long.parseLong(contentRange.substring(pos+1));
        }
        return getContentLength();
    }
    public void setContentLength(long contentLength) {
        metadata.put(HttpHeaders.ContentLength.toString(), contentLength);
    }
    public String getContentType() {
        return (String)metadata.get(HttpHeaders.ContentType.toString());
    }
    public void setContentType(String contentType) {
        metadata.put(HttpHeaders.ContentType.toString(), contentType);
    }
    public String getContentEncoding() {
        return (String)metadata.get(HttpHeaders.ContentEncoding.toString());
    }
    public void setContentEncoding(String encoding) {
        metadata.put(HttpHeaders.ContentEncoding.toString(), encoding);
    }
    public String getCacheControl() {
        return (String)metadata.get(HttpHeaders.CacheControl.toString());
    }
    public void setCacheControl(String cacheControl) {
        metadata.put(HttpHeaders.CacheControl.toString(), cacheControl);
    }
    public String getContentMD5() {
        return (String)metadata.get(HttpHeaders.ContentMD5.toString());
    }
    public void setContentMD5(String md5Base64) {
        if(md5Base64 == null){
            metadata.remove(HttpHeaders.ContentMD5.toString());
        }else{
            metadata.put(HttpHeaders.ContentMD5.toString(), md5Base64);
        }

    }
    public String getContentDisposition() {
        return (String)metadata.get(HttpHeaders.ContentDisposition.toString());
    }
    public void setContentDisposition(String disposition) {
        metadata.put(HttpHeaders.ContentDisposition.toString(), disposition);
    }
    public String getETag() {
        return (String)metadata.get(HttpHeaders.ETag.toString());
    }
    public void setHttpExpiresDate(Date httpExpiresDate) {
        this.httpExpiresDate = httpExpiresDate;
    }
    public Date getHttpExpiresDate() {
        return httpExpiresDate;
    }
    
    public String getSseAlgorithm(){
    	return (String) metadata.get(HttpHeaders.XKssServerSideEncryption.toString());
    }
    public void setSseAlgorithm(String value){
    	this.metadata.put(HttpHeaders.XKssServerSideEncryption.toString(), value);
    }
    public String getSseKMSKeyId(){
    	return (String) metadata.get(HttpHeaders.XKssServerSideEncryptionKMSKeyId.toString());
    }
    public void setSseKMSKeyId(String value){
    	this.metadata.put(HttpHeaders.XKssServerSideEncryptionKMSKeyId.toString(), value);
    }
    public String getSseCustomerAlgorithm(){
    	return (String) this.metadata.get(HttpHeaders.XKssServerSideEncryptionCustomerAlgorithm.toString());
    }
    /**
     * 仅供内部使用，设置加密请使用</br>{@link PutObjectRequest#setSseCustomerKey(SSECustomerKey key) }</br>{@link UploadPartRequest#setSseCustomerKey(SSECustomerKey key) }
     */
    public void setSseCustomerAlgorithm(String value){
    	this.metadata.put(HttpHeaders.XkssServerSideEncryptionCustomerKey.toString(), value);
    }
    public String getSseCustomerKeyMD5(){
    	return (String) this.metadata.get(HttpHeaders.XkssServerSideEncryptionCustomerKeyMD5.toString());
    }
    /**
     * 仅供内部使用，设置加密请使用</br>{@link PutObjectRequest#setSseCustomerKey(SSECustomerKey key) }</br>{@link UploadPartRequest#setSseCustomerKey(SSECustomerKey key) }
     */
    public void setSseCustomerKeyMD5(String value){
    	this.metadata.put(HttpHeaders.XkssServerSideEncryptionCustomerKeyMD5.toString(), value);
    }
    public Object getMeta(String key)
    {
    	return this.metadata.get(key);
    }
}
