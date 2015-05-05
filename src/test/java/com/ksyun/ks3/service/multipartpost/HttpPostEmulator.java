package com.ksyun.ks3.service.multipartpost;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpPostEmulator {
	// 每个post参数之间的分隔。随意设定，只要不会和其他的字符串重复即可。
	private static final String BOUNDARY = "----------HV2ymHFg03ehbqgZCaKO6jyH";

	public Map<String,List<String>> sendHttpPostRequest(String serverUrl,
			ArrayList<FormFieldKeyValuePair> generalFormFields,
			ArrayList<UploadFileItem> filesToBeUploaded,Map<String,String> headers) throws Exception {

		// 向服务器发送post请求

		URL url = new URL(serverUrl/* "http://127.0.0.1:8080/test/upload" */);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		// 发送POST请求必须设置如下两行

		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Charset", "UTF-8");
		connection.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + BOUNDARY);
		
		for(Entry<String,String> head:headers.entrySet()){
			connection.setRequestProperty(head.getKey(),head.getValue());
		}

		// 头

		String boundary = BOUNDARY;

		// 传输内容

		StringBuffer contentBody = new StringBuffer("--" + BOUNDARY);

		// 尾

		String endBoundary = "\r\n--" + boundary + "--\r\n";

		OutputStream out = connection.getOutputStream();

		// 1. 处理文字形式的POST请求

		for (FormFieldKeyValuePair ffkvp : generalFormFields)

		{

			contentBody.append("\r\n")

			.append("Content-Disposition: form-data; name=\"")

			.append(ffkvp.getKey() + "\"")

			.append("\r\n")

			.append("\r\n")

			.append(ffkvp.getValue())

			.append("\r\n")

			.append("--")

			.append(boundary);

		}

		String boundaryMessage1 = contentBody.toString();

		out.write(boundaryMessage1.getBytes("utf-8"));

		// 2. 处理文件上传

		for (UploadFileItem ufi : filesToBeUploaded)

		{

			contentBody = new StringBuffer();

			contentBody.append("\r\n")

			.append("Content-Disposition:form-data; name=\"")

			.append(ufi.getFormFieldName() + "\"; ") // form中field的名称

					.append("filename=\"")

					.append(ufi.getFileName() + "\"") // 上传文件的文件名，包括目录

					.append("\r\n")

					.append("Content-Type:application/octet-stream")

					.append("\r\n\r\n");

			String boundaryMessage2 = contentBody.toString();

			out.write(boundaryMessage2.getBytes("utf-8"));

			// 开始真正向服务器写文件
			InputStream stream = null;
			int length = 0;
			String name = ufi.getFileName();
			try{
				File file = new File(ufi.getFileName());
				stream = new FileInputStream(file);
				length = (int) file.length();
			}
			catch(Exception e){
				stream = new ByteArrayInputStream(name.getBytes());
				length = name.length();
			}
		

			DataInputStream dis = new DataInputStream(stream);

			int bytes = 0;

			byte[] bufferOut = new byte[length];

			bytes = dis.read(bufferOut);

			out.write(bufferOut, 0, bytes);

			dis.close();

			contentBody = new StringBuffer();
			
			contentBody.append("\r\n------------HV2ymHFg03ehbqgZCaKO6jyH");

			String boundaryMessage = contentBody.toString();

			out.write(boundaryMessage.getBytes("utf-8"));

			// System.out.println(boundaryMessage);

		}

		// 3. 写结尾

		out.write(endBoundary.getBytes("utf-8"));

		out.flush();

		out.close();

		// 4. 从服务器获得回答的内容

		String strLine = "";

		String strResponse = "";

		InputStream in = connection.getInputStream();

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		while ((strLine = reader.readLine()) != null)

		{

			strResponse += strLine + "\n";

		}

		// System.out.print(strResponse);

		return connection.getHeaderFields();

	}

}