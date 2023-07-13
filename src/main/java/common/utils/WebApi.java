package common.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

public class WebApi {
	//request
	String url;
	String method;
    static class Proxy {
    	String host;
    	String port;
    	String user;
    	String password;
    }
    static Proxy proxy = new WebApi.Proxy();
	String[] header_key = new String[5];
	String[] header_value = new String[5];
	int headerCnt = 0;
	
	//response
    int responseCode;
    String responseMessage;
    StringBuffer responseBody;
    String responseStr;
    JsonNode responseJson;
   
    public static class FormData {
    	public String userId;
    	public String documentId;
    	public String docsetName;
    	
    	public String sorterRuleId;
    	public String runSortingFlag;
    	public String sendOcrFlag;
    	
    	public String file;
    }
    //FormData formData = new WebApi.FormData();	//for Upload
    FormData formData;
    
    public WebApi() {
	}

	public FormData getFormData() {return formData;}
	public String getResponseStr() {return this.responseStr;}
	public JsonNode getResponseJson() {return this.responseJson;}

	public void setFormData(FormData formData) {
		this.formData = formData;
	}
	
	public void setUrl(String url) {
    	this.url = url;
	}

	public void setMethod(String method) {
    	this.method = method;
	}
	
	public void setUrl(String method, String url) {
    	this.method = method;
    	this.url = url;
	}
	
	public void setProxy(String host, String port, String user, String password) {
    	WebApi.proxy.host = host;
    	WebApi.proxy.port = port;
    	WebApi.proxy.user = user;
    	WebApi.proxy.password = password;
	}
	
    public void putRequestHeader(String key, String value) {
		this.header_key[headerCnt] = key;
		this.header_value[headerCnt] = value;
    	headerCnt++;
    }

	public void setResponseJson(JsonNode jnode) {
		this.responseJson = jnode;
	}
	
    //https://chivsp.hatenablog.com/entry/2022/10/03/090000
    //https://qiita.com/akane_kato/items/34b408336f4ec372b139
	public int upload(int type) throws IOException {
		if (this.formData.file == null)
            return -1;
    	String CRLF = "\r\n";
    	File file = new File(this.formData.file);
    	HttpURLConnection con = null;

    	try {
    		//http://www.mwsoft.jp/programming/java/http_proxy.html
    		if (WebApi.proxy.host != null && WebApi.proxy.host.equals("") != true) {
                System.setProperty("proxySet", "true");
                System.setProperty("proxyHost", WebApi.proxy.host);
                System.setProperty("proxyPort", WebApi.proxy.port);
                Authenticator.setDefault(new Authenticator() {
					@Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(WebApi.proxy.user, WebApi.proxy.password.toCharArray());
                    }
                });
    		}
    		
			//this.url = "http://httpbin.org/post";	//for upload debug
    	    con = (HttpURLConnection) new URL(this.url).openConnection();
    	    con.setRequestMethod("POST");
    	    con.setDoOutput(true);
            System.out.println("url: " + this.url + " method: " + this.method);

            //add request header
            for (int i=0; i<5; i++) {
            	if (this.header_key[i] == null) 
            		break;
        		con.setRequestProperty(this.header_key[i], this.header_value[i]);
                System.out.println("req header: " + this.header_key[i] + ":" + this.header_value[i]);
            }

            final String boundary = UUID.randomUUID().toString();
            con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            System.out.println("req header: Content-Type header_value:multipart/form-data");
    	    DataOutputStream request = new DataOutputStream(con.getOutputStream());
    	    request.writeBytes("--" + boundary + CRLF);
    	    request.writeBytes("Content-Disposition: form-data; name=\"userId\"" + CRLF + CRLF + this.formData.userId + CRLF);
    	    System.out.println("form-data: userId: " + this.formData.userId);
    	    if (type == 0) {
	    	    request.writeBytes("--" + boundary + CRLF);
	    	    request.writeBytes("Content-Disposition: form-data; name=\"documentId\"" + CRLF + CRLF + this.formData.documentId + CRLF);
	    	    System.out.println("form-data: documentId: " + this.formData.documentId);
    	    } else {
	    	    request.writeBytes("--" + boundary + CRLF);
	    	    request.writeBytes("Content-Disposition: form-data; name=\"sorterRuleId\"" + CRLF + CRLF + this.formData.sorterRuleId + CRLF);
	    	    System.out.println("form-data: sorterRuleId: " + this.formData.sorterRuleId);
	    	    request.writeBytes("--" + boundary + CRLF);
	    	    request.writeBytes("Content-Disposition: form-data; name=\"runSortingFlag\"" + CRLF + CRLF + this.formData.runSortingFlag + CRLF);
	    	    System.out.println("form-data: Disposition: " + this.formData.runSortingFlag);
	    	    request.writeBytes("--" + boundary + CRLF);
	    	    request.writeBytes("Content-Disposition: form-data; name=\"sendOcrFlag\"" + CRLF + CRLF + this.formData.sendOcrFlag + CRLF);
	    	    System.out.println("form-data: sendOcrFlag: " + this.formData.sendOcrFlag);
    	    }
    	    request.writeBytes("--" + boundary + CRLF);
            request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + CRLF + CRLF);
    	    System.out.println("form-data: filename: " + file.getName());
    	    // ファイルをbyte配列に変換
    	    byte[] fileByte = Files.readAllBytes(file.toPath());
    	    request.write(fileByte);
    	    request.writeBytes(CRLF);
    	    request.writeBytes("--" + boundary + "--" + CRLF);
    	    request.flush();
    	    request.close();

            //レスポンスコード/メッセージの取得
            this.responseCode = con.getResponseCode();
            this.responseMessage = con.getResponseMessage();
			System.out.println("Response Code : " + this.responseCode);
	
    	    if (this.responseCode == HttpURLConnection.HTTP_OK) {
                //レスポンスボディの読み出し	正常系の場合はgetInputStream
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                this.responseBody = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    this.responseBody.append(inputLine);
                    System.out.println(inputLine.toString());
                }
                in.close();
                
                //resuponse body
                this.responseStr = this.responseBody.toString();
                //System.out.println(this.responseStr);
    	    } 

            return this.responseCode;

    	} catch (Exception e) {
    	    e.printStackTrace();
            return -1;
    	} finally {
    	    if(con != null) {
    	        con.disconnect();
    	    }
    	}     
    }

	public int sendRequest() throws Exception {

		//http://www.mwsoft.jp/programming/java/http_proxy.html
		HttpURLConnection con = null;
		if (WebApi.proxy.host != null) {
            System.setProperty("proxySet", "true");
            System.setProperty("proxyHost", WebApi.proxy.host);
            System.setProperty("proxyPort", WebApi.proxy.port);
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(WebApi.proxy.user, WebApi.proxy.password.toCharArray());
                }
            });
		}
		
        con = (HttpURLConnection) new URL(this.url).openConnection();
        con.setRequestMethod(this.method);
        if (this.method.equals("POST") == true)
        	con.setDoOutput(true);
		System.out.println("url: " + this.url + " method: " + this.method);

        //add request header
        for (int i=0; i<5; i++) {
            if (this.header_key[i] == null) 
        		break;
    		con.setRequestProperty(this.header_key[i], this.header_value[i]);
			System.out.println("req header: " + this.header_key[i] + ":" + this.header_value[i]);
        }

        this.responseCode = con.getResponseCode();
        System.out.println("Response Code : " + this.responseCode);
        
        if (this.responseCode == HttpURLConnection.HTTP_OK) {
            //レスポンスボディの読み出し
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            this.responseBody = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                this.responseBody.append(inputLine);
            }
            in.close();

            //resuponse body
            this.responseStr = this.responseBody.toString();
            System.out.println(this.responseStr);
		} 
	    
        return this.responseCode;
    }

	public int download(String saveFile) throws IOException {
		if (saveFile == null)
            return -1;
		
		try {
			HttpURLConnection con = null;
			//http://www.mwsoft.jp/programming/java/http_proxy.html
    		if (WebApi.proxy.host != null) {
                System.setProperty("proxySet", "true");
                System.setProperty("proxyHost", WebApi.proxy.host);
                System.setProperty("proxyPort", WebApi.proxy.port);
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(WebApi.proxy.user, WebApi.proxy.password.toCharArray());
                    }
                });
    		}

			con = (HttpURLConnection) new URL(this.url).openConnection();
			// false の場合、ユーザーとの対話処理は許可されていません。
			con.setAllowUserInteraction(false);
			// true の場合、プロトコルは自動的にリダイレクトに従います
			con.setInstanceFollowRedirects(true);
			// URL 要求のメソッドを"GET"に設定
			con.setRequestMethod("GET");
            System.out.println("url: " + this.url + " method: " + this.method);

	        //add request header
	        for (int i=0; i<5; i++) {
            	if (this.header_key[i] == null) 
	        		break;
                con.setRequestProperty(this.header_key[i], this.header_value[i]);
                System.out.println("req header: " + this.header_key[i] + ":" + this.header_value[i]);
	        }
			
			con.connect();
			
            //レスポンスコード/メッセージの取得
            this.responseCode = con.getResponseCode();
            this.responseMessage = con.getResponseMessage();
			System.out.println("Response Code : " + this.responseCode);
			System.out.println("Response Message : " + this.responseMessage);

    	    if (this.responseCode == HttpURLConnection.HTTP_OK) {
                //レスポンスボディの読み出し	正常系の場合はgetInputStream
				//レスポンスボディの読み出し
				writeStream(con.getInputStream(), saveFile);
				System.out.println("Download completed!!");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return this.responseCode;
	}
	
	private void writeStream(InputStream inputStream, String outputPath) throws Exception {
		final int BufferSize = 4096;
		
		int availableByteNumber;
		byte[] buffers = new byte[BufferSize];
		try (DataInputStream dataInputStream = new DataInputStream(inputStream);
		      DataOutputStream outputStream = new DataOutputStream(
		            new BufferedOutputStream(new FileOutputStream(outputPath)))) {
		   while ((availableByteNumber = dataInputStream.read(buffers)) > 0) {
		      outputStream.write(buffers, 0, availableByteNumber);
		   }
		} catch (Exception ex) {
		   throw ex;
		}
	}
}
