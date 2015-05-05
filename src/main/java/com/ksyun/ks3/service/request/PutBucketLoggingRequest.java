package com.ksyun.ks3.service.request;

import java.io.ByteArrayInputStream;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;
import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNullInCondition;

import java.util.HashSet;

import com.ksyun.ks3.dto.BucketLoggingStatus;
import com.ksyun.ks3.dto.Grant;
import com.ksyun.ks3.dto.GranteeEmail;
import com.ksyun.ks3.dto.GranteeId;
import com.ksyun.ks3.dto.GranteeUri;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.StringUtils;
import com.ksyun.ks3.utils.XmlWriter;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月16日
 * 
 * @description 配置bucket日志的请求
 *              <p>
 *              public PutBucketLoggingRequest(String bucketName, boolean
 *              enable,String targetBucket)
 *              </p>
 *              <p>
 *              public PutBucketLoggingRequest(String bucketName, boolean
 *              enable,String targetBucket, String targetPrefix)
 *              </p>
 **/
public class PutBucketLoggingRequest extends Ks3WebServiceRequest {
	private String bucket;
	/**
	 * 
	 * @param bucketName
	 * @param enable
	 *            是否开启bucket的日志功能
	 * @param targetBucket
	 *            日志存放地点
	 */
	public PutBucketLoggingRequest(String bucketName, boolean enable,
			String targetBucket) {
		this(bucketName);
		this.bucketLoggingStatus.setEnable(enable);
		this.bucketLoggingStatus.setTargetBucket(targetBucket);
	}

	/**
	 * 
	 * @param bucketName
	 * @param enable
	 *            是否开启bucket的日志功能
	 * @param targetBucket
	 *            日志存放地点
	 * @param targetPrefix
	 *            日志文件前缀
	 */
	public PutBucketLoggingRequest(String bucketName, boolean enable,
			String targetBucket, String targetPrefix) {
		this(bucketName);
		this.bucketLoggingStatus.setEnable(enable);
		this.bucketLoggingStatus.setTargetBucket(targetBucket);
		this.bucketLoggingStatus.setTargetPrefix(targetPrefix);
	}

	/**
	 * 
	 * @param bucketName
	 * @param bucketLoggingStatus
	 *            {@link bucketLoggingStatus}
	 */
	public PutBucketLoggingRequest(String bucketName,
			BucketLoggingStatus bucketLoggingStatus) {
		this(bucketName);
		this.bucketLoggingStatus = bucketLoggingStatus;
	}

	public PutBucketLoggingRequest(String bucketName) {
		this.bucket = bucketName;
	}

	private BucketLoggingStatus bucketLoggingStatus = new BucketLoggingStatus();

	@Override
	public void validateParams() throws IllegalArgumentException {
		if (StringUtils.isBlank(this.bucket))
			throw notNull("bucketname");
		if (this.bucketLoggingStatus == null)
			throw notNull("bucketLoggingStatus");
		if (this.bucketLoggingStatus.isEnable()
				&& this.bucketLoggingStatus.getTargetBucket() == null)
			throw notNullInCondition("targetBucket", "enable 为 true");
	}

	public BucketLoggingStatus getBucketLoggingStatus() {
		return bucketLoggingStatus;
	}

	public void setBucketLoggingStatus(BucketLoggingStatus bucketLoggingStatus) {
		this.bucketLoggingStatus = bucketLoggingStatus;
	}

	/** 设置是否开启 */
	public void setEnable(boolean enable) {
		if (this.bucketLoggingStatus == null)
			this.bucketLoggingStatus = new BucketLoggingStatus();
		this.bucketLoggingStatus.setEnable(enable);
	}

	/** 设置日志文件存放位置 */
	public void setTargetBucket(String bucket) {
		if (this.bucketLoggingStatus == null)
			this.bucketLoggingStatus = new BucketLoggingStatus();
		this.bucketLoggingStatus.setTargetBucket(bucket);
	}

	/** 设置日志文件前缀 */
	public void setTargetPrefix(String prefix) {
		if (this.bucketLoggingStatus == null)
			this.bucketLoggingStatus = new BucketLoggingStatus();
		this.bucketLoggingStatus.setTargetPrefix(prefix);
	}

	@Override
	public void buildRequest(Request request) {
		request.setBucket(bucket);
		request.setMethod(HttpMethod.PUT);
		request.addQueryParam("logging", null);

		XmlWriter writer = new XmlWriter();
		writer.startWithNs("BucketLoggingStatus");
		if (this.bucketLoggingStatus.isEnable()) {
			writer.start("LoggingEnabled").start("TargetBucket")
					.value(this.bucketLoggingStatus.getTargetBucket()).end()
					.start("TargetPrefix")
					.value(this.bucketLoggingStatus.getTargetPrefix()).end();
			if (this.bucketLoggingStatus.getTargetGrants() != null
					&& this.bucketLoggingStatus.getTargetGrants().size() != 0) {
				HashSet<Grant> grants = this.bucketLoggingStatus
						.getTargetGrants();
				for (Grant grant : grants) {
					writer.start("Grant");

					if (grant.getGrantee() instanceof GranteeEmail) {
						writer.start("Grantee", new String[] { "xmlns:xsi",
								"xsi" }, new String[] {
								"http://www.w3.org/2001/XMLSchema-instance",
								"AmazonCustomerByEmail" });
						writer.start("EmailAddress");
					} else if (grant.getGrantee() instanceof GranteeUri) {
						writer.start("Grantee", new String[] { "xmlns:xsi",
								"xsi" }, new String[] {
								"http://www.w3.org/2001/XMLSchema-instance",
								"Group" });
						writer.start("URI");
					} else if (grant.getGrantee() instanceof GranteeId) {
						writer.start("Grantee", new String[] { "xmlns:xsi",
								"xsi" }, new String[] {
								"http://www.w3.org/2001/XMLSchema-instance",
								"CanonicalUser" });
						writer.start("ID");
					}
					writer.value(grant.getGrantee().getIdentifier()).end()
							.end().start("Permission")
							.value(grant.getPermission().toString()).end();

					writer.end();
				}
			}
			writer.end().end();
		} else {
			writer.end();
		}
		request.setContent(new ByteArrayInputStream(writer.toString()
				.getBytes()));
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
}
