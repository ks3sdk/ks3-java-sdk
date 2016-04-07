package com.ksyun.ks3.service;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.ksyun.ks3.dto.CreateBucketConfiguration.REGION;
import com.ksyun.ks3.dto.*;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.exception.Ks3ServiceException;
import com.ksyun.ks3.service.request.*;
import com.ksyun.ks3.service.response.GetBucketACLResponse;
import com.ksyun.ks3.service.response.Ks3WebServiceResponse;
import com.ksyun.ks3.service.request.AbortMultipartUploadRequest;
import com.ksyun.ks3.service.request.CompleteMultipartUploadRequest;
import com.ksyun.ks3.service.request.CreateBucketRequest;
import com.ksyun.ks3.service.request.DeleteBucketRequest;
import com.ksyun.ks3.service.request.DeleteObjectRequest;
import com.ksyun.ks3.service.request.GeneratePresignedUrlRequest;
import com.ksyun.ks3.service.request.GetObjectRequest;
import com.ksyun.ks3.service.request.HeadBucketRequest;
import com.ksyun.ks3.service.request.HeadObjectRequest;
import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.service.request.Ks3WebServiceRequest;
import com.ksyun.ks3.service.request.ListBucketsRequest;
import com.ksyun.ks3.service.request.ListObjectsRequest;
import com.ksyun.ks3.service.request.ListPartsRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.service.request.UploadPartRequest;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月14日 下午5:30:30
 * 
 * @description ks3客户端，用户使用时需要先配置ClientConfig{@link Ks3ClientConfig}
 *              ,然后初始化一个Ks3Client进行操作
 **/
public interface Ks3 {
	/**
	 * 设置accesskeyid accesskeysecret
	 * 
	 * @param auth
	 */
	public void setAuth(Authorization auth);
	/**
	 * 设置accesskeyid accesskeysecret
	 * 
	 * @param auth
	 */
	public Ks3 withAuth(Authorization auth);
	/**
	 * 服务地址,参考{@linkplain http://ks3.ksyun.com/doc/api/index.html} </br>
	 * 中国（杭州）:kss.ksyun.com</br>
	 * 中国（杭州）cdn:kssws.ks-cdn.com</br>
	 * 美国（圣克拉拉）:ks3-us-west-1.ksyun.com</br>
	 * 中国（北京）:ks3-cn-beijing.ksyun.com</br>
	 * 中国（香港）:ks3-cn-hk-1.ksyun.com</br>
	 * 中国（上海）:ks3-cn-shanghai.ksyun.com</br>
	 */
	public void setEndpoint(String endpoint);
	/**
	 * 设置服务地址
	 * 中国标准:kss.ksyun.com</br>
	 * 中国cdn:kssws.ks-cdn.com</br>
	 * 美国（圣克拉拉）:ks3-us-west-1.ksyun.com</br>
	 * @param endpoint
	 */
	public Ks3 withEndpoint(String endpoint);
	/**
	 * 设置是否使用pathStyle
	 * @param pathStyle
	 */
	public void setPathAccessStyle(boolean pathStyle);
	/**
	 * 设置是否使用pathStyle
	 * @param pathStyle
	 */
	public Ks3 withPathStyleAccess(boolean pathStyle);
	/**
	 * 获取配置信息
	 * @return {@link Ks3ClientConfig}
	 */
	public Ks3ClientConfig getKs3config();
	/**
	 * 设置配置信息
	 * @param ks3config
	 */
	public void setKs3config(Ks3ClientConfig ks3config);
	/**
	 * 设置配置信息
	 * @param ks3config
	 */
	public Ks3 withKs3config(Ks3ClientConfig ks3config);
	/**
	 * GET SERVICE
	 * 
	 * @return {@link Bucket}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             列出当前用户下的所有bucket
	 *             </p>
	 */
	public List<Bucket> listBuckets() throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * GET SERVICE
	 * 
	 * @param request
	 *            {@link ListBucketsRequest}
	 * @return {@link Bucket}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             列出当前用户下的所有bucket
	 *             </p>
	 */
	public List<Bucket> listBuckets(ListBucketsRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * GET Bucket Location
	 * 
	 * @param bucketName
	 * @return
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取bucket的存储地点
	 *             </p>
	 */
	public REGION getBucketLoaction(String bucketName)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * GET Bucket Location
	 * 
	 * @param request
	 *            {@link GetBucketLocationRequest}
	 * @return
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取bucket的存储地点
	 *             </p>
	 */
	public REGION getBucketLoaction(GetBucketLocationRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * GET Bucket Logging
	 * 
	 * @param bucketName
	 * @return {@link BucketLoggingStatus}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取bucket的日志配置
	 *             </p>
	 */
	public BucketLoggingStatus getBucketLogging(String bucketName)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * GET Bucket Logging
	 * 
	 * @param request
	 *            {@link GetBucketLoggingRequest}
	 * @return {@link BucketLoggingStatus}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取bucket的日志配置
	 *             </p>
	 */
	public BucketLoggingStatus getBucketLogging(GetBucketLoggingRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * PUT Bucket Logging
	 * 
	 * @param bucketName
	 * @param enable
	 *            是否开启
	 * @param targetBucket
	 *            存储日志的bucket
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             配置bucket的日志
	 *             </p>
	 */
	public void putBucketLogging(String bucketName, boolean enable,
			String targetBucket) throws Ks3ClientException, Ks3ServiceException;

	/**
	 * PUT Bucket Logging
	 * 
	 * @param bucketName
	 * @param enable
	 *            是否开启
	 * @param targetBucket
	 *            存储日志的bucket
	 * @param targetPrefix
	 *            日志文件前缀
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             配置 bucket的日志
	 *             </p>
	 */
	public void putBucketLogging(String bucketName, boolean enable,
			String targetBucket, String targetPrefix)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * PUT Bucket Logging
	 * 
	 * @param request
	 *            {@link PutBucketLoggingRequest}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             配置bucket的日志
	 *             </p>
	 */
	public void putBucketLogging(PutBucketLoggingRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * GET BUCKET acl
	 * 
	 * @param bucketName
	 *            bucket名称
	 * @return {@link AccessControlPolicy}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取bucket的acl
	 *             </p>
	 */
	public AccessControlPolicy getBucketACL(String bucketName)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * GET Bucket acl
	 * 
	 * @param request
	 *            {@link GetBucketACLRequest}
	 * @return {@link AccessControlPolicy}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取bucket的acl
	 *             </p>
	 */
	public AccessControlPolicy getBucketACL(GetBucketACLRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * GET BUCKET acl
	 * 
	 * @param bucketName
	 *            bucket名称
	 * @return {@link CannedAccessControlList}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取bucket的acl
	 *             </p>
	 */
	public CannedAccessControlList getBucketCannedACL(String bucketName)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * GET Bucket acl
	 * 
	 * @param request
	 *            {@link GetBucketACLRequest}
	 * @return {@link CannedAccessControlList}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取bucket的acl
	 *             </p>
	 */
	public CannedAccessControlList getBucketCannedACL(GetBucketACLRequest request)
			throws Ks3ClientException, Ks3ServiceException;
	/**
	 * PUT Bucket acl
	 * 
	 * @param bucketName
	 *            bucket名称
	 * @param accessControlList
	 *            {@link AccessControlList}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             编辑bucket的acl
	 *             </p>
	 */
	public void putBucketACL(String bucketName,
			AccessControlList accessControlList) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * PUT Bucket acl
	 * 
	 * @param bucketName
	 *            bucket名称
	 * @param CannedAcl
	 *            {@link CannedAccessControlList}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             编辑bucket的acl
	 *             </p>
	 */
	public void putBucketACL(String bucketName,
			CannedAccessControlList CannedAcl) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * PUT Bucket acl
	 * 
	 * @param request
	 *            {@link PutBucketACLRequest}
	 * @return
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             编辑bucket的acl
	 *             </p>
	 */
	public void putBucketACL(PutBucketACLRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * PUT OBJECT ACL
	 * 
	 * @param bucketName
	 *            bucket名称
	 * @param objectName
	 *            object名称
	 * @param accessControlList
	 *            {@link CannedAccessControlList}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             编辑object的acl
	 *             </p>
	 */
	public void putObjectACL(String bucketName, String objectName,
			CannedAccessControlList accessControlList)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * PUT OBJECT ACL
	 * 
	 * @param bucketName
	 *            bucket名称
	 * @param objectName
	 *            object名称
	 * @param accessControlList
	 *            {@link AccessControlList}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             编辑object的acl
	 *             </p>
	 */
	public void putObjectACL(String bucketName, String objectName,
			AccessControlList accessControlList) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * PUT Object acl
	 * 
	 * @param request
	 *            {@link PutObjectACLRequest}
	 * @return
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             编辑object的acl
	 *             </p>
	 */
	public void putObjectACL(PutObjectACLRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * Get Object acl
	 * 
	 * @param bucketName
	 *            bucket名称
	 * @param ObjectName
	 *            object名称
	 * @return {@link AccessControlPolicy}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取object的acl
	 *             </p>
	 */
	public AccessControlPolicy getObjectACL(String bucketName, String ObjectName)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * Get Object acl
	 * 
	 * @param request
	 *            {@link GetObjectACLRequest}
	 * @return {@link AccessControlPolicy}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取object的acl
	 *             </p>
	 */
	public AccessControlPolicy getObjectACL(GetObjectACLRequest request)
			throws Ks3ClientException, Ks3ServiceException;
	
	/**
	 * Get Object acl
	 * 
	 * @param bucketName
	 *            bucket名称
	 * @param ObjectName
	 *            object名称
	 * @return {@link CannedAccessControlList}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取object的acl
	 *             </p>
	 */
	public CannedAccessControlList getObjectCannedACL(String bucketName, String ObjectName)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * Get Object acl
	 * 
	 * @param request
	 *            {@link GetObjectACLRequest}
	 * @return {@link CannedAccessControlList}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取object的acl
	 *             </p>
	 */
	public CannedAccessControlList getObjectCannedACL(GetObjectACLRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * HEAD BUECKET
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @return statue code and headers{@link HeadBucketResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             返回headers
	 *             </p>
	 */
	public HeadBucketResult headBucket(String bucketname)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * HEAD BUCKET
	 * 
	 * @param request
	 *            {@link HeadBucketRequest}
	 * @return statue code and headers{@link HeadBucketResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             返回headers
	 *             </p>
	 */
	public HeadBucketResult headBucket(HeadBucketRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @return 如果bucket存在则为true
	 * @throws Ks3ClientException
	 *             <p>
	 *             判断bucket是否存在,bucket存在但不属于当前用户也会返回true
	 *             </p>
	 */
	public boolean bucketExists(String bucketname) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * PUT BUCKET
	 * 
	 * @param bucketname
	 *            bucket名称
	 *            <p>
	 *            Bucket是存放Object的容器，所有的Object都必须存放在特定的Bucket中。
	 *            ，每个Bucket中可以存放无限多个Object。Bucket不能嵌套，每个Bucket中只能存放Object，
	 *            不能再存放Bucket ，Bucket下的Object是一个平级的结构。
	 *            <p>
	 *            <p>
	 *            Bucket的名称全局唯一且命名规则与DNS命名规则相同：
	 *            <p>
	 *            <ul>
	 *            <li>长度3-63，</li>
	 *            <li>不包含大写字母，不包含[‘ ’,\t,\r,\n]，不包含连续的’.’</li>
	 *            <li>，’.’和’-’在bucket名称中不能相连，</li>
	 *            <li>仅可包含. - 数字 小写字母，</li>
	 *            <li>不以’.’或’-’结尾</li>
	 *            </ul>
	 * @return {@link Bucket}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             创建bucket，权限默认是私有的，存储地点为杭州
	 *             </p>
	 */
	public Bucket createBucket(String bucketname) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * PUT BUCKET
	 * 
	 * @param request
	 *            {@link CreateBucketRequest}
	 * @return {@link Bucket}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             创建bucket，权限默认是私有的，存储地点为杭州
	 *             </p>
	 */
	public Bucket createBucket(CreateBucketRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * 清空bucket中的所有内容，请谨慎使用。
	 * 
	 * @param bucketName
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 */
	public void clearBucket(String bucketName) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * 新建文件夹
	 * 
	 * @param bucketName
	 * @param dir
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 */
	public void makeDir(String bucketName, String dir)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * 删除文件夹及下面的所有内容
	 * 
	 * @param bucketName
	 * @param dir
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 */
	public void removeDir(String bucketName, String dir)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * DELETE BUCKET
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             删除bucket，bucket中内容为空时可以删除成功
	 *             </p>
	 *             <p>注意这个操作是不能回退的</p>
	 */
	public void deleteBucket(String bucketname) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * DELETE BUCKET
	 * 
	 * @param request
	 *            {@link DeleteBucketRequest}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             删除bucket，bucket中内容为空时可以删除成功
	 *             </p>
	 *             <p>注意这个操作是不能回退的</p>
	 */
	public void deleteBucket(DeleteBucketRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * GET BUCKET(LIST OBJECTS)
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @return {@link ObjectListing}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             列出bucket下满足条件的object
	 *             </p>
	 *             <p>delimiter使用默认的/</p>
	 *             <p>返回的最大数(max-keys)使用默认的1000</p>
	 */
	public ObjectListing listObjects(String bucketname)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * GET BUCKET(LIST OBJECTS)
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @param prefix
	 *            前缀
	 * @return {@link ObjectListing}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             列出bucket下满足条件的object
	 *             </p>
	 *             <p>delimiter使用默认的/</p>
	 *             <p>返回的最大数(max-keys)使用默认的1000</p>
	 */
	public ObjectListing listObjects(String bucketname, String prefix)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * GET BUCKET(LIST OBJECTS)
	 * 
	 * @param request
	 *            {@link ListObjectsRequest}
	 * @return {@link ObjectListing}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             列出bucket下满足条件的object
	 *             </p>
	 */
	public ObjectListing listObjects(ListObjectsRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * DELETE OBJECT
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @param key
	 *            object的key(即名称)
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             删除指定的object
	 *             </p>
	 */
	public void deleteObject(String bucketname, String key)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * DELETE OBJECT
	 * 
	 * @param request
	 *            {@link DeleteObjectRequest}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             删除指定的object
	 *             </p>
	 */
	public void deleteObject(DeleteObjectRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * DELETE Multiple Objects
	 * 
	 * @param request
	 *            {@link DeleteMultipleObjectsRequest}
	 * @return {@link DeleteMultipleObjectsResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             删除若干的objects
	 *             </p>
	 */
	public DeleteMultipleObjectsResult deleteObjects(
			DeleteMultipleObjectsRequest request) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * DELETE Multiple Objects
	 * 
	 * @param keys
	 *            要删除的keys
	 * @return {@link DeleteMultipleObjectsResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             删除若干的objects
	 *             </p>
	 */
	public DeleteMultipleObjectsResult deleteObjects(List<String> keys,
			String bucketName) throws Ks3ClientException, Ks3ServiceException;

	/**
	 * DELETE Multiple Objects
	 * 
	 * @param keys
	 *            要删除的keys
	 * @return {@link DeleteMultipleObjectsResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             删除若干的objects
	 *             </p>
	 */
	public DeleteMultipleObjectsResult deleteObjects(String[] keys,
			String bucketName) throws Ks3ClientException, Ks3ServiceException;

	/**
	 * GET OBJECT
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @param key
	 *            object的key(即名称)
	 * @return {@link GetObjectResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取指定的object
	 *             </p>
	 */
	public GetObjectResult getObject(String bucketname, String key)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * GET OBJECT
	 * 
	 * @param request
	 *            {@link GetObjectRequest}
	 * @return {@link GetObjectResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取指定的object
	 *             </p>
	 */
	public GetObjectResult getObject(GetObjectRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * 生成object的外链地址
	 * 
	 * @param bucket
	 * @param key
	 * @param expiration
	 *            外链地址过期时间，单位秒
	 * @return
	 * @throws Ks3ClientException
	 */
	public String generatePresignedUrl(String bucket, String key, int expiration)
			throws Ks3ClientException;
	/**
	 * 生成object的外链地址
	 * 
	 * @param bucket
	 * @param key
	 * @param expiration 外链地址过期时间，单位秒
	 * @param overrides 修改返回的headers
	 * @return
	 * @throws Ks3ClientException
	 */
	public String generatePresignedUrl(String bucket, String key, int expiration,ResponseHeaderOverrides overrides)
			throws Ks3ClientException;
	/**
	 * 生成外链
	 * 
	 * @param request {@link GeneratePresignedUrlRequest}
	 * @return
	 * @throws Ks3ClientException
	 */
	public String generatePresignedUrl(GeneratePresignedUrlRequest request) throws Ks3ClientException;
	/**
	 * PUT OBJECT
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @param objectkey
	 *            object的key(即名称,编码后的长度不得超过1024个字节。)
	 * @param file
	 *            要上传的文件
	 * @return {@link PutObjectResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             上传object
	 *             </p>
	 */
	public PutObjectResult putObject(String bucketname, String objectkey,
			File file) throws Ks3ClientException, Ks3ServiceException;

	/**
	 * PUT OBJECT
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @param objectkey
	 *            object的key(即名称,编码后的长度不得超过1024个字节。)
	 * @param inputstream
	 *            数据流
	 * @param objectmeta
	 *            object元数据{@link ObjectMetadata}
	 * @return {@link PutObjectResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             使用时请尽量在metadata中提供content
	 *             -length,否则有可能导致jvm内存溢出。可以在metadata中指定contentMD5
	 *             </p>
	 *             <p>
	 *             上传object
	 *             </p>
	 */
	public PutObjectResult putObject(String bucketname, String objectkey,
			InputStream inputstream, ObjectMetadata objectmeta)
			throws Ks3ClientException, Ks3ServiceException;
	/**
	 * PUT Object
	 * @param bucketname bucket名称
	 * @param objectkey
	 * @param content 要上传的内容
	 * @return
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 * <p>
	 * 将一个字符串作为内容上传
	 * </p>
	 */
	public PutObjectResult putObject(String bucketname,String objectkey,String content)
	throws Ks3ClientException, Ks3ServiceException;

	/**
	 * PUT OBJECT
	 * 
	 * @param request
	 *            {@link PutObjectRequest}
	 * @return {@link PutObjectResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             上传object
	 *             </p>
	 */
	public PutObjectResult putObject(PutObjectRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * Copy Object
	 * 
	 * @param destinationBucket
	 *            目标bucket
	 * @param destinationObject
	 *            目标object key
	 * @param sourceBucket
	 *            数据源bucket
	 * @param sourceKey
	 *            数据源object key
	 * @return {@link CopyResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             将指定的object复制到目标地点
	 *             </p>
	 */
	public CopyResult copyObject(String destinationBucket,
			String destinationObject, String sourceBucket, String sourceKey)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * Copy Object
	 * 
	 * @param destinationBucket
	 *            目标bucket
	 * @param destinationObject
	 *            目标object key
	 * @param sourceBucket
	 *            数据源bucket
	 * @param sourceKey
	 *            数据源object key
	 * @param cannedAcl
	 *            {@link CannedAccessControlList}
	 * @return {@link CopyResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             将指定的object复制到目标地点
	 *             </p>
	 */
	public CopyResult copyObject(String destinationBucket,
			String destinationObject, String sourceBucket, String sourceKey,
			CannedAccessControlList cannedAcl) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * Copy Object
	 * 
	 * @param destinationBucket
	 *            目标bucket
	 * @param destinationObject
	 *            目标object key
	 * @param sourceBucket
	 *            数据源bucket
	 * @param sourceKey
	 *            数据源object key
	 * @param accessControlList
	 *            {@link AccessControlList}
	 * @return {@link CopyResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             将指定的object复制到目标地点
	 *             </p>
	 */
	public CopyResult copyObject(String destinationBucket,
			String destinationObject, String sourceBucket, String sourceKey,
			AccessControlList accessControlList) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * Copy Object
	 * 
	 * @param request
	 *            {@link CopyObjectRequest}
	 * @return {@link CopyResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>将指定的object复制到目标地点，将复制源object的元数据、acl等信息</p>
	 */
	public CopyResult copyObject(CopyObjectRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * HEAD OBJECT
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @param objectkey
	 *            object的key(即object名称)
	 * @return {@link HeadObjectResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取object的元数据、etag、上次修改时间
	 *             </p>
	 */
	public HeadObjectResult headObject(String bucketname, String objectkey)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * HEAD OBJECT
	 * 
	 * @param request
	 *            {@link HeadObjectRequest}
	 * @return {@link HeadObjectResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             获取object的元数据、etag、上次修改时间
	 *             </p>
	 */
	public HeadObjectResult headObject(HeadObjectRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * 判断object是否存在
	 * @param bucket
	 * @param key
	 * @return boolean
	 */
	public boolean objectExists(String bucket,String key);
	/**
	 * Initiate Multipart Upload
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @param objectkey
	 *            object的key(即object名称)
	 * @return {@link InitiateMultipartUploadResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             初始化分块上传
	 *             </p>
	 */
	public InitiateMultipartUploadResult initiateMultipartUpload(
			String bucketname, String objectkey) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * Initiate Multipart Upload
	 * 
	 * @param request
	 *            {@link InitiateMultipartUploadRequest}
	 * @return {@link InitiateMultipartUploadResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             初始化分块上传
	 *             </p>
	 */
	public InitiateMultipartUploadResult initiateMultipartUpload(
			InitiateMultipartUploadRequest request) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * Upload Part
	 * 
	 * @param request
	 *            {@link UploadPartRequest}
	 * @return {@link PartETag}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             分块上传文件
	 *             </p>
	 */
	public PartETag uploadPart(UploadPartRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * Upload Part Copy
	 * 
	 * @param request
	 *            {@link CopyPartRequest}
	 * @return {@link CopyResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             分块上传时使用copy
	 *             </p>
	 */
	public CopyResult copyPart(CopyPartRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * Complete Multipart Upload
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @param objectkey
	 *            object的key(即名称)
	 * @param uploadId
	 *            通过初始化分块上传获取到的uploadId
	 * @param partETags
	 *            <p>
	 *            public PartETag uploadPart(UploadPartRequest request)throws
	 *            Ks3ClientException, Ks3ServiceException;
	 *            <p>
	 *            获取到的结果{@link PartETag}
	 * @return {@link CompleteMultipartUploadResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             完成分块上传，使ks3服务器将之前上传的小块合并成一个object
	 *             </p>
	 */
	public CompleteMultipartUploadResult completeMultipartUpload(
			String bucketname, String objectkey, String uploadId,
			List<PartETag> partETags) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * Complete Multipart Upload
	 * 
	 * @param result
	 *            {@link ListPartsResult}ListParts操作的返回值
	 * @return{@link CompleteMultipartUploadResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             完成分块上传，使ks3服务器将之前上传的小块合并成一个object
	 *             </p>
	 */
	public CompleteMultipartUploadResult completeMultipartUpload(
			ListPartsResult result) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * Complete Multipart Upload
	 * 
	 * @param request
	 *            {@link CompleteMultipartUploadRequest}
	 * @return {@link CompleteMultipartUploadResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             完成分块上传，使ks3服务器将之前上传的小块合并成一个object
	 *             </p>
	 */
	public CompleteMultipartUploadResult completeMultipartUpload(
			CompleteMultipartUploadRequest request) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * Abort Multipart Upload
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @param objectkey
	 *            object的key(即object的名称)
	 * @param uploadId
	 *            通过初始化分块上传获取到的uploadId
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             中止分块上传
	 *             </p>
	 */
	public void abortMultipartUpload(String bucketname, String objectkey,
			String uploadId) throws Ks3ClientException, Ks3ServiceException;

	/**
	 * Abort Multipart Upload
	 * 
	 * @param request
	 *            {@link AbortMultipartUploadRequest}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             中止分块上传
	 *             </p>
	 */
	public void abortMultipartUpload(AbortMultipartUploadRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * List Parts
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @param objectkey
	 *            object的key(即object的名称)
	 * @param uploadId
	 *            通过初始化分块上传获取到的uploadId
	 * @return {@link ListPartsResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             列出该uploadid下已经上传成功的块
	 *             </p>
	 */
	public ListPartsResult listParts(String bucketname, String objectkey,
			String uploadId) throws Ks3ClientException, Ks3ServiceException;

	/**
	 * List Parts
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @param objectkey
	 *            object的key(即object的名称)
	 * @param uploadId
	 *            通过初始化分块上传获取到的uploadId
	 * @param maxParts
	 *            列出的最大结果数
	 * @return {@link ListPartsResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             列出该uploadid下已经上传成功的块
	 *             </p>
	 */
	public ListPartsResult listParts(String bucketname, String objectkey,
			String uploadId, int maxParts) throws Ks3ClientException,
			Ks3ServiceException;

	/**
	 * List Parts
	 * 
	 * @param bucketname
	 *            bucket名称
	 * @param objectkey
	 *            object的key(即object的名称)
	 * @param uploadId
	 *            通过初始化分块上传获取到的uploadId
	 * @param maxParts
	 *            列出的最大结果数
	 * @param partNumberMarker
	 *            partnumber游标，即从第partNumberMarker开始罗列
	 * @return {@link ListPartsResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             列出该uploadid下已经上传成功的块
	 *             </p>
	 */
	public ListPartsResult listParts(String bucketname, String objectkey,
			String uploadId, int maxParts, int partNumberMarker)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * List Parts
	 * 
	 * @param request
	 *            {@link ListPartsRequest}
	 * @return {@link ListPartsResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 *             <p>
	 *             列出该uploadid下已经上传成功的块
	 *             </p>
	 */
	public ListPartsResult listParts(ListPartsRequest request)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * List Multipart Uploads
	 * 
	 * @param bucketName
	 * @return {@link ListMultipartUploadsResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ClientException
	 *             <p>
	 *             列出bucket下分块上传未abort或complete的块
	 *             </p>
	 */
	public ListMultipartUploadsResult listMultipartUploads(String bucketName)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * List Multipart Uploads
	 * 
	 * @param bucketName
	 * @param prefix
	 *            object key前缀
	 * @return {@link ListMultipartUploadsResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ClientException
	 *             <p>
	 *             列出bucket下分块上传未abort或complete的块
	 *             </p>
	 */
	public ListMultipartUploadsResult listMultipartUploads(String bucketName,
			String prefix) throws Ks3ClientException, Ks3ServiceException;

	/**
	 * List Multipart Uploads
	 * 
	 * @param bucketName
	 * @param prefix
	 *            object key前缀
	 * @param keyMarker
	 * @param uploadIdMarker
	 *            <p>
	 *            keyMarker为空，uploadIdMarker不为空
	 *            <P>
	 *            <p>
	 *            无意义
	 *            </p>
	 *            <p>
	 *            keyMarker不为空，uploadIdMarker不为空
	 *            <P>
	 *            <p>
	 *            列出分块上传object key为keyMarker，且upload id 大于uploadIdMarker的块
	 *            </p>
	 *            <p>
	 *            keyMarker不为空，uploadIdMarker为空
	 *            <P>
	 *            <p>
	 *            列出分块上传object key大于keyMarker的块
	 *            </p>
	 * @return {@link ListMultipartUploadsResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ClientException
	 *             <p>
	 *             列出bucket下分块上传未abort或complete的块
	 *             </p>
	 */
	public ListMultipartUploadsResult listMultipartUploads(String bucketName,
			String prefix, String keyMarker, String uploadIdMarker)
			throws Ks3ClientException, Ks3ServiceException;

	/**
	 * List Multipart Uploads
	 * 
	 * @param request
	 *            {@link ListMultipartUploadsRequest}
	 * @return {@link ListMultipartUploadsResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ClientException
	 *             <p>
	 *             列出bucket下分块上传未abort或complete的块
	 *             </p>
	 */
	public ListMultipartUploadsResult listMultipartUploads(
			ListMultipartUploadsRequest request) throws Ks3ClientException,
			Ks3ServiceException;
	/**
	 * PUT Bucket cors
	 * @param request {@link PutBucketCorsRequest}
	 * @throws Ks3ClientException
	 * @throws Ks3ClientException
	 * <p>配置bucket的跨域资源共享</p>
	 */
	public void putBucketCors(PutBucketCorsRequest request) throws Ks3ClientException,
	Ks3ServiceException;
	/**
	 * GET Bucket cors
	 * @param bucketname
	 * @return
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 * <p>获取bucket的跨域资源共享配置</p>
	 */
	public BucketCorsConfiguration getBucketCors(String bucketname) throws Ks3ClientException,Ks3ServiceException;
	/**
	 * GET Bucket cors
	 * @param request {@link GetBucketCorsRequest}
	 * @return
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 * <p>获取bucket的跨域资源共享配置</p>
	 */
	public BucketCorsConfiguration getBucketCors(GetBucketCorsRequest request) throws Ks3ClientException,Ks3ServiceException;
	/**
	 * DELETE Bucket cors
	 * @param bucketname
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 * <p>删除bucket的跨域资源共享配置</p>
	 */
	public void deleteBucketCors(String bucketname) throws Ks3ClientException,Ks3ServiceException;
	/**
	 * DELETE Bucket cors
	 * @param request {@link DeleteBucketCorsRequest}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 * <p>删除bucket的跨域资源共享配置</p>
	 */
	public void deleteBucketCors(DeleteBucketCorsRequest request) throws Ks3ClientException,Ks3ServiceException;
	/**
	 * 
	 * @param request {@code Class<? extends Ks3WebServiceRequest> }
	 * @param clazz {@code Class<? extends Ks3WebServiceResponse> }
	 * @return
	 * @throws Ks3ClientException
	 * @throws Ks3ClientException
	 * <p>对于自定义的request和response可以通过这个方法执行</p>
	 */
	public <X extends Ks3WebServiceResponse<Y>, Y> Y execute(
			Ks3WebServiceRequest request, Class<X> clazz)
			throws Ks3ClientException, Ks3ServiceException;
	/**
	 * 
	 * @param policy
	 * @return {@link PostObjectFormFields}
	 * @throws Ks3ClientException
	 * <p>通过自定义policy规则获取post object时表单中的Signature、KSSAccessKeyId、policy三项的值</p>
	 */
	public PostObjectFormFields postObject(PostPolicy policy) throws Ks3ClientException;
	/**
	 * 
	 * @param bucket 目标bucket
	 * @param filename 要上传的文件名称,当postFormData中没有使用${filename}时可以不提供
	 * @param postFormData 可以确定值得表单项
	 * @param unknowValueFormFiled 无法确定值得表单项
	 * @return {@link PostObjectFormFields}
	 * @throws Ks3ClientException
	 * <p>通过bucket和表单中的除 Signature、KSSAccessKeyId、policy外的所有表单项 获取post object时表单中的Signature、KSSAccessKeyId、policy三项的值</p>
	 * <p>由此方法生成的签名只能适用于本次请求，matchingType全部使用eq或通配</p>
	 */
	public PostObjectFormFields postObject(String bucket,String filename,Map<String,String> postFormData,List<String> unknowValueFormFiled) throws Ks3ClientException;
	/**
	 * 
	 * @param bucketName 要处理的数据所在的bucket
	 * @param objectKey 要处理的数据的key
	 * @param adps 一系列的操作指令{@link Adp}
	 * @return PutAdpResult {@link PutAdpResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 * 
	 * <p>添加数据处理任务</p>
	 */
	public PutAdpResult putAdpTask(String bucketName,String objectKey,List<Adp> adps) throws Ks3ClientException, Ks3ServiceException;;
	/**
	 * 
	 * @param bucketName 要处理的数据所在的bucket
	 * @param objectKey 要处理的数据的key
	 * @param adps 一系列的操作指令{@link Adp}
	 * @param notifyURL 处理完成后KS3将调用该url,以通知用户
	 * @return PutAdpResult {@link PutAdpResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 * 
	 * <p>添加数据处理任务</p>
	 */
	public PutAdpResult putAdpTask(String bucketName,String objectKey,List<Adp> adps,String notifyURL) throws Ks3ClientException, Ks3ServiceException;
	/**
	 * 
	 * @param request {@link PutAdpRequest}
	 * @return PutAdpResult {@link PutAdpResult}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 * 
	 * <p>添加数据处理任务</p>
	 */
	public PutAdpResult putAdpTask(PutAdpRequest request) throws Ks3ClientException, Ks3ServiceException;
	/**
	 * 
	 * @param taskid 由putpfop，postobject，putobject，complete_mutipart_upload返回的taskid
	 * @return {@link AdpTask}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 * <p>查询数据处理任务的状态</p>
	 */
	public AdpTask getAdpTask(String taskid) throws Ks3ClientException, Ks3ServiceException;
	/**
	 * @param request {@link GetAdpRequest}
	 * @return {@link AdpTask}
	 * @throws Ks3ClientException
	 * @throws Ks3ServiceException
	 * <p>查询数据处理任务的状态</p>
	 */
	public AdpTask getAdpTask(GetAdpRequest request) throws Ks3ClientException, Ks3ServiceException;
}
