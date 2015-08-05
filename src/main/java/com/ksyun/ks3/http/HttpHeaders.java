package com.ksyun.ks3.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.ksyun.ks3.config.Constants;

/**
 * * @author lijunwei[lijunwei@kingsoft.com]   * * @date 2014年10月15日 下午1:49:51 *
 * * @description
 **/
public enum HttpHeaders {
	RequestId("request-id"),
	Authorization("Authorization", false), 
	Date("Date", false), 
	Host("Host", false), 
	Server("Server", false),
	UserAgent("User-Agent", false),
	ExtendedRequestId("id-2"), 
	ContentMD5("Content-MD5", false), 
	Expect("Expect", false),
	IfMatch("If-Match",false), 
	IfNoneMatch("If-None-Match", false), 
	IfModifiedSince("If-Modified-Since", false), 
	IfUnmodifiedSince("If-Unmodified-Since", false), 
	XApplicationContext("X-Application-Context", false), 
	XNoReferer("X-No-Referer", false), 
	XBlackList("X-Black-List", false), 
	XWhiteList("X-White-List", false), 
	XKssOp("Op", false),
	// put object metadata
	ContentLength("Content-Length", false), 
	CacheControl("Cache-Control", false), 
	ContentType("Content-Type", false), 
	ContentDisposition("Content-Disposition",false), 
	ContentEncoding("Content-Encoding", false), 
	Expires("Expires", false), 
	Range("Range", false), 
	ContentRange("Content-Range", false),
	//
	AcceptRanges("Accept-Ranges", false), 
	Connection("Connection", false),
	// acl
	CannedAcl("acl"), 
	AclPrivate("acl-private"),
	AclPubicRead("acl-public-read"),
	AclPublicReadWrite("acl-public-write"),
	AclPublicAuthenticatedRead("acl-public-authenticated-read"), 
	GrantFullControl("grant-full-control"),
	GrantRead("grant-read"), 
	GrantWrite("grant-write"),
	//
	ETag("ETag", false), 
	LastModified("Last-Modified", false),
	// get object response
	/** *default false */
	XKssDeleteMarker("delete-marker"), 
	XKssExpiration("expiration"), /** * 服务端加密 */
	XKssServerSideEncryption("server-side-encryption"), 
	XKssServerSideEncryptionKMSKeyId("server-side-encryption-kss-kms-key-id","server-side-encryption-aws-kms-key-id"), 
	XKssServerSideEncryptionCustomerAlgorithm("server-side-encryption-customer-algorithm"),
	XkssServerSideEncryptionCustomerKey("server-side-encryption-customer-key"),
	XkssServerSideEncryptionCustomerKeyMD5("server-side-encryption-customer-key-MD5"),
	XKssCPSourceServerSideEncryptionCustomerAlgorithm("copy-source-server-side-encryption-customer-algorithm"), 
	XkssCPSourceServerSideEncryptionCustomerKey("copy-source-server-side-encryption-customer-key"),
	XkssCPSourceServerSideEncryptionCustomerKeyMD5("copy-source-server-side-encryption-customer-key-MD5"), /**
	 * * default
	 * None
	 */
	XKssRestore("restore"), /** * default None */
	XKssWebsiteRedirectLocation("website-redirect-location"), 
	XKssCopySource("copy-source"),
	XKssCopySourceRange("copy-source-range"), /**
	 * *
	 * callback
	 */
	XKssCallbackUrl("callbackurl"), 
	XKssCallbackBody("callbackbody"), 
	AsynchronousProcessingList("kss-async-process", false), 
	NotifyURL("kss-notifyurl", false),
	TaskId("TaskID", false), 
	/** * 客户端加密 */
	CRYPTO_KEY("key"), 
	CRYPTO_KEY_V2("key-v2"), 
	CRYPTO_IV("iv"), 
	MATERIALS_DESCRIPTION("matdesc"),
	UNENCRYPTED_CONTENT_MD5("unencrypted-content-md5"),
	UNENCRYPTED_CONTENT_LENGTH("unencrypted-content-length"), 
	CRYPTO_INSTRUCTION_FILE("crypto-instr-file"), 
	CRYPTO_CEK_ALGORITHM("cek-alg"), 
	CRYPTO_TAG_LENGTH("tag-len"),
	CRYPTO_KEYWRAP_ALGORITHM("wrap-alg");
	private String value;
	private String value2;
	private boolean isSpecHeader;

	HttpHeaders(String value) {
		this(value, value, true);
	}

	HttpHeaders(String value, boolean isSpecHeader) {
		this(value, value, isSpecHeader);
	}

	HttpHeaders(String value, String value2) {
		this(value, value2, true);
	}

	HttpHeaders(String value, String value2, boolean isSpecHeader) {
		this.value = value;
		this.value2 = value2;
		this.isSpecHeader = isSpecHeader;
	}

	@Override
	public String toString() {
		String prefix = "";
		if (isSpecHeader)
			prefix = Constants.specHeaderPrefix;
		String value = this.value;
		if (Constants.aws)
			value = this.value2;
		return prefix + value;
	}
	public static void main(String [] args){
		HttpHeaders header = HttpHeaders.XkssServerSideEncryptionCustomerKeyMD5;
		byte [] bytes = header.toString().getBytes();
		int i = 0;
		for(byte b : bytes){
			System.out.println(header.toString().charAt(i)+"-"+b);
			i++;
		}
	}
}