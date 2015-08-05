package com.ksyun.ks3.service.response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;

import com.ksyun.ks3.config.Constants;
import com.ksyun.ks3.dto.HeadObjectResult;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.utils.Converter;
import com.ksyun.ks3.utils.DateUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月22日 下午7:52:42
 * 
 * @description
 **/
public class HeadObjectResponse extends
		Ks3WebServiceDefaultResponse<HeadObjectResult> {
	private static Log log = LogFactory.getLog(HeadObjectResponse.class);

	public int[] expectedStatus() {
		/**
		 * 200 正常 206加rang的时候 304 not modified 412 Precondition Failed
		 */
		return new int[] { 200, 206, 304, 412 };
	}

	@Override
	public void preHandle() {
		result = new HeadObjectResult();
		int statusCode = this.getHttpResponse().getStatusLine().getStatusCode();
		if (statusCode == 200 || statusCode == 206) {
			ObjectMetadata metaData = new ObjectMetadata();
			Header[] headers = this.getHttpResponse().getAllHeaders();
			for (int i = 0; i < headers.length; i++) {
				if (headers[i].getName().startsWith(Constants.userMetaPrefix)) {
					metaData.setUserMeta(headers[i].getName(),
							headers[i].getValue());
				} else {
					String key = headers[i].getName();
					String value = headers[i].getValue();
					if (Constants.KS3_IGNOREG_HEADERS.contains(key)) {
						// ignore...
					} else if (key.equals(HttpHeaders.LastModified.toString())) {
						try {
							metaData.setLastModified(DateUtils
									.convertStr2Date(value));
						} catch (Exception pe) {
							log.warn("Unable to parse last modified date: "
									+ value, pe);
						}
					} else if (key.equals(HttpHeaders.ContentLength.toString())) {
						try {
							metaData.setHeader(key, Long.parseLong(value));
						} catch (NumberFormatException nfe) {
							log.warn(
									"Unable to parse content length: " + value,
									nfe);
						}
					} else if (key.equals(HttpHeaders.ETag.toString())) {
						metaData.setHeader(key, value.replace("\"", ""));
						metaData.setHeader(HttpHeaders.ContentMD5.toString(), Converter.ETag2MD5(value));
					} else if (key.equals(HttpHeaders.Expires.toString())) {
						try {
							metaData.setHttpExpiresDate(DateUtils
									.convertStr2Date(value));
						} catch (Exception pe) {
							log.warn("Unable to parse http expiration date: "
									+ value, pe);
						}
					} else {
						metaData.setHeader(key, value);
					}
				}
			}
			result.setObjectMetadata(metaData);
		} else if (statusCode == 304) {
			result.setIfModified(false);
		} else if (statusCode == 412) {
			result.setIfPreconditionSuccess(false);
		}
	}
}
