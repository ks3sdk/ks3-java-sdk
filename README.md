# KS3 SDK For Java使用指南 
---
## 注意
文档中的示例代码仅供参考之用，具体使用的时候请参考KS3 API文档，根据自己的实际情况调节参数。  
lib目录下为该项目所依赖的所有jar包，以及将sdk打好的jar包  

[TOC]

## 1 概述
此SDK适用于Java 5及以上版本。基于KS3 API 构建。

## 2 环境准备
配置Java 5 以上开发环境  
下载KS3 SDK For Java [https://github.com/ks3sdk/ks3-java-sdk](https://github.com/ks3sdk/ks3-java-sdk)  
添加Maven依赖

    <dependency>
        <groupId>com.ksyun</groupId>
        <artifactId>ks3-kss-java-sdk</artifactId>
        <version>0.8.11</version>
    </dependency>
    
或者直接引用lib目录下的所有jar包
## 3 初始化
### 3.1 配置参数
用户可以跳过参数配置，一般情况下默认配置可以满足大部分的需求.    

	Ks3ClientConfig config = new Ks3ClientConfig();
	/**
	 * 设置服务地址
	 * 杭州:kss.ksyun.com
     * 北京:ks3-cn-beijing.ksyun.com
     * 上海:ks3-cn-shanghai.ksyun.com
     * 香港:ks3-cn-hk-1.ksyun.com
     * 俄罗斯:ks3-rus.ksyun.com
     * 新加坡:ks3-sgp.ksyun.com
     * 广州：ks3-cn-guangzhou.ksyun.com     
	*/
	config.setEndpoint("ks3-cn-beijing.ksyun.com");//此处以北京为例
	/**     
     *true：表示以自定义域名访问    
     *false：表示以KS3的外网域名或内网域名访问，默认为false    
    */   
    config.setDomainMode(false);    
    config.setProtocol(PROTOCOL.http);    
    /**    
     *true表示以   endpoint/{bucket}/{key}的方式访问    
     *false表示以  {bucket}.endpoint/{key}的方式访问    
     *如果domainMode设置为true，pathStyleAccess可忽略设置    
    */   
    config.setPathStyleAccess(false);     
    HttpClientConfig hconfig = new HttpClientConfig();
	//在HttpClientConfig中可以设置httpclient的相关属性，比如代理，超时，重试等。
    config.setHttpClientConfig(hconfig);    
    Ks3 client = new Ks3Client("<您的AccessKeyID>","<您的AccessKeySecret>",config);    
    /* 或者：client.setKs3config(config); */
    
    
### 3.2 配置日志
该SDK使用commons-logging

使用log4j的示例：  
1、引用log4j相关jar包
	
		<dependency>
			<groupId>log4j</groupId>
			<artifactId>log4j</artifactId>
			<version>1.2.16</version>
		</dependency>
		
2、新建log4j.properties(如下为示例配置)

		log4j.logger.com.ksyun.ks3=DEBUG,stdout
		log4j.logger.org.apache.http=DEBUG,stdout
		log4j.logger.org.apache.http.wire=ERROR,stdout

		log4j.addivity.org.apache=true
		log4j.appender.stdout=org.apache.log4j.ConsoleAppender
		log4j.appender.stdout.Target=System.out
		log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
		log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss SSS} %-5p [%t]%C{1}.%M(%L) - %m%n
		
使用logback示例  
1、引用SDK的时候排除commons-logging，引用logback相关包（包括但不止jcl-over-slf4j）

		<dependency>
			<groupId>com.ksyun</groupId>
			<artifactId>ks3-kss-java-sdk</artifactId>
			<version>0.6.0</version>
			<exclusions>
       	 		<exclusion>
          			<artifactId>commons-logging</artifactId>
         			 <groupId>commons-logging</groupId>
        		</exclusion>
      		</exclusions>
		</dependency>
		<dependency>
      		<groupId>org.slf4j</groupId>
     		<artifactId>jcl-over-slf4j</artifactId>
     		<version>1.7.7</version>
   		</dependency>
   		
2、配置logback  

### 3.3 获取秘钥
1、开通KS3服务，[http://www.ksyun.com/user/register](http://www.ksyun.com/user/register) 注册账号  
2、进入控制台, [http://ks3.ksyun.com/console.html#/setting](http://ks3.ksyun.com/console.html#/setting) 获取AccessKeyID 、AccessKeySecret
### 3.4 初始化客户端
当以上全部完成之后用户便可初始化客户端进行操作了  

	Ks3 client = new Ks3Client("<您的AccessKeyID>","<您的AccessKeySecret>");
使用自定义的配置（3.1）
	
	Ks3 client = new Ks3Client("<您的AccessKeyID>","<您的AccessKeySecret>",config);
	或者：client.setKs3config(config);
	
## 4 快速入门
请先阅读常用概念术语文档,[http://ks3.ksyun.com/doc/api/index.html](http://ks3.ksyun.com/doc/api/index.html)
### 4.1 新建一个存储空间
进入控制台，[http://ks3.ksyun.com/console.html#/](http://ks3.ksyun.com/console.html#/),点击右上角新建空间，新建的空间名称即为之后提到的bucket
### 4.2 上传文件
    /**
    *将new File("<filePath>")这个文件上传至<bucket名称>这个存储空间下，并命名为<key>
    */
    public void putObjectSimple(){
    	PutObjectRequest request = new PutObjectRequest("<bucket名称>",
				"<key>", new File("<filePath>"));
		//上传一个公开文件
		//request.setCannedAcl(CannedAccessControlList.PublicRead);
	    client.putObject(request);
    }
    
说明：

- 该方法需要三个参数，第一个参数为在4.1时新建的存储空间名称。第二个参数为文件在KS3上保持的路径，比如：image.jpg，2015/10/19/image.jpg 等。第三个参数为要上传的文件对象。  

### 4.3 通过表单的方式上传文件
1、了解KS3表单上传协议[http://ks3.ksyun.com/doc/api/object/post.html](http://ks3.ksyun.com/doc/api/object/post.html)。以及表单上传签名认证方式[http://ks3.ksyun.com/doc/api/object/post_policy.html](http://ks3.ksyun.com/doc/api/object/post_policy.html)  
2、在KS3控制台->空间设置->CORS配置里配置CORS跨域规则。[http://ks3.ksyun.com/console.html#/](http://ks3.ksyun.com/console.html#/),CORS为跨域资源共享，当使用js跨域时，需要配置该规则。W3C文档[http://www.w3.org/TR/cors/](http://www.w3.org/TR/cors/)    
3、使用js sdk上传文件[https://github.com/ks3sdk/ks3-js-sdk](https://github.com/ks3sdk/ks3-js-sdk)  
4、js sdk 中的policy和signature可以通过Java SDK的该方法计算出来。

	/**
	  如果用户对KS3协议不是特别清楚,建议使用该方法。每次上传的时候都去获取一次最新的签名信息
	*/
	public PostObjectFormFields postObjectSimple(){
		/**
		 * 需要用户在postData和unknowValueField中提供所有的除KSSAccessKeyId, signature, file, policy外的所有表单项。否则用生成的签名上传会返回403</br>
		 * 对于用户可以确定表单值的放在 postData中，对于用户无法确定表单值的放在unknownValueField中(比如有的上传控件会添加一些表单项,但表单项的值可能是随机的)</br>
		 * 
		 */
		Map<String,String> postData = new HashMap<String,String>();
		
		//如果使用js sdk上传的时候设置了ACL，请提供以下一行，且值要与SDK中一致，否则删除下面一行代码
		postData.put("acl","public-read");
		//提供js sdk中的key值
		postData.put("key","20150115/中文/${filename}");
		
		List<String> unknowValueField = new ArrayList<String>();
		//js sdk上传的时候会自动加上一个name的表单项，所以下面需要加上这样的代码。
		unknowValueField.add("name");
		
		//如果计算签名时提供的key里不包含${filename}占位符，可以把第二个参数传一个空字符串。因为计算policy需要的key是把${filename}进行替换后的key。
		PostObjectFormFields fields = client.postObject("<您的bucket名称>", "<要上传的文件名称,不包含路径信息>", postData, unknowValueField);
		
		fields.getKssAccessKeyId();
		fields.getPolicy();
		fields.getSignature();
		
		return fields;
	}
常见问题：

- 上传时，浏览器一共会发送两个请求，第一个为OPTIONS请求，第二个为POST请求。如果第一个请求返回403，请检测CORS配置是否正确。如果第二个请求返回403，请检查生成policy时，是否完全按照表单内容生成。或者把policy进行base64解码，对比表单内容是否正确。对比规则：[http://ks3.ksyun.com/doc/api/object/post_policy.html](http://ks3.ksyun.com/doc/api/object/post_policy.html)  

### 4.4 获取文件访问地址
1、如果是公开文件  
通过：http://{bucket}.{endpoint}/{key}的方式拼接一个URL即可。比如：http://test-bucket.kssws.ks-cdn.com/2015/10/19/image.jpg,该URL中的{bucket}是test-bucket,{endpoint}是kssws.ks-cdn.com,{key}是2015/10/19/image.jpg  
2、如果是私有文件  
通过以下代码可以生成一个访问地址

	//生成一个在1000秒后过期的外链
	client.generatePresignedUrl(<bucket>,<key>,1000);
	
	//生成一个1000秒后过期并重写返回的heade的外链
	ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
	//overrides.setContentType("text/html");
	//.......

	client.generatePresignedUrl(<bucket>,<key>,1000,overrides);
	
常见问题：

- 如果文件不存在，会返回NoSuchKey错误
- 如果以公开的方式访问私有文件，会返回AccessDined错误
- 如果私有文件访问地址过期，会返回URLExpired错误
- 1000秒后过期是参照客户端本地时间的。

## 5 存储空间(bucket)相关
### 5.1 获取所有bucket
通过该接口，用户可以获取到其所有的bucket。  

	public List<Bucket> listBuckets(){
		List<Bucket> buckets = client.listBuckets();

		for(Bucket bucket:buckets){
			//获取bucket的创建时间
			bucket.getCreationDate();
			//获取bucket的名称
			bucket.getName();
			//获取bucket的拥有者（用户ID base64后的值）
			bucket.getOwner();
			//获取bucket的区域信息
			bucket.getRegion();
			//获取bucket类型，非归档（BucketType.Normal）、归档（BucketType.Archive）
			bucket.getType();
		}
		
		return buckets;
	}

### 5.2 新建bucket

	/**
	 * <p>使用最简单的方式创建一个bucket</p>
	 * <p>将使用默认的配置，权限为私有，存储地点为杭州</p>
	 */
	public void createBucketSimple(){
		client.createBucket("<您的bucket名称>");
	}
	/**
	 * <p>新建bucket的时候配置bucket的存储地点和访问权限</p>
	 */
	public void createBucketWithConfig(){
		CreateBucketRequest request = new CreateBucketRequest("<您的bucket名称>");
		//配置bucket的存储地点
		CreateBucketConfiguration config = new CreateBucketConfiguration(REGION.HANGZHOU);
		request.setConfig(config);
		//配置bucket类型，非归档（BucketType.Normal）、归档（BucketType.Archive）
		request.setBucketType(BucketType.Normal);
		//配置bucket的访问权限
		request.setCannedAcl(CannedAccessControlList.Private);
		//执行操作
		client.createBucket(request);
	}

注意:

- bucket名称是全局唯一的

### 5.3 设置bucket访问权限
bucket的访问权限说明 [http://ks3.ksyun.com/doc/api/index.html](http://ks3.ksyun.com/doc/api/index.html) 详解ACL小节    

	/**
	*通过预设的ACL设置bucket的访问权限，预设的ACL包括:private:私有。public-read:为所有用户授予read权限。public-read-write:为所有用户授予read和write权限，
	*/
	public void putBucketAclWithCannedAcl(){
		PutBucketACLRequest request = new PutBucketACLRequest("<您的bucket名称>");
		//设为私有
		request.setCannedAcl(CannedAccessControlList.Private);
		//设为公开读 
		//request.setCannedAcl(CannedAccessControlList.PublicRead);
		//设为公开读写
		//request.setCannedAcl(CannedAccessControlList.PublicReadWrite);
		client.putBucketACL(request);
	}
	/**
	*自定义bucket的访问权限。
	*/
	public void putBucketAclWithAcl(){
		PutBucketACLRequest request = new PutBucketACLRequest("<您的bucket名称>");

		AccessControlList acl = new AccessControlList();
		//设置用户id为12345678的用户对bucket的读权限
		Grant grant1 = new Grant();
		grant1.setGrantee(new GranteeId("12345678"));
		grant1.setPermission(Permission.Read);
		acl.addGrant(grant1);
		//设置用户id为123456789的用户对bucket完全控制
		Grant grant2 = new Grant();
		grant2.setGrantee(new GranteeId("123456789"));
		grant2.setPermission(Permission.FullControl);
		acl.addGrant(grant2);
		//设置用户id为12345678910的用户对bucket的写权限
		Grant grant3 = new Grant();
		grant3.setGrantee(new GranteeId("12345678910"));
		grant3.setPermission(Permission.Write);
		acl.addGrant(grant3);
		request.setAccessControlList(acl);
		
		client.putBucketACL(request);
	}


### 5.4 获取bucket访问权限

	public AccessControlPolicy getBucketAcl(){
		AccessControlPolicy acl = null;
		//只需要传入这个bucket的名称即可
		acl = client.getBucketACL("<您的bucket名称>");
		return acl;
	}
	
说明:

- AccessControlPolicy中有public CannedAccessControlList getCannedAccessControlList()方法可以从ACL中提取出预设ACL来。
	
### 5.5 为bucket配置日志

设置bucket的日志配置,当为bucket添加上该配置后，KS3将自动把bucket及bucket下的文件的操作日志放到指定的空间中  

	/**
	 * 将存储空间的操作日志存储在 <存放日志文件的bucket名称> 里面，日志文件的前缀是"logging-"
	 */
	public void putBucketLogging(){
		PutBucketLoggingRequest request = new PutBucketLoggingRequest("<您的bucket名称>");
		//开启日志
		request.setEnable(true);
		request.setTargetBucket("<存放日志文件的bucket名称>");
		//设置日志文件的前缀为logging-
		request.setTargetPrefix("logging-");
		client.putBucketLogging(request);
	}
注意：

- 存放日志文件的bucket必须是当前用户同一个region的bucket。
- 要配置日志的bucket和存放日志文件的bucket可以是一个bucket，但是为了用户方便管理，建议使用两个不同的bucket。

### 5.6 获取bucket的日志配置

	public BucketLoggingStatus getBucketLogging(){
		//只需要传入bucket的名称，由于ks3暂时对日志权限不支持，所以返回的BucketLoggingStatus中targetGrants始终为空集合
		BucketLoggingStatus logging = client.getBucketLogging("<您的bucket名称>");
		return logging;
	}

### 5.7 判断bucket是否存在，以及是否有读取权限

	/**
	 * Head请求一个bucket
	 */
	public HeadBucketResult headBucket() {
		HeadBucketResult result = client.headBucket("<您的bucket名称>");
		/**
		 * 通过result.getStatueCode()状态码 404则这个bucket不存在，403当前用户没有权限访问这个bucket
		 */
		return result;
	}

### 5.8 获取bucket的region信息

	public REGION getBucketLocation(){
		//只需要传入bucket的名称
		REGION region = client.getBucketLoaction("<您的bucket名称>");
		return region;
	}

### 5.9 设置bucket的CORS跨域规则

	public void putBucketCors(){
		BucketCorsConfiguration config = new BucketCorsConfiguration();
		//资源跨域共享规则
		CorsRule rule1 = new CorsRule();
		
		//指定允许的跨域请求方法(GET/PUT/DELETE/POST/HEAD) 
		List<AllowedMethods> allowedMethods = new ArrayList<AllowedMethods>();
		allowedMethods.add(AllowedMethods.GET);
		//指定允许跨域请求的来源 
		List<String> allowedOrigins = new ArrayList<String>();
		allowedOrigins.add("http://example.com");
		//指定允许用户从应用程序中访问的响应头 
		List<String> exposedHeaders = new ArrayList<String>();
		exposedHeaders.add("x-kss-test1");
		//控制在 OPTIONS 预取指令中 Access-Control-Request-Headers 头中指定的 header 是否允许。
		List<String> allowedHeaders = new ArrayList<String>();
		allowedHeaders.add("x-kss-test"); 

		rule1.setAllowedHeaders(allowedHeaders);
		rule1.setAllowedMethods(allowedMethods);
		rule1.setAllowedOrigins(allowedOrigins);
		rule1.setExposedHeaders(exposedHeaders);
		//指定浏览器对特定资源的预取(OPTIONS)请求返回结果的缓存时间,单位为秒。 
		rule1.setMaxAgeSeconds(200);
		
		config.addRule(rule1);
		
		
		//一个bucket可以最多配置10条规则
		CorsRule rule2 = new CorsRule();
		List<AllowedMethods> allowedMethods2 = new ArrayList<AllowedMethods>();
		allowedMethods2.add(AllowedMethods.GET);
		allowedMethods2.add(AllowedMethods.POST);
		List<String> allowedOrigins2 = new ArrayList<String>();
		allowedOrigins2.add("http://example.com");
		allowedOrigins2.add("http://*.example.com");
		List<String> exposedHeaders2 = new ArrayList<String>();
		exposedHeaders2.add("x-kss-test1");
		exposedHeaders2.add("x-kss-test2");
		List<String> allowedHeaders2 = new ArrayList<String>();
		allowedHeaders2.add("x-kss-test"); 
		allowedHeaders2.add("x-kss-test2"); 
		rule2.setAllowedHeaders(allowedHeaders2);
		rule2.setAllowedMethods(allowedMethods2);
		rule2.setAllowedOrigins(allowedOrigins2);
		rule2.setExposedHeaders(exposedHeaders2);
		rule2.setMaxAgeSeconds(500);
		
		config.addRule(rule2);
		
		PutBucketCorsRequest request = new PutBucketCorsRequest("<您的bucket名称>",config);
		client.putBucketCors(request);
	}

注意：

- CORS是指跨域资源共享，当使用javascript进行跨域的时候，需要为bucket配置该规则。W3C文档[http://www.w3.org/TR/cors/](http://www.w3.org/TR/cors/)   
- AllowedMethods、AllowedHeaders和AllowedOrigins为必选参数。AllowedHeaders和AllowedOrigins中支持最多一个*号的通配。

### 5.10 获取bucket的CORS跨域规则

	public BucketCorsConfiguration getBucketCors(){
		BucketCorsConfiguration config = client.getBucketCors("test.bucket");
		List<CorsRule> rules = config.getRules();
		for(CorsRule rule : rules){
			//控制在 OPTIONS 预取指令中 Access-Control-Request-Headers 头中指定的 header 是否允许。
			rule.getAllowedHeaders();
			//允许的跨域请求方法(GET/PUT/DELETE/POST/HEAD) 
			rule.getAllowedMethods();
			//允许跨域请求的来源 
			rule.getAllowedOrigins();
			//允许用户从应用程序中访问的响应头 
			rule.getExposedHeaders();
			//浏览器对特定资源的预取(OPTIONS)请求返回结果的缓存时间,单位为秒。 
			rule.getMaxAgeSeconds();
		}
		return config;
	}



### 5.11 删除bucket的CORS跨域规则

	public void deleteBucketCors(){
		client.deleteBucketCors("test.bucket");
	}

## 6 文件(object)相关
### 6.1 罗列空间中的文件列表

	/**
	 * 列出一个bucket下的object，返回的最大数为1000条
	 */
	public ObjectListing listObjectsSimple(){
		ObjectListing list = client.listObjects("<您的bucket名称>");
		return list;
	}
	/**
	 * 将列出bucket下满足object key前缀为指定字符串的object，返回的最大数为1000条
	 */
	public ObjectListing listObjectsWithPrefix(){
		ObjectListing list = client.listObjects("<您的bucket名称>","<object key前缀>");
		return list;
	}
	/**
	 * 自己调节列出object的参数，
	 */
	public ObjectListing listObjectsUseRequest(){
		ObjectListing list = null;
		//新建一个ListObjectsRequest
		ListObjectsRequest request = new ListObjectsRequest("<您的bucket名称>");
		//设置参数
		request.setMaxKeys("<max keys>");//指定返回条数最大值
		request.setPrefix("<object key前缀>");//返回以指定前缀开头的object
        request.setDelimiter("<delimiter>");//设置文件分隔符，系统将根据该分隔符组织文件夹结构，默认是"/"
		//执行操作
		list = client.listObjects(request);
		return list;
	}
	/**
	 * 使用循环列出所有object
	 */
	public void listAllObjects(){
		ObjectListing list = null;
		//初始化一个请求
		ListObjectsRequest request = new ListObjectsRequest("<您的bucket名称>");
		do{
			//isTruncated为true时表示之后还有object，所以应该继续循环
			if(list!=null&&list.isTruncated())
				//在ObjectListing中将返回下次请求的marker
				//如果请求的时候没有设置delimiter，则不会返回nextMarker,需要使用上一次list的最后一个key做为nextMarker
		    	request.setMarker(list.getNextMarker());
			list = client.listObjects(request);
		}while(list.isTruncated());
	}

注意：

- 单次list，最多只可能返回1000个文件。如果需要返回更多，请通过marker参数设置游标。
- 具体输入输出参数说明详见API文档。[http://ks3.ksyun.com/doc/api/bucket/get.html](http://ks3.ksyun.com/doc/api/bucket/get.html)

### 6.2 上传文件


通过文件上传

    /**
    *将new File("<filePath>")这个文件上传至<bucket名称>这个存储空间下，并命名为<object key>
    */
    public void putObjectSimple(){
    	PutObjectRequest request = new PutObjectRequest("<bucket名称>",
				"<object key>", new File("<filePath>"));
		//上传一个公开文件
		//request.setCannedAcl(CannedAccessControlList.PublicRead);
	    client.putObject(request);
    }

通过流上传

	/**
	    *将一个输入流中的内容上传至<bucket名称>这个存储空间下，并命名为<object key>
    */
    public void putObjectSimple(){
		ObjectMetadata meta = new ObjectMetadata();
		PutObjectRequest request = new PutObjectRequest("<bucket名称>",
				"<object key>", new ByteArrayInputStream("1234".getBytes()),
				meta);
		// 可以指定内容的长度，否则程序会把整个输入流缓存起来，可能导致jvm内存溢出
		meta.setContentLength(4);
		client.putObject(request);
	}
	
上传文件时设置存储类型

	request.setStorageClass(StorageClass.Standard);
	或
	request.setStorageClass(StorageClass.StandardInfrequentAccess);
	或
	request.setStorageClass(StorageClass.Archive);
	
上传文件时设置元数据

	ObjectMetadata meta = new ObjectMetadata();
	// 设置将要上传的object的用户元数据。当下载文件的时候，返回的header中将会带上设置的用户元数据。
	//meta.setUserMeta("x-kss-meta-example", "example");
	//设置将要上传的object的元数据。
	//如果用户设置了Content-Type，那么在下载的时候，返回的header中将会带上Content-Type:{设置的值} 。建议根据文件实际类型做相应设置，方便浏览器识别。
	//meta.setContentType("text/html");
	request.setObjectMeta(meta);

设置回调
	
	//如果用户设置了回调，那么当文件即将上传成功时，KS3将会使用POST的方式调用用户提供的回调地址，如果调用成功且用户处理成功(用户处理成功指返回{"result":true}给KS3，处理失败返回{"result":false})，那么文件才会真正的上传成功，如果调用失败或者用户处理失败，那么文件最终会上传失败。用户可以设置KS3回调时的body，最后KS3将会把用户提供的参数组织成json格式返回给用户。在下面示例中，KS3回调时的body为{"bucket":<实际存储的bucket>,"createTime":<文件创建时间>,"etag":<文件的etag,即文件的MD5经hex处理后的值>,"key":<文件实际保持的key>,"mimType":<文件的Content-Type>,"objectSize":<文件的字节数大小>,"time":"20150222"}
	CallBackConfiguration config = new CallBackConfiguration();
	config.setCallBackUrl("http://10.4.2.38:19090/");//KS3服务器回调的地址
	//以下为KS3服务器访问http://10.4.2.38:19090/时body中的参数
	Map<String,MagicVariables> magicVariables = new HashMap<String,MagicVariables>();
	magicVariables.put("bucket", MagicVariables.bucket);
	magicVariables.put("createTime", MagicVariables.createTime);
	magicVariables.put("etag", MagicVariables.etag);
	magicVariables.put("key", MagicVariables.key);
	magicVariables.put("mimeType", MagicVariables.mimeType);
	magicVariables.put("objectSize", MagicVariables.objectSize);
		
	config.setBodyMagicVariables(magicVariables);
	
	//用户可以自己定义返回的参数。
	Map<String,String> kssVariables = new HashMap<String,String>();
		
	kssVariables.put("time", "20150222");
	request.setCallBackConfiguration(config);

设置异步数据处理任务

	//设置异步数据处理任务,该任务的作用是当文件上传成功后，对上传的文件进行视频转码功能（以下代码中是视频转码，当然还有其	他各种各样的功能），将转码后的视频存储为“野生动物-转码.3gp”，并且将转码结果信息发送到http://10.4.2.38:19090/   。
	List<Adp> adps= new ArrayList<Adp>();
	Adp adp= new Adp();
	//具体command详见：http://ks3.ksyun.com/doc/video/avop.html
	adp.setCommand("tag=avop&f=mp4&res=1280x720&vbr=1000k&abr=128k");
	//处理完成后存储的key，相当于文档中说的tag=saveas&object=....
	adp.setKey("野生动物-转码.3gp");
	adps.add(adp);
	request.setAdps(adps);
	request.setNotifyURL("http://10.4.2.38:19090/");

设置服务端加密

	ObjectMetadata meta = new ObjectMetadata();
	meta.setSseAlgorithm("AES256");
	request.setObjectMeta(meta);

设置用户提供key的服务端加密

	//生成一个秘钥，这个秘钥需要自己保存好，加密解密都需要
	KeyGenerator symKeyGenerator = KeyGenerator.getInstance("AES");
	symKeyGenerator.init(256); 
	SecretKey symKey = symKeyGenerator.generateKey();
	
	SSECustomerKey ssec = new SSECustomerKey(symKey);
	request.setSseCustomerKey(ssec);

注意：

- 如何下载文件？详见6.7和6.8
- 异步数据处理命令详见：[http://ks3.ksyun.com/doc/video/avop.html](http://ks3.ksyun.com/doc/video/avop.html)
- 如果用户使用了用户提供key的服务端加密，则下载文件的时候，必须提供加密使用的key，否则将无法下载。

### 6.3 通过表单上传文件

1、了解KS3表单上传协议[http://ks3.ksyun.com/doc/api/object/post.html](http://ks3.ksyun.com/doc/api/object/post.html)。以及表单上传签名认证方式[http://ks3.ksyun.com/doc/api/object/post_policy.html](http://ks3.ksyun.com/doc/api/object/post_policy.html)  
2、在KS3控制台->空间设置->CORS配置里配置CORS跨域规则。[http://ks3.ksyun.com/console.html#/](http://ks3.ksyun.com/console.html#/),CORS为跨域资源共享，当使用js跨域时，需要配置该规则。W3C文档[http://www.w3.org/TR/cors/](http://www.w3.org/TR/cors/)    
3、使用js sdk上传文件[https://github.com/ks3sdk/ks3-js-sdk](https://github.com/ks3sdk/ks3-js-sdk)  
4、js sdk 中的policy和signature可以通过Java SDK的该方法计算出来。

	/**
	  如果用户对KS3协议不是特别清楚,建议使用该方法。每次上传的时候都去获取一次最新的签名信息
	*/
	public PostObjectFormFields postObjectSimple(){
		/**
		 * 需要用户在postData和unknowValueField中提供所有的除KSSAccessKeyId, signature, file, policy外的所有表单项。否则用生成的签名上传会返回403</br>
		 * 对于用户可以确定表单值的放在 postData中，对于用户无法确定表单值的放在unknownValueField中(比如有的上传控件会添加一些表单项,但表单项的值可能是随机的)</br>
		 * 
		 */
		Map<String,String> postData = new HashMap<String,String>();
		
		//如果使用js sdk上传的时候设置了ACL，请提供以下一行，且值要与SDK中一致，否则删除下面一行代码
		postData.put("acl","public-read");
		//提供js sdk中的key值
		postData.put("key","20150115/中文/${filename}");
		
		List<String> unknowValueField = new ArrayList<String>();
		//js sdk上传的时候会自动加上一个name的表单项，所以下面需要加上这样的代码。
		unknowValueField.add("name");
		
		//如果计算签名时提供的key里不包含${filename}占位符，可以把第二个参数传一个空字符串。因为计算policy需要的key是把${filename}进行替换后的key。
		PostObjectFormFields fields = client.postObject("<您的bucket名称>", "<要上传的文件名称,不包含路径信息>", postData, unknowValueField);
		
		fields.getKssAccessKeyId();
		fields.getPolicy();
		fields.getSignature();
		
		return fields;
	}
常见问题：

- 上传时，浏览器一共会发送两个请求，第一个为OPTIONS请求，第二个为POST请求。如果第一个请求返回403，请检测CORS配置是否正确。如果第二个请求返回403，请检查生成policy时，是否完全按照表单内容生成。或者把policy进行base64解码，对比表单内容是否正确。对比规则：[http://ks3.ksyun.com/doc/api/object/post_policy.html](http://ks3.ksyun.com/doc/api/object/post_policy.html)  

### 6.4 文件复制

	public void copyObject(){
		/**将sourceBucket这个存储空间下的sourceKey这个object复制到destinationBucket这个存储空间下，并命名为destinationObject
		 */
		CopyObjectRequest request = new CopyObjectRequest("destinationBucket","destinationObject","sourceBucket","sourceKey");
		client.copyObject(request);
	}
	
copy后的文件以服务端加密方式存储

	ObjectMetadata meta = new ObjectMetadata();
	meta.setSseAlgorithm("AES256");
	request.setNewObjectMetadata(meta);

copy的文件以用户提供key的方式进行服务端加密，并设置新的文件的服务端加密

	//生成一个秘钥，这个秘钥需要自己保存好，加密解密都需要
	KeyGenerator symKeyGenerator = KeyGenerator.getInstance("AES");
	symKeyGenerator.init(256); 
	SecretKey destKey= symKeyGenerator.generateKey();

	SecretKey sourceKey= ??//当初加密时用的key
	
	//指定被拷贝的数据是用sourceKey进行加密的，拷贝时将用该key先对数据解密
	request.setSourceSSECustomerKey(new SSECustomerKey(sourceKey));
	//指定拷贝生成的新数据的加密方式
	request.setDestinationSSECustomerKey(new SSECustomerKey(destKey));
	
注意：

- 文件复制的时候，需要用户对源文件有读取权限，对目标bucket有写权限。
- 如果目标文件已经存在，复制操作将会抛出400，对应的ErrorCode是 invalidKey

### 6.5 获取文件元数据

	public HeadObjectResult headObject() {
		HeadObjectRequest request = new HeadObjectRequest("<bucket名称>",
				"<object名称>");
		/**
		 * <p>
		 * 如果抛出{@link NotFoundException} 表示这个object不存在
		 * </p>
		 * <p>
		 * 如果抛出{@link AccessDinedException} 表示当前用户没有权限访问
		 * </p>
		 */
		HeadObjectResult result = client.headObject(request);
		// head请求可以用于获取object的元数据
		result.getObjectMetadata();
		return result;
	}

### 6.6 判断文件是否存在

	/**
	 * 判断一个object是否存在
	 */
	public boolean objectExists() {
		try {
			HeadObjectRequest request = new HeadObjectRequest("<bucket名称>",
					"<object名称>");
			client.headObject(request);
			return true;
		} catch (NotFoundException e) {
			return false;
		}
	}

### 6.7 下载文件

	public GetObjectResult getObject(){
		GetObjectRequest request = new GetObjectRequest("<bucket名称>","<object key>");
		
		//重写返回的header
		ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
		overrides.setContentType("text/html");
		//.......
		request.setOverrides(overrides);
		//只接受数据的0-10字节。通过控制该项可以实现分块下载
		//request.setRange(0,10);
		GetObjectResult result = client.getObject(request);
		
		Ks3Object object = result.getObject();
		//获取object的元数据
		ObjectMetadata meta = object.getObjectMetadata();
		//当分块下载时获取文件的实际大小，而非当前小块的大小
		Long length = meta.getInstanceLength();
		//获取object的输入流
		object.getObjectContent();
		
		return result;
	}

如果文件是通过用户提供key的方式进行服务端加密的

	SecretKey sourceKey= ??//当初加密时用的key
	request.setSseCustomerKey(new SSECustomerKey(sourceKey));

### 6.8 获取文件访问地址

1、如果是公开文件  
通过：http://{bucket}.{endpoint}/{key}的方式拼接一个URL即可。比如：http://test-bucket.kssws.ks-cdn.com/2015/10/19/image.jpg,该URL中的{bucket}是test-bucket,{endpoint}是kssws.ks-cdn.com,{key}是2015/10/19/image.jpg  
2、如果是私有文件  
通过以下代码可以生成一个访问地址

	//生成一个在1000秒后过期的外链
	client.generatePresignedUrl(<bucket>,<key>,1000);
	
	//生成一个1000秒后过期并重写返回的heade的外链
	ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
	//overrides.setContentType("text/html");
	//.......

	client.generatePresignedUrl(<bucket>,<key>,1000,overrides);
	
常见问题：

- 如果文件不存在，会返回NoSuchKey错误
- 如果以公开的方式访问私有文件，会返回AccessDined错误
- 如果私有文件访问地址过期，会返回URLExpired错误
- 1000秒后过期是参照客户端本地时间的。

### 6.9 设置文件访问权限
object的访问权限说明 [http://ks3.ksyun.com/doc/api/index.html](http://ks3.ksyun.com/doc/api/index.html) 详解ACL小节    

	/**
	*通过预设的ACL设置object的访问权限，预设的ACL包括:private:私有。public-read:为所有用户授予read权限。
	*/
	public void putBucketAclWithCannedAcl(){
		PutBucketACLRequest request = new PutBucketACLRequest("<bucket名称>");
		//设为私有
		request.setCannedAcl(CannedAccessControlList.Private);
		//设为公开读 
		//request.setCannedAcl(CannedAccessControlList.PublicRead);
		client.putBucketACL(request);
	}
	/**
	*自定义文件访问权限
	*/
	public void putBucketAclWithAcl(){
		PutBucketACLRequest request = new PutBucketACLRequest("<bucket名称>");

		AccessControlList acl = new AccessControlList();
		//设置用户id为12345678的用户对bucket的读权限
		Grant grant1 = new Grant();
		grant1.setGrantee(new GranteeId("12345678"));
		grant1.setPermission(Permission.Read);
		acl.addGrant(grant1);
		//设置用户id为123456789的用户对bucket完全控制
		Grant grant2 = new Grant();
		grant2.setGrantee(new GranteeId("123456789"));
		grant2.setPermission(Permission.FullControl);
		acl.addGrant(grant2);
		//设置用户id为12345678910的用户对bucket的写权限
		Grant grant3 = new Grant();
		grant3.setGrantee(new GranteeId("12345678910"));
		grant3.setPermission(Permission.Write);
		acl.addGrant(grant3);
		request.setAccessControlList(acl);
		
		client.putBucketACL(request);
	}


### 6.10 获取文件访问权限

	public AccessControlPolicy getObjectAcl(){
		/**
		 * 获取 <bucket名称>这个bucket下<object key>的权限控制信息
		 */
		AccessControlPolicy policy = client.getObjectACL("<bucket名称>","<object key>");
		return policy;
	}

说明:

- AccessControlPolicy中有public CannedAccessControlList getCannedAccessControlList()方法可以从ACL中提取出预设ACL来。


### 6.11 删除文件

	/**
	 * 将<bucket名称>这个存储空间下的<object key>删除
	 */
	public void deleteObject(){
		client.deleteObject("<bucket名称>","<object key>");
	}


### 6.12 解冻文件

	/**
	 * 解冻<bucket名称>这个存储空间下的<object key>文件
	 */
	public RestoreObjectResult restoreObject(){
		RestoreObjectResult result = client.restoreObject("<bucket名称>","<object key>");
		//获取<object key>文件的存储类型，类型如下
		//标准（StorageClass.Standard）、低频（StorageClass.StandardInfrequentAccess）、归档（StorageClass.Archive）
		result.getCls();
		//获取<object key>文件的解冻状态，状态如下
		//解冻成功（RestoreCycle.RESTORE）、正在解冻中（RestoreCycle.RESTORING）、已解冻（RestoreCycle.RESTORED）
		result.getType();
		return result;
	}



## 7 分块上传相关

分块上传流程：  
1、初始化分块上传：7.2  
2、多次调用上传块：7.3  
3、完成分块上传：7.5（如果中途失败，则取消分块上传：7.6）  

### 7.1 罗列空间中正在进行的分块上传

	public ListMultipartUploadsResult listMultipartUploads() {
		ListMultipartUploadsRequest request = new ListMultipartUploadsRequest("test.bucket");
		/**
		 * keyMarker为空，uploadIdMarker不为空
		 * 无意义
		 * 
		 * keyMarker不为空，uploadIdMarker不为空
		 * 列出分块上传object key为keyMarker，且upload id 字典排序大于uploadIdMarker的结果
		 * 
		 * keyMarker不为空，uploadIdMarker为空
		 * 列出分块上传object key字典排序大于keyMarker的结果
		 */
		 request.setKeyMarker("keyMarker");
		 request.setUploadIdMarker("uploadIdMarker");

		 /**
		 * prefix和delimiter详解
		 * 
		 * commonPrefix由prefix和delimiter确定，以prefix开头的object
		 * key,在prefix之后第一次出现delimiter的位置之前（包含delimiter）的子字符串将存在于commonPrefixes中
		 * 比如有一下几个个分块上传
		 * 
		 * aaaa/bbb/ddd.txt
		 * aaaa/ccc/eee.txt
		 * ssss/eee/fff.txt
		 * 
		 * prefix为空 delimiter为/ 
		 * 则commonPrefix 为 aaaa/和ssss/ 返回的uploads为空
		 * 
		 * prefix为aaaa/ delimiter为/ 
		 * 则commonPrefix 为 aaaa/bbb/和aaaa/ccc/ 返回的uploads为空
		 * 
		 * prefix为ssss/ delimiter为/ 
		 * 则commonPrefix 为 aaaa/eee/ 返回的uploads为空
		 * 
		 * prefix为空 delimiter为空 
		 * 则commonPrefix 为空 返回的uploads为aaaa/bbb/ddd.txt、aaaa/ccc/eee.txt、ssss/eee/fff.txt
		 * 
		 * prefix为aaaa/ delimiter为空 
		 * 则commonPrefix 为空 返回的uploads为aaaa/bbb/ddd.txt、aaaa/ccc/eee.txt
		 * 
		 * prefix为ssss/ delimiter为空 
		 * 则commonPrefix 为空 返回的uploads为ssss/eee/fff.txt
		 * </p>
		 */
		request.setDelimiter("/");
		request.setPrefix("prefix");
		request.setMaxUploads(100);// 最多返回100条记录，默认（最大）是1000
		ListMultipartUploadsResult result = client
				.listMultipartUploads(request);
		return result;
	}
	
说明：

- 最多只能返回1000条记录
- keyMarker、uploadIdMarker、delimiter、prefix、maxUploads都不是必要参数。用户可以根据自己的实际情况调节
- 当返回的result.isTruncated()为true时，需要用返回的result.getNextKeyMarker()和result.getNextUploadIdMarker参数作为keyMarker和uploadIdMarker继续罗列之后的分块上传。

### 7.2 初始化分块上传

	public InitiateMultipartUploadResult initMultipartUpload(){
		InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(
					bucket, key);
		//设置为公开读
		request.setCannedAcl(CannedAccessControlList.PublicRead);
		
		InitiateMultipartUploadResult result = client
				.initiateMultipartUpload(request);
		result.getUploadId();//获取初始化的uploadId,之后的操作中将会用到。
		return result;
	}
	
初始化分块上传时设置元数据

	ObjectMetadata meta = new ObjectMetadata();
	// 设置将要上传的object的用户元数据。当下载文件的时候，返回的header中将会带上设置的用户元数据。
	//meta.setUserMeta("x-kss-meta-example", "example");
	//设置将要上传的object的元数据。
	//如果用户设置了Content-Type，那么在下载的时候，返回的header中将会带上Content-Type:{设置的值} 。建议根据文件实际类型做相应设置，方便浏览器识别。
	//meta.setContentType("text/html");
	request.setObjectMeta(meta);

设置服务端加密

	ObjectMetadata meta = new ObjectMetadata();
	meta.setSseAlgorithm("AES256");
	request.setObjectMeta(meta);

设置用户提供key的服务端加密

	//生成一个秘钥，这个秘钥需要自己保存好，加密解密都需要
	KeyGenerator symKeyGenerator = KeyGenerator.getInstance("AES");
	symKeyGenerator.init(256); 
	SecretKey symKey = symKeyGenerator.generateKey();
	
	SSECustomerKey ssec = new SSECustomerKey(symKey);
	request.setSseCustomerKey(ssec);
	
说明：

- 通过result.getUploadId()可以获取到本次上传的uploadId，之后的操作中将到会用到该uploadId。
- 如何下载文件？详见6.7和6.8
- 如果用户使用了用户提供key的服务端加密，则下载文件的时候，必须提供加密使用的key，否则将无法下载。

### 7.3 上传块
提供文件及partSize和offset参数的方式上传。

	public PartETag uploadPart(){
			UploadPartRequest request = new UploadPartRequest(
					{bucket}, {key} ,{uploadId},
					{partNumber},{要上传的文件}, {partSize},{offset});
			//可以指定内容的MD5值，否则程序只会在客户端进行MD5校验。如果指定的话会在服务端进行MD5校验
			//request.setContentMd5("yE0ZQBpRgPrLSDEe6fGAvg==");
			PartETag result = client.uploadPart(request);
			return result;
	}
	
提供输入流和partSize的方式上传。

	public PartETag uploadPart(){
			InputStream content = new ByteArrayInputStream("content".getBytes());
			UploadPartRequest request = new UploadPartRequest(
					{bucket}, {key} ,{uploadId},
					{partNumber},content,{partSize});
			//可以指定内容的MD5值，否则程序只会在客户端进行MD5校验。如果指定的话会在服务端进行MD5校验
			//request.setContentMD5("yE0ZQBpRgPrLSDEe6fGAvg==");
			PartETag result = client.uploadPart(request);
			return result;
	}
	
参数说明：

- bucket：初始化分块上传时的bucket
- key:初始化分块上传时的key
- uploadId:初始化分块上传获取到的uploadId
- partNumber:当前上传的块的序号，需要为连续的整数
- partSize:当前上传的块的字节数

设置用户提供key的服务端加密

	//当init multipart upload时指定了用户提供key的服务端加密，在upload part时也需要指定相同的加密信息
	SecretKey symKey = ??//init时的key
	SSECustomerKey ssec = new SSECustomerKey(symKey);
	request.setSseCustomerKey(ssec);
	
说明：

- partNumber需要是连续的整数
- 用户需要在自己程序中保存每次分块上传返回的partNumber和ETag,在之后的完成分块上传时需要用到。

### 7.4 罗列已经上传的块

罗列一个uploadId已经上传的块。

	public ListPartsResult listParts(){
		ListPartsRequest request = new ListPartsRequest({bucket}, {key} ,{uploadId});
		ListPartsResult tags = client.listParts(request);
		return tags;
	}

参数说明：

- bucket：初始化分块上传时的bucket
- key:初始化分块上传时的key
- uploadId:初始化分块上传获取到的uploadId

说明：

- 该接口最多只会返回1000条记录，如果总数大于1000，请使用request.setPartNumberMarker()方法设置partNumber的起始位置。


### 7.5 完成分块上传
该接口将完成分块上传，把所有的块合并成一个文件。

	public void completaMultipartUpload(){
		List<PartETag> parts = new ArrayList<PartETag>();
		//把上传块时返回的所有要合并的块依次添加进来
		//parts.add(...)
		CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest({bucket},{key},{uploadId}, parts);
		client.completeMultipartUpload(request);
	}

设置回调
	
	//如果用户设置了回调，那么当文件即将上传成功时，KS3将会使用POST的方式调用用户提供的回调地址，如果调用成功且用户处理成功(用户处理成功指返回{"result":true}给KS3，处理失败返回{"result":false})，那么文件才会真正的上传成功，如果调用失败或者用户处理失败，那么文件最终会上传失败。用户可以设置KS3回调时的body，最后KS3将会把用户提供的参数组织成json格式返回给用户。在下面示例中，KS3回调时的body为{"bucket":<实际存储的bucket>,"createTime":<文件创建时间>,"etag":<文件的etag,即文件的MD5经hex处理后的值>,"key":<文件实际保持的key>,"mimType":<文件的Content-Type>,"objectSize":<文件的字节数大小>,"time":"20150222"}
	CallBackConfiguration config = new CallBackConfiguration();
	config.setCallBackUrl("http://10.4.2.38:19090/");//KS3服务器回调的地址
	//以下为KS3服务器访问http://10.4.2.38:19090/时body中的参数
	Map<String,MagicVariables> magicVariables = new HashMap<String,MagicVariables>();
	magicVariables.put("bucket", MagicVariables.bucket);
	magicVariables.put("createTime", MagicVariables.createTime);
	magicVariables.put("etag", MagicVariables.etag);
	magicVariables.put("key", MagicVariables.key);
	magicVariables.put("mimeType", MagicVariables.mimeType);
	magicVariables.put("objectSize", MagicVariables.objectSize);
		
	config.setBodyMagicVariables(magicVariables);
	
	//用户可以自己定义返回的参数。
	Map<String,String> kssVariables = new HashMap<String,String>();
		
	kssVariables.put("time", "20150222");
	request.setCallBackConfiguration(config);

设置异步数据处理任务

	//设置异步数据处理任务,该任务的作用是当文件上传成功后，对上传的文件进行视频转码功能（以下代码中是视频转码，当然还有其	他各种各样的功能），将转码后的视频存储为“野生动物-转码.3gp”，并且将转码结果信息发送到http://10.4.2.38:19090/   。
	List<Adp> adps= new ArrayList<Adp>();
	Adp adp= new Adp();
	//具体command详见：http://ks3.ksyun.com/doc/video/avop.html
	adp.setCommand("tag=avop&f=mp4&res=1280x720&vbr=1000k&abr=128k");
	//处理完成后存储的key，相当于文档中说的tag=saveas&object=....
	adp.setKey("野生动物-转码.3gp");
	adps.add(adp);
	request.setAdps(adps);
	request.setNotifyURL("http://10.4.2.38:19090/");

说明：

- part列表中的partNumber经排序后应该是连续的整数。
- 异步数据处理命令详见：[http://ks3.ksyun.com/doc/video/avop.html](http://ks3.ksyun.com/doc/video/avop.html)

### 7.6 取消分块上传

对于不再使用的分块上传，建议调用该接口取消。  
	
	public void abortMultipartUpload(){
		client.abortMultipartUpload(bucketname, objectkey, uploadId);
	}

## 8 图片处理相关

1、根据[http://ks3.ksyun.com/doc/imghandle/api/index.html](http://ks3.ksyun.com/doc/imghandle/api/index.html)和[http://ks3.ksyun.com/doc/imghandle/imgstyle/index.html](http://ks3.ksyun.com/doc/imghandle/imgstyle/index.html)在原有key的基础上拼接图片处理参数。比如：{原key}@base@tag=imageInfo 或者 {原key}@style@{样式名称}  
2、使用拼接好的key，根据6.8获取文件访问地址即可。

## 9 音视频处理相关

KS3支持用户在上传文件的时候，设置一个异步数据处理任务。当文件上传成功后，KS3将启动该异步数据处理任务。等任务处理完成后，KS3会把处理后的结果上传到用户指定的bucket和key。之后KS3将会使用POST的方式向用户提供的URL发送处理结果，用户接受成功并返回{"result":true}视为通知成功。同时，KS3也支持直接提交异步数据处理任务,详见:9.2。

### 9.1 上传文件以及分块上传文件时触发处理

详见6.2上传文件、7.5 完成分块上传。

### 9.2 对已经存在于KS3上的文件做处理
	
	// 添加异步数据处理任务,该任务的作用是将指定的文件进行视频转码功能（以下代码中是视频转码，当然还有其他各种各样的功能），将转码后的视频存储为“野生动物-转码.3gp”，并且将转码结果信息发送到http://10.4.2.38:19090/
	// 。具体参考API文档，异步数据处理。
	public String putAdp() {
		PutAdpRequest request = new PutAdpRequest("<您的bucket名称>",
				"<要处理的数据的object key>");
		List<Adp> adps = new ArrayList<Adp>();

		Adp adp = new Adp();
		//具体command详见：http://ks3.ksyun.com/doc/video/avop.html
		adp.setCommand("tag=avop&f=mp4&res=1280x720&vbr=1000k&abr=128k");
		//处理完成后存储的key，相当于文档中说的tag=saveas&object=....
		adp.setKey("野生动物-转码.3gp");
		adps.add(adp);

		request.setAdps(adps);
		//处理结果接受服务，具体查看API文档，异步数据处理
		request.setNotifyURL("http://10.4.2.38:19090/");
		//任务id,用于查询任务处理状态
		String id = client.putAdpTask(request).getTaskId();
		return id;
	}

### 9.3 查询任务处理状态

9.1、9.2提到的方法中，如果提交了异步数据处理任务，那么返回的结果中是会有一个taskId的。用户可以使用该taskId查询任务处理状态。

	public AdpTask getAdp(){
		AdpTask task = client.getAdpTask("<TaskID>");
		//0,"task is create fail"、1,"task is create success"、2,"task is processing"、3,"task is process success"、4,"task is process fail"
		task.getProcessstatus();
		//0,"task is not notify"、1,"task is notify success"、2,"task is notify fail"
		task.getNotifystatus();
		
		//查询每条命令的具体执行结果，包括是否执行成功，以及执行成功后存储的key
		task.getAdpInfos();
		
		return task;
	}
	
在浏览器中直接查看：  
http://{endpoint}/{taskId}?queryadp  
比如:http://kss.ksyun.com/{taskId}?queryadp

## 10 通过外链操作  

### 10.1 通过外链访问文件  

	GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest();
	req.setMethod(HttpMethod.GET);
	req.setBucket("<bucket名称>");
	req.setKey("key");
	req.setExpiration(<生成的外链的过期时间>);//unix时间戳，不指定的话则默认为15分钟后过期
	ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
	//overrides.setContentType("application/xml");//设置返回的Content-Type
	req.setResponseHeaders(overrides);
	String url = client.generatePresignedUrl(req);

  
### 10.2 通过外链上传文件  

KS3上传协议详见[http://ks3.ksyun.com/doc/api/object/put.html](http://ks3.ksyun.com/doc/api/object/put.html)   

	GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest();
	req.setMethod(HttpMethod.PUT);
	req.setBucket(bucket);//文件上传的bucket
	req.setKey(key);//文件名
	req.setExpiration(<生成的外链的过期时间>);//不指定的话则默认为15分钟后过期
	req.getRequestConfig().getExtendHeaders().put("x-kss-acl", "public-read");//设置acl为公开读，不加该header则默认为私有，生成外链时设置了header，则在使用外链的时候也需要添加相应的header
	req.setContentType("application/ocet-stream");//设置文件的Content-Type,具体值请根据时间情况设定。在使用外链的时候需要把Content-Type设置成指定的值
	//req.setSseAlgorithm("AES256");//设置服务端加密
	String url = client.generatePresignedUrl(req);
	
当用户拿到该URL之后便可以通过以下请求上传文件(请自行拆分uri和host)  

	PUT /{uri} HTTP/1.1
	Host: {host}
	Date: {当前时间}
	x-kss-acl:public-read //因为在生成外链的时候设置了该header，所以在发送的时候也需要带上
	Content-Type:application/ocet-stream //同上
	Content-Length:100
	[100 bytes of data]

## 11 客户端文件加密
用户可以使用sdk将数据加密后再上传到ks3

### 11.1 环境配置(以JDK7示例)

下载[UnlimitedJCEPolicyJDK7](http://www.oracle.com/technetwork/java/embedded/embedded-se/downloads/jce-7-download-432124.html )，将local_policy.jar和US_export_policy.jar放在{JAVA_HOME}\jre\lib\security目录下，覆盖原有的。

下载[bcprov-jdk15on-152.jar](http://www.bouncycastle.org/latest_releases.html)，放在{JAVA_HOME}\jre\lib\ext目录下  

### 11.2 初始化主秘钥  

使用对称主密钥  

	import java.io.File;
	import java.io.FileInputStream;
	import java.io.FileOutputStream;
	import java.io.IOException;
	import java.security.InvalidKeyException;
	import java.security.NoSuchAlgorithmException;
	import java.security.spec.InvalidKeySpecException;
	import java.security.spec.X509EncodedKeySpec;
	import java.util.Arrays;
	import javax.crypto.KeyGenerator;
	import javax.crypto.SecretKey;
	import javax.crypto.spec.SecretKeySpec;

	import org.junit.Assert;

	public class GenerateSymmetricMasterKey {

	    private static final String keyDir  = System.getProperty("java.io.tmpdir"); 
	    private static final String keyName = "secret.key";
    
	    public static void main(String[] args) throws Exception {
        
	        //Generate symmetric 256 bit AES key.
	        KeyGenerator symKeyGenerator = KeyGenerator.getInstance("AES");
	        symKeyGenerator.init(256); 
	        SecretKey symKey = symKeyGenerator.generateKey();
 
	        //Save key.
	        saveSymmetricKey(keyDir, symKey);
        
	        //Load key.
	        SecretKey symKeyLoaded = loadSymmetricAESKey(keyDir, "AES");           
	        Assert.assertTrue(Arrays.equals(symKey.getEncoded(), symKeyLoaded.getEncoded()));
	    }

	    public static void saveSymmetricKey(String path, SecretKey secretKey) 
	        throws IOException {
	        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
	                secretKey.getEncoded());
	        FileOutputStream keyfos = new FileOutputStream(path + "/" + keyName);
	        keyfos.write(x509EncodedKeySpec.getEncoded());
	        keyfos.close();
	    }
    
	    public static SecretKey loadSymmetricAESKey(String path, String algorithm) 
	        throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException{
	        //Read private key from file.
	        File keyFile = new File(path + "/" + keyName);
	        FileInputStream keyfis = new FileInputStream(keyFile);
	        byte[] encodedPrivateKey = new byte[(int)keyFile.length()];
	        keyfis.read(encodedPrivateKey);
	        keyfis.close(); 

	        //Generate secret key.
	        return new SecretKeySpec(encodedPrivateKey, "AES");
	    }
	}
使用非对称主密钥  

	import static org.junit.Assert.assertTrue;
	import java.io.File;
	import java.io.FileInputStream;
	import java.io.FileOutputStream;
	import java.io.IOException;
	import java.security.KeyFactory;
	import java.security.KeyPair;
	import java.security.KeyPairGenerator;
	import java.security.NoSuchAlgorithmException;
	import java.security.PrivateKey;
	import java.security.PublicKey;
	import java.security.SecureRandom;
	import java.security.spec.InvalidKeySpecException;
	import java.security.spec.PKCS8EncodedKeySpec;
	import java.security.spec.X509EncodedKeySpec;
	import java.util.Arrays;

	public class GenerateAsymmetricMasterKey {
	    private static final String keyDir  = System.getProperty("java.io.tmpdir");
	    private static final SecureRandom srand = new SecureRandom();

	    public static void main(String[] args) throws Exception {
	        // Generate RSA key pair of 1024 bits
	        KeyPair keypair = genKeyPair("RSA", 1024);
	        // Save to file system
	        saveKeyPair(keyDir, keypair);
	        // Loads from file system
	        KeyPair loaded = loadKeyPair(keyDir, "RSA");
	        // Sanity check
	        assertTrue(Arrays.equals(keypair.getPublic().getEncoded(), loaded
                .getPublic().getEncoded()));
	        assertTrue(Arrays.equals(keypair.getPrivate().getEncoded(), loaded
                .getPrivate().getEncoded()));
	    }

	    public static KeyPair genKeyPair(String algorithm, int bitLength)
            throws NoSuchAlgorithmException {
	        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(algorithm);
	        keyGenerator.initialize(1024, srand);
	        return keyGenerator.generateKeyPair();
	    }

	    public static void saveKeyPair(String dir, KeyPair keyPair)
            throws IOException {
	        PrivateKey privateKey = keyPair.getPrivate();
	        PublicKey publicKey = keyPair.getPublic();

	        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                publicKey.getEncoded());
	        FileOutputStream fos = new FileOutputStream(dir + "/public.key");
	        fos.write(x509EncodedKeySpec.getEncoded());
	        fos.close();

	        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                privateKey.getEncoded());
	        fos = new FileOutputStream(dir + "/private.key");
	        fos.write(pkcs8EncodedKeySpec.getEncoded());
	        fos.close();
	    }

	    public static KeyPair loadKeyPair(String path, String algorithm)
            throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException {
	        // read public key from file
	        File filePublicKey = new File(path + "/public.key");
	        FileInputStream fis = new FileInputStream(filePublicKey);
	        byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
	        fis.read(encodedPublicKey);
	        fis.close();
	
	        // read private key from file
	        File filePrivateKey = new File(path + "/private.key");
	        fis = new FileInputStream(filePrivateKey);
	        byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
	        fis.read(encodedPrivateKey);
	        fis.close();

	        // Convert them into KeyPair
	        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
	        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                encodedPublicKey);
	        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

	        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                encodedPrivateKey);
	        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

	        return new KeyPair(publicKey, privateKey);
	    }
	}

### 11.3 初始化客户端

	SecretKey symKey = ??//主密钥
	EncryptionMaterials keyMaterials = new EncryptionMaterials(symKey);
	Ks3  client = new Ks3EncryptionClient("<accesskeyid>","<accesskeysecret>",keyMaterials);

### 11.4 注意事项
1、上传上去的文件是经过加密的。  
2、下载文件只能通过该客户端getObject方法下载，用其他方法下载下来的文件是经过加密的。    
3、分块上传时必须依次上传每一块。当上传最后一块时必须通过request.setLastPart指定最后一块。上传顺序不能错乱，不能使用多线程分块上传。  
4、请妥善保管自己的主密钥，如果主密钥丢失，将无法解密数据。
