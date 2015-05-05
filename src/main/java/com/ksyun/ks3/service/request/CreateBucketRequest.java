package com.ksyun.ks3.service.request;

import java.io.ByteArrayInputStream;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;
import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notCorrect;

import com.ksyun.ks3.dto.AccessControlList;
import com.ksyun.ks3.dto.CannedAccessControlList;
import com.ksyun.ks3.dto.CreateBucketConfiguration;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.HttpUtils;
import com.ksyun.ks3.utils.StringUtils;
import com.ksyun.ks3.utils.XmlWriter;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月15日 下午4:43:12
 * 
 * @description 
 * <p>构造函数</p>
 * <p>public CreateBucketRequest(String bucketName)</p>
 * <p>public CreateBucketRequest(String bucketName,{@link CannedAccessControlList} cannedAcl)</p>
 * <p>public CreateBucketRequest(String bucketName,{@link AccessControlList} acl)</p>
 * <p>public CreateBucketRequest(String bucketName,{@link CreateBucketConfiguration.REGION} region)</p>
 * <p>新建bucket时的请求信息</p>
 *              <p>
 *              Bucket是存放Object的容器，所有的Object都必须存放在特定的Bucket中。
 *              ，每个Bucket中可以存放无限多个Object。Bucket不能嵌套，每个Bucket中只能存放Object，
 *              不能再存放Bucket ，Bucket下的Object是一个平级的结构。
 *              <p>
 *              <p>
 *              Bucket的名称全局唯一且命名规则与DNS命名规则相同：
 *              <p>
 *              <ul>
 *              <li>长度3-63，</li>
 *              <li>不包含大写字母，不包含[‘ ’,\t,\r,\n]，不包含连续的’.’</li>
 *              <li>，’.’和’-’在bucket名称中不能相连，</li>
 *              <li>仅可包含. - 数字 小写字母，</li>
 *              <li>不以’.’或’-’结尾</li>
 *              </ul>
 **/
public class CreateBucketRequest extends Ks3WebServiceRequest {
	private String bucket;
	/**
	 * {@link CannedAccessControlList}设置新建的bucket的acl
	 */
	private CannedAccessControlList cannedAcl;
	/**
	 * 设置新建的bucket的acl
	 */
	private AccessControlList acl = new AccessControlList();
	/**
	 * Bucket存储地点配置
	 */
	private CreateBucketConfiguration config = null;

	public CreateBucketRequest(String bucketName) {
		this.bucket = bucketName;
	}

	public CreateBucketRequest(String bucketName,
			CannedAccessControlList cannedAcl) {
		this(bucketName);
		this.setCannedAcl(cannedAcl);
	}

	public CreateBucketRequest(String bucketName, AccessControlList acl) {
		this(bucketName);
		this.setAcl(acl);
	}

	public CreateBucketRequest(String bucketName,
			CreateBucketConfiguration.REGION region) {
		this(bucketName);
		config = new CreateBucketConfiguration(region);
	}

	public CannedAccessControlList getCannedAcl() {
		return cannedAcl;
	}

	public void setCannedAcl(CannedAccessControlList cannedAcl) {
		this.cannedAcl = cannedAcl;
	}

	public AccessControlList getAcl() {
		return acl;
	}

	public void setAcl(AccessControlList acl) {
		this.acl = acl;
	}

	@Override
	public void validateParams() throws IllegalArgumentException {
		if (StringUtils.validateBucketName(this.bucket) == null)
			throw notCorrect("bucketname",this.bucket,"请参考KS3 API文档");
	}

	public CreateBucketConfiguration getConfig() {
		return config;
	}
	/**
	 * 
	 * @param config {@link CreateBucketConfiguration}
	 * 设置bucket存储地点
	 */
	public void setConfig(CreateBucketConfiguration config) {
		this.config = config;
	}

	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.PUT);
		request.setBucket(bucket);
		if (this.config != null && this.config.getLocation() != null) {
			XmlWriter writer = new XmlWriter();
			writer.startWithNs("CreateBucketConfiguration")
					.start("LocationConstraint")
					.value(config.getLocation().toString()).end().end();
			request.setContent(new ByteArrayInputStream(writer.toString()
					.getBytes()));
		}
		if (this.cannedAcl != null) {
			request.addHeader(HttpHeaders.CannedAcl.toString(),
					cannedAcl.toString());
		}
		if (this.acl != null) {
			request.getHeaders().putAll(HttpUtils.convertAcl2Headers(acl));
		}
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	
}
