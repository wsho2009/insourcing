package common.utils;

import java.net.HttpURLConnection;

public class TestWebApi {

	public static void main(String[] args) {

		//---------------------------------------
		//HTTP request parametes
		//---------------------------------------
		WebApi api = new WebApi();
		String  url = "http://httpbin.org/get";	//for upload debug
		//String  url = OCR_HOST_URL + String.format(OCR_READ_UNIT, ocrData.unitId);
		api.setUrl("GET", url);
		//api.setProxy(PROXY_HOST, PROXY_PORT, PROXY_USER, PROXY_PASSWORD);
		//api.putRequestHeader(OCR_API_KEY, OCR_API_KEY_VALUE);
		api.putRequestHeader("documentId", "ドキュメント");

		//---------------------------------------
		//HTTP request process
		//---------------------------------------
		int res;
		try {
			res = api.sendRequest();
		} catch (Exception e) {
			e.printStackTrace();
			res = -1;
		}

		//---------------------------------------
		//HTTP response process
		//---------------------------------------
        if (res != HttpURLConnection.HTTP_OK) {
	        MyUtils.SystemErrPrint("HTTP Connection Failed " + res);
	        return;
        } 
/*
		try {
			ObjectMapper mapper = new ObjectMapper();
			api.responseJson = mapper.readTree(api.responseStr);
			
			String status = api.responseJson.get("status").asText();
			int errorCode = api.responseJson.get("errorCode").asInt();
			String message = api.responseJson.get("message").asText();
			MyUtils.SystemLogPrint("  status: " + status + "  " + message);
			
			String unitId = api.responseJson.get("readingUnits").get(0).get("id").asText();
			String unitName = api.responseJson.get("readingUnits").get(0).get("name").asText();
			String unitStatus = api.responseJson.get("readingUnits").get(0).get("status").asText();
			String docsetId = api.responseJson.get("readingUnits").get(0).get("docsetId").asText();
			String csvFileName = api.responseJson.get("readingUnits").get(0).get("csvFileName").asText();
			String documentId = api.responseJson.get("readingUnits").get(0).get("documentId").asText();
			String documentName = api.responseJson.get("readingUnits").get(0).get("documentName").asText();	//全角は文字化けする。
			String createdAt = api.responseJson.get("readingUnits").get(0).get("createdAt").asText();

		} catch (Exception e) {
			e.printStackTrace();
		}
*/
		MyUtils.SystemLogPrint("■searchReadingUnit: end");
    	return;

	}

}
