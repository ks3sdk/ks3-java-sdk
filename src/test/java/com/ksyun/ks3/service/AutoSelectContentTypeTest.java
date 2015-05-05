package com.ksyun.ks3.service;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ksyun.ks3.config.ClientConfig;
import com.ksyun.ks3.dto.Adp;
import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.dto.HeadObjectResult;
import com.ksyun.ks3.dto.InitiateMultipartUploadResult;
import com.ksyun.ks3.dto.ListPartsResult;
import com.ksyun.ks3.dto.PostObjectFormFields;
import com.ksyun.ks3.request.WithOutContentTypeInitMultipartUploadRequest;
import com.ksyun.ks3.request.WithOutContentTypePutObjectRequest;
import com.ksyun.ks3.service.multipartpost.FormFieldKeyValuePair;
import com.ksyun.ks3.service.multipartpost.HttpPostEmulator;
import com.ksyun.ks3.service.multipartpost.UploadFileItem;
import com.ksyun.ks3.service.request.CompleteMultipartUploadRequest;
import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.service.request.UploadPartRequest;
import com.ksyun.ks3.service.response.InitiateMultipartUploadResponse;
import com.ksyun.ks3.service.response.PutObjectResponse;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年2月4日 上午11:36:01
 * 
 * @description API根据key自动识别content-type
 **/
public class AutoSelectContentTypeTest extends Ks3ClientTest {
	final private String bucketName = "content-type-lijunwei-test";
	Map<String, String> tests = new HashMap<String, String>();

	@Test
	public void postObjectTest() throws Exception {
		for(Entry<String,String> entry:tests.entrySet()){
			//为了节约时间，只做抽查
			if(new Random().nextInt()%9!=0)
				continue;
			String key ="test."+entry.getKey();
			this.postObjectCommon(key);
			
			HeadObjectResult result = client.headObject(bucketName, key);
			assertEquals(entry.getValue(),result.getObjectMetadata().getContentType().split(";")[0]);
			
			GetObjectResult gResult = client.getObject(bucketName,key);
			assertEquals(entry.getValue(),gResult.getObject().getObjectMetadata().getContentType().split(";")[0]);
		}
	}
	@Test
	public void putObjectTest() throws Exception {
		for(Entry<String,String> entry:tests.entrySet()){
			//为了节约时间，只做抽查
			if(new Random().nextInt()%9!=0)
				continue;
			String key ="test."+entry.getKey();
			client.execute(new WithOutContentTypePutObjectRequest(bucketName,key,new ByteArrayInputStream("123456".getBytes()),null),PutObjectResponse.class);
			
			HeadObjectResult result = client.headObject(bucketName, key);
			assertEquals(entry.getValue(),result.getObjectMetadata().getContentType().split(";")[0]);
			
			GetObjectResult gResult = client.getObject(bucketName,key);
			assertEquals(entry.getValue(),gResult.getObject().getObjectMetadata().getContentType().split(";")[0]);
		}
	}
	@Test
	public void putObjectTestForSdk(){
		for(Entry<String,String> entry:tests.entrySet()){
			//为了节约时间，只做抽查
			if(new Random().nextInt()%9!=0)
				continue;
			String key ="test."+entry.getKey();
			client.execute(new PutObjectRequest(bucketName,key,new ByteArrayInputStream("123456".getBytes()),null),PutObjectResponse.class);
			
			HeadObjectResult result = client.headObject(bucketName, key);
			assertEquals(entry.getValue(),result.getObjectMetadata().getContentType().split(";")[0]);
			
			GetObjectResult gResult = client.getObject(bucketName,key);
			assertEquals(entry.getValue(),gResult.getObject().getObjectMetadata().getContentType().split(";")[0]);
		}
	}
	@Test
	public void mulitipartUploadObject() throws Exception{
		for(Entry<String,String> entry:tests.entrySet()){
			//为了节约时间，只做抽查
			if(new Random().nextInt()%9!=0)
				continue;
			String key ="test."+entry.getKey();
			InitiateMultipartUploadResult result = client.execute(new WithOutContentTypeInitMultipartUploadRequest(bucketName,key),InitiateMultipartUploadResponse.class);
			
			UploadPartRequest request = new UploadPartRequest(bucketName,key,result.getUploadId(),1,new ByteArrayInputStream("123456".getBytes()),6);
			client.uploadPart(request);	
			
			ListPartsResult parts = client.listParts(bucketName, key, result.getUploadId());

			CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(parts);
			client.completeMultipartUpload(compRequest);
			
			HeadObjectResult result1 = client.headObject(bucketName, key);
			assertEquals(entry.getValue(),result1.getObjectMetadata().getContentType().split(";")[0]);
			
			GetObjectResult gResult = client.getObject(bucketName,key);
			assertEquals(entry.getValue(),gResult.getObject().getObjectMetadata().getContentType().split(";")[0]);
		}
	}
	@Test
	public void mulitipartUploadObjectForSdk() throws Exception{
		for(Entry<String,String> entry:tests.entrySet()){
			//为了节约时间，只做抽查
			if(new Random().nextInt()%9!=0)
				continue;
			String key ="test."+entry.getKey();
			InitiateMultipartUploadResult result = client.execute(new InitiateMultipartUploadRequest(bucketName,key),InitiateMultipartUploadResponse.class);
			
			UploadPartRequest request = new UploadPartRequest(bucketName,key,result.getUploadId(),1,new ByteArrayInputStream("123456".getBytes()),6);
			client.uploadPart(request);	
			
			ListPartsResult parts = client.listParts(bucketName, key, result.getUploadId());

			CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(parts);
			client.completeMultipartUpload(compRequest);
			
			HeadObjectResult result1 = client.headObject(bucketName, key);
			assertEquals(entry.getValue(),result1.getObjectMetadata().getContentType().split(";")[0]);
			
			GetObjectResult gResult = client.getObject(bucketName,key);
			assertEquals(entry.getValue(),gResult.getObject().getObjectMetadata().getContentType().split(";")[0]);
		}
	}
	private Map<String, List<String>> postObjectCommon(String key)
			throws Exception {
		Map<String, String> postData = new HashMap<String, String>();

		postData.put("key", key);

		PostObjectFormFields fields = client.postObject(bucketName, key,
				postData, null);

		postData.put("policy", fields.getPolicy());
		postData.put("KSSAccessKeyId", fields.getKssAccessKeyId());
		postData.put("signature", fields.getSignature());

		String serverUrl = "http://"+ClientConfig.getConfig().getStr(ClientConfig.END_POINT)+"/" + bucketName;// 上传地址

		// 设定要上传的普通Form Field及其对应的value
		ArrayList<FormFieldKeyValuePair> ffkvp = new ArrayList<FormFieldKeyValuePair>();

		for (Entry<String, String> entry : postData.entrySet()) {
			ffkvp.add(new FormFieldKeyValuePair(entry.getKey(), entry
					.getValue()));
		}

		// 设定要上传的文件
		ArrayList<UploadFileItem> ufi = new ArrayList<UploadFileItem>();
		ufi.add(new UploadFileItem("file", "123456"));

		HttpPostEmulator hpe = new HttpPostEmulator();
		return hpe.sendHttpPostRequest(serverUrl, ffkvp, ufi,
				new HashMap<String, String>());
	}
	@Before
	public void initBucket() {
		if(client.bucketExists(bucketName)){
			client.clearBucket(bucketName);
		}else{
			client.createBucket(bucketName);
		}
		tests.put("unknow", "application/octet-stream");
		tests.put("unknow", "application/octet-stream");
		tests.put("unknow", "application/octet-stream");
		
		tests.put("ez", "application/andrew-inset");
		tests.put("atom", "application/atom+xml");
		tests.put("hqx", "application/mac-binhex40");
		tests.put("cpt", "application/mac-compactpro");
		tests.put("mathml", "application/mathml+xml");
		tests.put("bin", "application/octet-stream");
		tests.put("dms", "application/octet-stream");
		tests.put("lha", "application/octet-stream");
		tests.put("lzh", "application/octet-stream");
		tests.put("exe", "application/octet-stream");
		tests.put("class", "application/octet-stream");
		tests.put("so", "application/octet-stream");
		tests.put("dll", "application/octet-stream");
		tests.put("dmg", "application/octet-stream");
		tests.put("oda", "application/oda");
		tests.put("ogg", "application/ogg");
		tests.put("pdf", "application/pdf");
		tests.put("ai", "application/postscript");
		tests.put("eps", "application/postscript");
		tests.put("ps", "application/postscript");
		tests.put("rdf", "application/rdf+xml");
		tests.put("smi", "application/smil");
		tests.put("smil", "application/smil");
		tests.put("gram", "application/srgs");
		tests.put("grxml", "application/srgs+xml");
		tests.put("xls", "application/vnd.ms-excel");
		tests.put("ppt", "application/vnd.ms-powerpoint");
		tests.put("rm", "application/vnd.rn-realmedia");
		tests.put("wbxml", "application/vnd.wap.wbxml");
		tests.put("wmlc", "application/vnd.wap.wmlc");
		tests.put("wmlsc", "application/vnd.wap.wmlscriptc");
		tests.put("vxml", "application/voicexml+xml");
		tests.put("bcpio", "application/x-bcpio");
		tests.put("vcd", "application/x-cdlink");
		tests.put("pgn", "application/x-chess-pgn");
		tests.put("cpio", "application/x-cpio");
		tests.put("csh", "application/x-csh");
		tests.put("dcr", "application/x-director");
		tests.put("dir", "application/x-director");
		tests.put("dxr", "application/x-director");
		tests.put("dvi", "application/x-dvi");
		tests.put("spl", "application/x-futuresplash");
		tests.put("gtar", "application/x-gtar");
		tests.put("gz", "application/x-gzip");
		tests.put("hdf", "application/x-hdf");
		tests.put("js", "application/x-javascript");
		tests.put("jnlp", "application/x-java-jnlp-file");
		tests.put("skp", "application/x-koan");
		tests.put("skd", "application/x-koan");
		tests.put("skt", "application/x-koan");
		tests.put("skm", "application/x-koan");
		tests.put("latex", "application/x-latex");
		tests.put("nc", "application/x-netcdf");
		tests.put("cdf", "application/x-netcdf");
		tests.put("sh", "application/x-sh");
		tests.put("shar", "application/x-shar");
		tests.put("swf", "application/x-shockwave-flash");
		tests.put("sit", "application/x-stuffit");
		tests.put("sv4cpio", "application/x-sv4cpio");
		tests.put("sv4crc", "application/x-sv4crc");
		tests.put("tar", "application/x-tar");
		tests.put("tcl", "application/x-tcl");
		tests.put("tex", "application/x-tex");
		tests.put("texinfo", "application/x-texinfo");
		tests.put("texi", "application/x-texinfo");
		tests.put("t", "application/x-troff");
		tests.put("tr", "application/x-troff");
		tests.put("roff", "application/x-troff");
		tests.put("man", "application/x-troff-man");
		tests.put("me", "application/x-troff-me");
		tests.put("ms", "application/x-troff-ms");
		tests.put("ustar", "application/x-ustar");
		tests.put("src", "application/x-wais-source");
		tests.put("xhtml", "application/xhtml+xml");
		tests.put("xht", "application/xhtml+xml");
		tests.put("xslt", "application/xslt+xml");
		tests.put("xml", "application/xml");
		tests.put("xsl", "application/xml");
		tests.put("dtd", "application/xml-dtd");
		tests.put("zip", "application/zip");
		tests.put("au", "audio/basic");
		tests.put("snd", "audio/basic");
		tests.put("mid", "audio/midi");
		tests.put("midi", "audio/midi");
		tests.put("kar", "audio/midi");
		tests.put("m4a", "audio/mp4a-latm");
		tests.put("m4p", "audio/mp4a-latm");
		tests.put("mpga", "audio/mpeg");
		tests.put("mp2", "audio/mpeg");
		tests.put("mp3", "audio/mpeg");
		tests.put("aif", "audio/x-aiff");
		tests.put("aiff", "audio/x-aiff");
		tests.put("aifc", "audio/x-aiff");
		tests.put("m3u", "audio/x-mpegurl");
		tests.put("ram", "audio/x-pn-realaudio");
		tests.put("ra", "audio/x-pn-realaudio");
		tests.put("wav", "audio/x-wav");
		tests.put("pdb", "chemical/x-pdb");
		tests.put("xyz", "chemical/x-xyz");
		tests.put("bmp", "image/bmp");
		tests.put("cgm", "image/cgm");
		tests.put("gif", "image/gif");
		tests.put("ief", "image/ief");
		tests.put("jpeg", "image/jpeg");
		tests.put("jpg", "image/jpeg");
		tests.put("jpe", "image/jpeg");
		tests.put("jp2", "image/jp2");
		tests.put("pict", "image/pict");
		tests.put("pic", "image/pict");
		tests.put("pct", "image/pict");
		tests.put("png", "image/png");
		tests.put("svg", "image/svg+xml");
		tests.put("tiff", "image/tiff");
		tests.put("tif", "image/tiff");
		tests.put("djvu", "image/vnd.djvu");
		tests.put("djv", "image/vnd.djvu");
		tests.put("wbmp", "image/vnd.wap.wbmp");
		tests.put("ras", "image/x-cmu-raster");
		tests.put("pntg", "image/x-macpaint");
		tests.put("pnt", "image/x-macpaint");
		tests.put("mac", "image/x-macpaint");
		tests.put("ico", "image/x-icon");
		tests.put("pnm", "image/x-portable-anymap");
		tests.put("pbm", "image/x-portable-bitmap");
		tests.put("pgm", "image/x-portable-graymap");
		tests.put("ppm", "image/x-portable-pixmap");
		tests.put("qtif", "image/x-quicktime");
		tests.put("qti", "image/x-quicktime");
		tests.put("rgb", "image/x-rgb");
		tests.put("xbm", "image/x-xbitmap");
		tests.put("xpm", "image/x-xpixmap");
		tests.put("xwd", "image/x-xwindowdump");
		tests.put("igs", "model/iges");
		tests.put("iges", "model/iges");
		tests.put("msh", "model/mesh");
		tests.put("mesh", "model/mesh");
		tests.put("silo", "model/mesh");
		tests.put("wrl", "model/vrml");
		tests.put("vrml", "model/vrml");
		tests.put("ics", "text/calendar");
		tests.put("ifb", "text/calendar");
		tests.put("css", "text/css");
		tests.put("html", "text/html");
		tests.put("htm", "text/html");
		tests.put("asc", "text/plain");
		tests.put("txt", "text/plain");
		tests.put("rtx", "text/richtext");
		tests.put("rtf", "text/rtf");
		tests.put("sgml", "text/sgml");
		tests.put("sgm", "text/sgml");
		tests.put("tsv", "text/tab-separated-values");
		tests.put("wml", "text/vnd.wap.wml");
		tests.put("wmls", "text/vnd.wap.wmlscript");
		tests.put("etx", "text/x-setext");
		tests.put("3gp", "video/3gpp");
		tests.put("mp4", "video/mp4");
		tests.put("mpeg", "video/mpeg");
		tests.put("mpg", "video/mpeg");
		tests.put("mpe", "video/mpeg");
		tests.put("ogv", "video/ogv");
		tests.put("qt", "video/quicktime");
		tests.put("mov", "video/quicktime");
		tests.put("mxu", "video/vnd.mpegurl");
		tests.put("m4u", "video/vnd.mpegurl");
		tests.put("webm", "video/webm");
		tests.put("dv", "video/x-dv");
		tests.put("dif", "video/x-dv");
		tests.put("flv", "video/x-flv");
		tests.put("m4v", "video/x-m4v");
		tests.put("wmv", "video/x-ms-wmv");
		tests.put("avi", "video/x-msvideo");
		tests.put("movie", "video/x-sgi-movie");
		tests.put("ice", "x-conference/x-cooltalk");
	}

}
