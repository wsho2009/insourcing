package common.ocr;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.poi.ss.usermodel.CellType;

import com.example.demo.InsourcingConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.fax.FaxDataDAO;
import common.fax.FaxScanFile;
import common.utils.MyExcel;
import common.utils.MyFiles;
import common.utils.MyUtils;
import common.utils.WebApi;

public class OcrProcess {
	static InsourcingConfig config;
	static FaxScanFile scan2;
	static String PROXY_HOST;
	static String PROXY_PORT;
	static String PROXY_USER;
	static String PROXY_PASSWORD;
	static String OCR_HOST_URL;
	static String OCR_API_KEY;
	static String OCR_API_KEY_VALUE;
	static String OCR_USER_ID;
	static String OCR_OUTPUT_PATH;
	static String OCR_UPLOAD_PATH;
	static String OCR_ADD_PAGE;
	static String OCR_ADD_SORT;
	static String OCR_READ_SORT;
	static String OCR_READ_UNIT;
	static String OCR_UNIT_EXPORT;
	static String OCR_LINK_ENTRY;
	static String MAIL_HOST;
	static String MAIL_PORT;
	static String MAIL_USERNAME;
	static String MAIL_PASSWORD;
	static String MAIL_SMTP_AUTH;
	static String MAIL_SMTP_STARTTLS_ENABLE;
	static String MAIL_FROM;
	static String CURRENT_PATH;
	static String SCAN_CLASS1;
	static String SCAN_CLASS2;
	static String SCAN_TARGET_PATH1;
	static String SCAN_TARGET_PATH2;

	public OcrProcess(InsourcingConfig arg_config, FaxScanFile arg_scan) {
    	MyUtils.SystemLogPrint("■OcrProcessコンストラクタ");
		config = arg_config;
		scan2 = arg_scan;
		PROXY_HOST = config.getProxyHost();
		PROXY_PORT = config.getProxyPort();
		PROXY_USER = config.getProxyUsername();
		PROXY_PASSWORD = config.getProxyPassword();
		
		OCR_HOST_URL = config.getOcrHostUrl();
		OCR_API_KEY = config.getOcrApiKey();
		OCR_API_KEY_VALUE = config.getOcrApiKeyValye();
		OCR_USER_ID = config.getOcrUserId();
		OCR_UPLOAD_PATH = config.getOcrUploadPath();
		OCR_OUTPUT_PATH = config.getOcrOutputPath();
		OCR_ADD_PAGE = config.getOcrAddPage();
		OCR_ADD_SORT = config.getOcrAddSort();
		OCR_READ_UNIT = config.getOcrReadUnit();
		OCR_READ_SORT = config.getOcrReadSort();
		OCR_UNIT_EXPORT = config.getOcrUnitExport();
		OCR_LINK_ENTRY = config.getOcrLinkEntry();
		
		MAIL_HOST = config.getMailHost();
		MAIL_PORT = config.getMailPort();
		MAIL_USERNAME = config.getMailUsername();
		MAIL_PASSWORD = config.getMailPassword();
		MAIL_SMTP_AUTH = config.getMailSmtpAuth();
		MAIL_SMTP_STARTTLS_ENABLE = config.getMailSmtpStarttlsEnable();

		SCAN_CLASS1 = config.getScanDefTgt1();
		SCAN_CLASS2 = config.getScanDefTgt2();
		SCAN_TARGET_PATH1 = config.getScanPath1();
		SCAN_TARGET_PATH2 = config.getScanPath2();
		
		CURRENT_PATH = MyFiles.getCurrentPath();	//カレントパス取得
	}
	
	//---------------------------------
	//読取ユニット検索(POLLING)
	//---------------------------------
	public void pollingReadingUnit() {
		//MyUtils.SystemLogPrint("■pollingReadingUnit: start");
		try {
			String type = "0";
			ArrayList<OcrDataFormBean> list = OcrDataFormDAO.getInstance(config).queryNotComplete(type);
			int count = list.size();
			if (count != 0) {
				MyUtils.SystemLogPrint("  find data: " + count);
			}
			for (int o=0; o<count; o++) {
				OcrDataFormBean ocrDataForm = (OcrDataFormBean)list.get(o);
				setTargetPath(ocrDataForm);
				if (ocrDataForm.status.equals("REGIST") == true) {
					MyUtils.SystemLogPrint("  " + o + " addReadingPage");
					addReadingPage(ocrDataForm);
					break;
				} else if (ocrDataForm.status.equals("REGISTED") == true) {
					MyUtils.SystemLogPrint("  " + o + " searchReadingUnit");
					searchReadingUnit(ocrDataForm, false);
				} else if (ocrDataForm.status.equals("OCR") == true || ocrDataForm.status.equals("ENTRY") == true) {
					MyUtils.SystemLogPrint("  " + o + " processReadingUnit");
					processReadingUnit(ocrDataForm);
				
					//ocrDataForm.outFolderPath = getTgtFolderPath(ocrDataForm);
					//debug_copyCsvFile(ocrDataForm.outFolderPath + "\\",  ocrDataForm.csvFileName);
					//convertCSV(ocrDataForm);		//for debug
					//postOcrProcess(ocrDataForm);	//for debug
					//exportResultCSV(ocrDataForm);
				} else if (ocrDataForm.status.equals("SORT") == true) {
					MyUtils.SystemLogPrint("  " + o + " addSortingPage");
					addSortingPage(ocrDataForm);	//ocrDataForm.documentName, ocrDataForm.documentId, ocrDataForm.uploadFilePath
					break;
				} else if (ocrDataForm.status.equals("SORTED") == true) {
					MyUtils.SystemLogPrint("  " + o + " searchSortingUnit");
					searchSortingUnit(ocrDataForm);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//MyUtils.SystemLogPrint("■pollingReadingUnit: end");
	}

	private void setTargetPath(OcrDataFormBean ocrDataForm) {
		if (ocrDataForm.docsetName == null)	//暫定
			ocrDataForm.targetPath = SCAN_TARGET_PATH2;
		else if (ocrDataForm.docsetName.equals(SCAN_CLASS1)==true) {
			ocrDataForm.targetPath = SCAN_TARGET_PATH1;
		} else if (ocrDataForm.docsetName.equals(SCAN_CLASS2)==true) {
			ocrDataForm.targetPath = SCAN_TARGET_PATH2;
		} else {	//暫定
			ocrDataForm.targetPath = SCAN_TARGET_PATH2;
		}
	}

	//---------------------------------------
	//読取ページ追加（処理）
	//---------------------------------------
	private int addReadingPage(OcrDataFormBean ocrData) {
		MyUtils.SystemLogPrint("■addReadingPage: start: " + ocrData.uploadFilePath);
		//---------------------------------------
		//HTTP request parametes
		//---------------------------------------
		WebApi api = new WebApi();
		api.setUrl("POST", OCR_HOST_URL + OCR_ADD_PAGE);
		api.setProxy(PROXY_HOST, PROXY_PORT, PROXY_USER, PROXY_PASSWORD);
		api.putRequestHeader(OCR_API_KEY, OCR_API_KEY_VALUE);
		//---------------------------------------
		WebApi.FormData formData = new WebApi.FormData();
		formData.userId = OCR_USER_ID;
		formData.documentId = ocrData.documentId;
		formData.file = ocrData.uploadFilePath;
		api.setFormData(formData);
		
		//---------------------------------------
		//HTTP request process
		//---------------------------------------
		int res;
		try {
			res = api.upload(0);
		} catch (IOException e) {
			res = -1;
			e.printStackTrace();
		}
		
		//---------------------------------------
		//HTTP response process
		//---------------------------------------
        if (res != HttpURLConnection.HTTP_OK) {
	        MyUtils.SystemErrPrint("HTTP Connection Failed " + res);
	        return -1;
        } 

		try {
			ObjectMapper mapper = new ObjectMapper();
			api.setResponseJson(mapper.readTree(api.getResponseStr()));

			String status = api.getResponseJson().get("status").asText();
			int errorCode = api.getResponseJson().get("errorCode").asInt();
			String message = api.getResponseJson().get("message").asText();
			String unitId = api.getResponseJson().get("unitId").asText();
			MyUtils.SystemLogPrint("  status: " + status + "  " + message);
			
			if (errorCode != 0) {
				System.err.println("  addReadingPage: HTTPレスポンスエラー: " + errorCode);
				ocrData.setUploadFilePath(ocrData.uploadFilePath);
				ocrData.setUnitName("登録不可");
				ocrData.setStatus("COMPLETE");
				ocrData.setCreatedAt(MyUtils.sdf.format(new Date()));	 //yyyy/MM/dd HH:mm:ss           
				OcrDataFormDAO.getInstance(config).updateWithUploadFile(ocrData);

				return -1;
			}
			
			//unitId,statusの更新
			MyUtils.SystemLogPrint("登録済ステータス更新(REGISTED)");
			ocrData.setUploadFilePath(ocrData.uploadFilePath);
			ocrData.setUnitId(unitId);
			ocrData.setStatus("REGISTED");
			ocrData.setCreatedAt(MyUtils.sdf.format(new Date()));		//yyyy/MM/dd HH:mm:ss
			OcrDataFormDAO.getInstance(config).updateWithUploadFile(ocrData);
		} catch (SQLException e2) {
			e2.printStackTrace();
		} catch (Exception e) {
		    e.printStackTrace();
			System.err.println("  addReadingPage: 登録エラー");
			ocrData.setUploadFilePath(ocrData.uploadFilePath);
			ocrData.setUnitName("登録不可");
			ocrData.setStatus("COMPLETE");
			ocrData.setCreatedAt(MyUtils.sdf.format(new Date()));	 //yyyy/MM/dd HH:mm:ss           
			try {
				OcrDataFormDAO.getInstance(config).updateWithUploadFile(ocrData);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			return -1;
		}

    	//ReadingUnit情報の取得(2000ms後に1回実行)
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
			@Override
            public void run() {
				searchReadingUnit(ocrData, false);
            	timer.cancel();
            }
        };
        timer.schedule(task, 5000); // 5000ms後に実行
		MyUtils.SystemLogPrint("■addReadingPage: end");
		
    	return 0;
	}
	
	//---------------------------------------
	//仕分けニット追加（処理）
	//---------------------------------------
	private int addSortingPage(OcrDataFormBean ocrData) {	//String docsetName, String sorterRuleId, String uploadFilePath
		MyUtils.SystemLogPrint("■addSortingPage: start: " + ocrData.uploadFilePath);
		//---------------------------------------
		//HTTP request parametes
		//---------------------------------------
		WebApi api = new WebApi();
		api.setUrl("POST", OCR_HOST_URL + OCR_ADD_SORT);
		api.setProxy(PROXY_HOST, PROXY_PORT, PROXY_USER, PROXY_PASSWORD);
		api.putRequestHeader(OCR_API_KEY, OCR_API_KEY_VALUE);
		//---------------------------------------
		WebApi.FormData formData = new WebApi.FormData();
		formData.userId = OCR_USER_ID;
		formData.sorterRuleId = ocrData.documentId;	//sorterRuleId;
		formData.runSortingFlag = "true";
		formData.sendOcrFlag = "true";
		formData.file = ocrData.uploadFilePath;
		api.setFormData(formData);
		//---------------------------------------
		//HTTP request process
		//---------------------------------------
		int res;
		try {
			res = api.upload(2);
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}

		//---------------------------------------
		//HTTP response process
		//---------------------------------------
        if (res != HttpURLConnection.HTTP_OK) {
	        MyUtils.SystemErrPrint("HTTP Connection Failed " + res);
			if (res == 400) {
				//仕分け不可
				ocrData.setUnitName("仕分け不可");
				ocrData.setStatus("COMPLETE");
				ocrData.setCreatedAt(MyUtils.sdf.format(new Date()));	//yyyy/MM/dd HH:mm:ss
				return -1;	
			}
	        return -1;
        } 
        
		try {
			ObjectMapper mapper = new ObjectMapper();
			api.setResponseJson(mapper.readTree(api.getResponseStr()));
			
			String status = api.getResponseJson().get("status").asText();;
			int errorCode = api.getResponseJson().get("errorCode").asInt();;
			String message = api.getResponseJson().get("message").asText();;
			String sortingUnitId = api.getResponseJson().get("sortingUnitId").asText();;
			MyUtils.SystemLogPrint("  status: " + status + "  " + message);
			
			if (errorCode != 0) {
				System.err.println("  addSortingPage エラー: " + errorCode);
				ocrData.setUploadFilePath(ocrData.uploadFilePath);	//すでに入っているはず
				ocrData.setUnitId(sortingUnitId);
				ocrData.setStatus("COMPLETE");
				ocrData.setCreatedAt(MyUtils.sdf.format(new Date()));	 //yyyy/MM/dd HH:mm:ss           
				OcrDataFormDAO.getInstance(config).updateWithUploadFile(ocrData);
				
				return -1;
			}
		
			//---------------------------------------
			//unitId,statusの更新
			MyUtils.SystemLogPrint("仕分済ステータス更新(SORTED)");
			ocrData.setUploadFilePath(ocrData.uploadFilePath);
			ocrData.setUnitId(sortingUnitId);
			ocrData.setStatus("SORTED");
			ocrData.setCreatedAt(MyUtils.sdf.format(new Date()));	 //yyyy/MM/dd HH:mm:ss           
			OcrDataFormDAO.getInstance(config).updateWithUploadFile(ocrData);
	
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	//ReadingUnit情報の取得(2000ms後に1回実行)
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
			@Override
            public void run() {
				searchSortingUnit(ocrData);
            	timer.cancel();
            }
        };
        timer.schedule(task, 5000); // 5000ms後に実行
        
    	return 0;
	}

	//---------------------------------------
	//読取ユニット検索
	//---------------------------------------
	private int searchReadingUnit(OcrDataFormBean ocrData, boolean sortFlag) {
		MyUtils.SystemLogPrint("■searchReadingUnit: start");
		//add後、まだ、レスポンスが返ってきていないケースがあるので、終了する。
		if (ocrData.unitId == null) {
			MyUtils.SystemErrPrint("■searchReadingUnit: unitId不正エラー");
			return -1;
		}
		if (ocrData.unitId.equals("") == true) {
			MyUtils.SystemErrPrint("■searchReadingUnit: unitId不正エラー");
			return -1;
		}
		
		//---------------------------------------
		//HTTP request parametes
		//---------------------------------------
		WebApi api = new WebApi();
		api.setUrl("GET", OCR_HOST_URL + String.format(OCR_READ_UNIT, ocrData.unitId));
		api.setProxy(PROXY_HOST, PROXY_PORT, PROXY_USER, PROXY_PASSWORD);
		api.putRequestHeader(OCR_API_KEY, OCR_API_KEY_VALUE);

		//---------------------------------------
		//HTTP request process
		//---------------------------------------
		int res = -1;
		try {
			res = api.sendRequest();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//---------------------------------------
		//HTTP response process
		//---------------------------------------
        if (res != HttpURLConnection.HTTP_OK) {
	        MyUtils.SystemErrPrint("HTTP Connection Failed " + res);
	        return -1;
        } 

		try {
			ObjectMapper mapper = new ObjectMapper();
			api.setResponseJson(mapper.readTree(api.getResponseStr()));
			
			String status = api.getResponseJson().get("status").asText();
			int errorCode = api.getResponseJson().get("errorCode").asInt();
			String message = api.getResponseJson().get("message").asText();
			MyUtils.SystemLogPrint("  status: " + status + "  " + message);
			if (errorCode != 0) {
				//リトライ(5000ms後)
				Timer timer = new Timer(false);
				TimerTask task = new TimerTask() {
					@Override
					public void run() {
						searchReadingUnit(ocrData, false);
						timer.cancel();
					}
				};
				MyUtils.SystemErrPrint("■searchReadingUnit: リトライ...");
				timer.schedule(task, 5000);
				return -1;
			} 
			String unitId = api.getResponseJson().get("readingUnits").get(0).get("id").asText();
			String unitName = api.getResponseJson().get("readingUnits").get(0).get("name").asText();
			String unitStatus = api.getResponseJson().get("readingUnits").get(0).get("status").asText();
			String docsetId = api.getResponseJson().get("readingUnits").get(0).get("docsetId").asText();
			String csvFileName = api.getResponseJson().get("readingUnits").get(0).get("csvFileName").asText();
			String documentId = api.getResponseJson().get("readingUnits").get(0).get("documentId").asText();
			String documentName = api.getResponseJson().get("readingUnits").get(0).get("documentName").asText();	//全角は文字化けする。
			String createdAt = api.getResponseJson().get("readingUnits").get(0).get("createdAt").asText();
			createdAt = createdAt.substring(0, createdAt.length()-2);	//語尾の.0(2桁)をとる(yyyy-MM-dd HH:mm:ss)
			createdAt = createdAt.replace("-", "/");				//(yyyy-MM-dd HH:mm:ss) → (yyyy/MM/dd HH:mm:ss)
			String linkUrl = OCR_HOST_URL + String.format(OCR_LINK_ENTRY, docsetId, documentId);	//Entry画面へのリンク
			
			if (sortFlag == true) {
				//２の場合、documentNameが入っていないのでここに入れる
				ocrData.unitName = documentName;		//仕分け結果を入れる。
				ocrData.documentId = documentId;		//仕分け結果を入れる。
				ocrData.documentName = documentName;	//仕分け結果を入れる。
				//２の場合は、送信元情報を更新
				FaxDataDAO.getInstance(config).updateSoshinmotoWithUploadFile(ocrData.documentName, ocrData.uploadFilePath);
			}
			//unitId,statusの更新
			ocrData.setUnitId(unitId);
			ocrData.setStatus("OCR");
			ocrData.setCsvFileName(csvFileName);
			ocrData.setDocsetId(docsetId);
			ocrData.setCreatedAt(createdAt);
			ocrData.setLinkUrl(linkUrl);
			OcrDataFormDAO.getInstance(config).updateWithUploadFile(ocrData);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		MyUtils.SystemLogPrint("■searchReadingUnit: end");
    	return 0;
	}

	//---------------------------------------
	//仕分けユニット検索
	//---------------------------------------
	private int searchSortingUnit(OcrDataFormBean ocrData) {
		MyUtils.SystemLogPrint("■searchSortingUnit: start");
		
		//---------------------------------------
		//HTTP request parametes
		//---------------------------------------
		WebApi api = new WebApi();
		api.setUrl("POST", OCR_HOST_URL + String.format(OCR_READ_SORT, ocrData.unitId));
		api.setProxy(PROXY_HOST, PROXY_PORT, PROXY_USER, PROXY_PASSWORD);
		api.putRequestHeader(OCR_API_KEY, OCR_API_KEY_VALUE);

		//---------------------------------------
		//HTTP request process
		//---------------------------------------
		int res;
		try {
			res = api.sendRequest();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		
		//---------------------------------------
		//HTTP response process
		//---------------------------------------
        if (res != HttpURLConnection.HTTP_OK) {
	        MyUtils.SystemErrPrint("HTTP Connection Failed " + res);
	        return -1;
        } 
        
		try {
			ObjectMapper mapper = new ObjectMapper();
			api.setResponseJson(mapper.readTree(api.getResponseStr()));
			
			String status = api.getResponseJson().get("status").asText();
			int statusCode = api.getResponseJson().get("statusCode").asInt();
			int errorCode = api.getResponseJson().get("errorCode").asInt();
			String message = api.getResponseJson().get("message").asText();
			String pageCountAll = api.getResponseJson().get("pageCountAll").asText();
			MyUtils.SystemLogPrint("  status: " + status + "  " + message);
			
			//unitId指定なので1件しかないはず
			String readingUnitId = "0";
			for (JsonNode list : api.getResponseJson().get("statusList")) {
				readingUnitId = list.get("readingUnitId").asText();
				//readingUnitIdが0でないものを採用する。
				if (readingUnitId.equals("0")==false)
					break;
			}
			
			//仕分け不可
			if (statusCode == 990) {
				ocrData.setUnitName("仕分け不可");
				//ocrData.setStatus("COMPLETE");
				ocrData.setCreatedAt(MyUtils.sdf.format(new Date()));	 //yyyy/MM/dd HH:mm:ss           
				OcrDataFormDAO.getInstance(config).updateWithUploadFile(ocrData);
				//仕分け不可のメール連絡
				MyUtils.SystemLogPrint("■searchSortingUnit: 仕分け不可");
				scan2.sendSorting990Mail(ocrData);
				return -1;
			}
			
			//ステータス：OCR送信完了以降
			if (readingUnitId.equals("0") == false) {
				ocrData.setUnitId(readingUnitId);
				ocrData.setStatus("OCR");
				ocrData.setDocumentId("");
				ocrData.setDocumentName("");	//この時点で定義はわからないので空白セット
				OcrDataFormDAO.getInstance(config).updateWithUploadFile(ocrData);
				//ReadingUnit情報取得（500ms後に1回実行）
				Timer timer = new Timer();
				TimerTask task = new TimerTask() {
					@Override
					public void run() {
						searchReadingUnit(ocrData, true);	//sortFlag=true
						timer.cancel();
					}
				};
				timer.schedule(task, 500); // 500ms後に実行
			} else {
				MyUtils.SystemLogPrint("■searchSortingUnit: エラー");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		} catch (Exception e) {
			e.printStackTrace();
		}        
		MyUtils.SystemLogPrint("■searchSortingUnit: end");
		
    	return 0;
	}
	
	//---------------------------------------
	//読取ユニット処理
	//---------------------------------------
	private int processReadingUnit(OcrDataFormBean ocrData) {
        MyUtils.SystemLogPrint("■processReadingUnit: start");
		//---------------------------------------
		//HTTP request parametes
		//---------------------------------------
		WebApi api = new WebApi();
		api.setUrl("GET", OCR_HOST_URL + String.format(OCR_READ_UNIT, ocrData.unitId));
		api.setProxy(PROXY_HOST, PROXY_PORT, PROXY_USER, PROXY_PASSWORD);
		api.putRequestHeader(OCR_API_KEY, OCR_API_KEY_VALUE);
		//---------------------------------------
		//HTTP request process
		//---------------------------------------
		int res;
		try {
			res = api.sendRequest();
		} catch (Exception e) {
			e.printStackTrace();
			return -1; 
		}
		//---------------------------------------
		//HTTP response process
		//---------------------------------------
        if (res != HttpURLConnection.HTTP_OK) {
	        MyUtils.SystemErrPrint("HTTP Connection Failed " + res);
	        return -1;
        } 
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			api.setResponseJson(mapper.readTree(api.getResponseStr()));
			
			String status = api.getResponseJson().get("status").asText();
			int errorCode = api.getResponseJson().get("errorCode").asInt();
			String message = api.getResponseJson().get("message").asText();
			MyUtils.SystemLogPrint("  status: " + status + "  " + message);
			if (status.equals("success") != true) {
				if (errorCode == 103) {
					MyUtils.SystemErrPrint("  addReadingPage: HTTPレスポンスエラー: " + errorCode);
					ocrData.setUnitName("削除");
					ocrData.setStatus("COMPLETE");
					ocrData.setCreatedAt(MyUtils.sdf.format(new Date()));	 //yyyy/MM/dd HH:mm:ss           
					OcrDataFormDAO.getInstance(config).updateWithUploadFile(ocrData);
				}
				return -1;
			}
			//unitiId指定なので、1個しかないはず！
			String unitId = api.getResponseJson().get("readingUnits").get(0).get("id").asText();
			String unitName = api.getResponseJson().get("readingUnits").get(0).get("name").asText();
			String unitStatus = api.getResponseJson().get("readingUnits").get(0).get("status").asText();
			String docsetId = api.getResponseJson().get("readingUnits").get(0).get("docsetId").asText();
			String csvFileName = api.getResponseJson().get("readingUnits").get(0).get("csvFileName").asText();
			String documentId = api.getResponseJson().get("readingUnits").get(0).get("documentId").asText();
			String documentName = api.getResponseJson().get("readingUnits").get(0).get("documentName").asText();	//文字列SJISなのでいったんコメントアウト
			String createdAt = api.getResponseJson().get("readingUnits").get(0).get("createdAt").asText();
			createdAt = createdAt.substring(0, createdAt.length()-2);	//語尾の.0(2桁)をとる(YYYY-MM-DD HH:MM:SS)
			createdAt = createdAt.replace("-", "/");				//(yyyy-MM-dd HH:mm:ss) → (yyyy/MM/dd HH:mm:ss)
			String linkUrl = OCR_HOST_URL + String.format(OCR_LINK_ENTRY, docsetId, documentId);	//Entry画面へのリンク
			
			//name,status以外を更新
			ocrData.setCsvFileName(csvFileName);
			ocrData.setDocsetId(docsetId);
			ocrData.setDocumentId(documentId);
			ocrData.setDocumentName(documentName);
			ocrData.setCreatedAt(createdAt);
			ocrData.setLinkUrl(linkUrl);
			
			//statusが13(エントリー中)、22(CSVエクスポート済み)なら、CSVエクスポート処理へ移行
			if (unitStatus.equals("13")==true && ocrData.status.equals("ENTRY")==false 
					&& ocrData.status.equals("SORT")==false) {
				//CSVエクスポート（メール送信なし）、OCR後処理
				ocrData.mailFlag = 0;	//結果送付なし
				exportResultCSV(ocrData);
			//statusが16(ベリファイ完了)、22(CSVエクスポート済み)なら、CSVエクスポート処理へ移行
			} else if (unitStatus.equals("16")==true || unitStatus.equals("22")==true) {
				//CSVエクスポート（メール送信あり）、OCR後処理
				ocrData.mailFlag = 1;	//結果送付あり
				exportResultCSV(ocrData);
			} else {
				if (unitStatus.equals("13")==true) {
					MyUtils.SystemLogPrint("  定義:" + ocrData.unitName + " OCR Status(検証完了待ち): " + unitStatus);
				} else {
					MyUtils.SystemLogPrint("  定義:" + ocrData.unitName + " OCR Status(OCR完了待ち): " + unitStatus);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
        MyUtils.SystemLogPrint("■processReadingUnit: end");
        
    	return 0;
	}

	//---------------------------------------
	//読取結果のCSVエクスポート
	//---------------------------------------
	private int exportResultCSV(OcrDataFormBean ocrData) throws IOException {
        MyUtils.SystemLogPrint("■exportResultCSV: start");
		//---------------------------------------
        //出力先フォルダ取得(なければ作成)
		//---------------------------------------
        ocrData.outFolderPath = getTgtFolderPath(ocrData);
        //---------------------------------------
        //DLファイル(ファイルパス)取得
		//---------------------------------------
        String csvFilePath = ocrData.outFolderPath + "\\" + ocrData.csvFileName;	//CSVファイルフルパス
        //既存ファイルがあれば削除
		MyFiles.existsDelete(csvFilePath);
        MyUtils.SystemLogPrint("  CSV file: " + csvFilePath);
		//---------------------------------------
		//HTTP request parametes
		//---------------------------------------
		WebApi api = new WebApi();
		api.setUrl("GET", OCR_HOST_URL + String.format(OCR_UNIT_EXPORT, ocrData.unitId));
		api.setProxy(PROXY_HOST, PROXY_PORT, PROXY_USER, PROXY_PASSWORD);
		api.putRequestHeader(OCR_API_KEY, OCR_API_KEY_VALUE);
		//---------------------------------------
		//HTTP request process
		//---------------------------------------
		int res;
		try {
			res = api.download(csvFilePath);	//CSVファイルフルパス
		} catch (Exception e) {
			e.printStackTrace();
			res = -1;
		}
		
		//---------------------------------------
		//HTTP response process
		//---------------------------------------
        if (res != HttpURLConnection.HTTP_OK) {
	        MyUtils.SystemErrPrint("HTTP Connection Failed " + res);
	        return -1;
        } 

		if (ocrData.meisaiNum != 0) {
			convertCSV(ocrData);		//DLしたCSV変換処理
			if (ocrData.uploadFilePath.contains("00送信元なし")==true) {
				//送信元なしは、新帳票フォルダへファイルを移動し、ocrData, faxDataのフォルダパスを更新する必要がある。
				scan2.changeFilePath(ocrData);
			}
			postOcrProcess(ocrData);	//OCR後処理
		} else {
			//帳票定義なし、OCR結果とマッチング
			sortMatchPorcess(ocrData);
		}
     	MyUtils.SystemLogPrint("■exportResultCSV: end");
     	
     	return 0;
	}

	//---------------------------------------
	//OCRデータの出力先フォルダパスを取得
	//---------------------------------------
	private String getTgtFolderPath(OcrDataFormBean ocrData) throws IOException {
		//定義名のフォルダ作成
		if (ocrData.docsetName == null) {
			ocrData.docsetName = "仕分け不可";
		}
		String teigiFolder = OCR_OUTPUT_PATH + ocrData.docsetName + "\\" + ocrData.unitName;
		//フォルダ作成（フォルダ存在を確認し、なければフォルダ作成）
    	MyFiles.notExistsCreateDirectory(teigiFolder);
		
		//サブフォルダ(定義名＋日時)作成
		//いったん、ocrData.createdAtから日時取得する	yyyy/MM/dd HH:mm:ss → yyyy-MM-dd_HH-mm-ss
		String dtStr = ocrData.createdAt;
		dtStr = dtStr.replace("/", "-");
		dtStr = dtStr.replace(" ", "_");
		dtStr = dtStr.replace(":", "-");
		String subFolder = ocrData.unitName + "_" + dtStr;
		String outputFolder = teigiFolder + "\\" + subFolder;
		//フォルダ作成（フォルダ存在を確認し、なければフォルダ作成）
    	MyFiles.notExistsCreateDirectory(outputFolder);
    	
		return outputFolder;
	}

	private ArrayList<String> csvSplit(String line) {

        char c;
        StringBuilder s = new StringBuilder();
        String str;
        ArrayList<String> data = new ArrayList<String>();
        boolean singleQuoteFlg = false;

        for (int i=0; i < line.length(); i++){
            c = line.charAt(i);
            if (c == ',' && !singleQuoteFlg) {
            	str = s.toString().replace("\"","");	//ダブルクォーテーションは外す。
            	//MyUtils.SystemLogPrint(s.toString() + ": " + str);
                data.add(str);
                s.delete(0,s.length());
            } else if (c == ',' && singleQuoteFlg) {
                s.append(c);
            } else if (c == '\"') {
                singleQuoteFlg = !singleQuoteFlg;
                s.append(c);
            } else {
                s.append(c);
            }
        }
        if (!singleQuoteFlg) {
        	str = s.toString().replace("\"","");	//ダブルクォーテーションは外す。
        	//MyUtils.SystemLogPrint(s.toString() + ": " + str);
            data.add(str);
            s.delete(0,s.length());
        }
        
        return data;
    }
    
	private void debug_copyCsvFile(String csvPath, String csvFileName) {
		String srcPath = "D:\\pleiades\\output\\2FAX\\テスト定義名\\" + csvFileName;
		String dstPath = csvPath + csvFileName;
    	try {
			MyFiles.copyOW(srcPath, dstPath);	//上書きコピー
		} catch (IOException e) {
			e.printStackTrace();
		}
    	MyUtils.SystemLogPrint("  ファイルコピー: " + dstPath);
	}

	//---------------------------------------
	//DLしたCSV変換処理
	//---------------------------------------
    @SuppressWarnings("null")
	private void convertCSV(OcrDataFormBean ocrData) {	//throws Throwable 
		String formName = ocrData.formName;
		String documentName = ocrData.documentName;
		String createdAt = ocrData.createdAt;
		int headerNum = ocrData.headerNum;
		int meisaiNum = ocrData.meisaiNum;
		String colOutput = ocrData.colOutput;
		
		MyUtils.SystemLogPrint("■convertCSV: start");
		//---------------------------------------
		//CSVファイル読み込み
		//---------------------------------------
        String outputCsvFile = ocrData.outFolderPath + "\\" + ocrData.csvFileName;
        ArrayList<ArrayList<String>> list = null;
		try {
			list = MyFiles.parseCSV(outputCsvFile, "SJIS");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
        int maxRow = list.size();
		int maxCol = 0;
        for (int r=1; r<maxRow; r++) {	//ヘッダは除く、明細から
			MyUtils.SystemLogPrint("");
        	for (int c=0; c<list.get(r).size(); c++) {
        		System.out.print(list.get(r).get(c) + " ");
        	}
			System.out.print("\n");
            if (maxCol < list.get(r).size())
            	maxCol = list.get(r).size();	//行ごとにレス数が異なる（現状、ヘッダを全カラム設定しないため）
        }
        int repeatNum = (maxCol-ocrData.headerNum)/ocrData.meisaiNum;
        int cnvRowWidth = repeatNum * (maxRow-1) + 1;
        int cnvColWidth = ocrData.headerNum + ocrData.meisaiNum;
        ArrayList<ArrayList<String>> cnvList = new ArrayList<ArrayList<String>>();
        ArrayList<String> arrList = new ArrayList<String>();

		//---------------------------------------
        //CSV2Excelテーブル変換処理
		//---------------------------------------
        int r2offset = 0;
        String str;
        for (int r=0; r<maxRow; r++) {
        	if (r == 0) {
        		//1行目のヘッダ(カラム)
        		for (int c=0; c<cnvColWidth; c++) {
        			arrList.add(list.get(r).get(c));
        		}
    			cnvList.add(arrList);
    			//arrList.clear();
				arrList = new ArrayList<String>();
        	} else {
        		//加工前データの変換
        		if (documentName.equals("GJPN") == true) {
        			//◆2022/05/31: ヘッダ部のPOは、大文字に変換
        			String po = list.get(r).get(headerNum-1);
        			list.get(r).set((headerNum-1), po.toUpperCase());
        		}
        		int r2;
				int c2;
				boolean addOkFlag = false;
        		for (int p=0; p<repeatNum; p++) {
        			r2 = (r-1)*repeatNum + 1 + p - r2offset;
					addOkFlag = true;
					str = list.get(r).get(headerNum + p*meisaiNum);
					//特定条件下で、明細行をスキップする
					if (str.equals("") == true) {
						//明細先頭カラムが空白 この行はaddしない
						r2offset++;				//1行戻すためのオフセット加算
						cnvRowWidth--;			//データ削除分のcnvRowWidthを更新
						addOkFlag = false;
					} 
					
					if (addOkFlag == true) {
						//ヘッダデータ
						for (int c=0; c<headerNum; c++) {
							arrList.add(list.get(r).get(c));
						}
						//明細データ
						for (int c=0; c<meisaiNum; c++) {
							if ((list.get(r).size()-1) >= (c + headerNum + p*meisaiNum)) //暫定：スキップ
								str = list.get(r).get(c + headerNum + p*meisaiNum);
							else
								str = "";	//CSVパースでlistに追加できなかったので、ブランク設定
							arrList.add(str);
						}
						cnvList.add(arrList);
						//arrList.clear();
						arrList = new ArrayList<String>();
					}
        		}
        	}
        }
		//---------------------------------------
		//置換マスタオープン、データ取得処理
		//---------------------------------------
		String repXlsPath = CURRENT_PATH + "\\replace.xlsx";	//カレントフォルダのファイル
		//Excelシートは、documentNameと同名
		int repRowWidth = 256;				//暫定
		int repColWidth = cnvColWidth*2;	//暫定(カラム数x2列)
        ArrayList<ArrayList<String>> repList = new ArrayList<ArrayList<String>>();
		int res = -1;
		if (MyFiles.exists(repXlsPath)) {
			//Excelオープン(XLSXのファイル読込)
			MyUtils.SystemLogPrint("  Excelオープン...: " + repXlsPath + " 定義名: " + documentName);
			try {
				MyExcel xlsx = new MyExcel();
				xlsx.open(repXlsPath, documentName, true);
				if (xlsx.sheetExist()) {
					CellType ctype;
					str = "";
					arrList = new ArrayList<String>();
					for (int rowIdx=0; rowIdx<repRowWidth; rowIdx++) {	//データ1行目（ヘッダ）がMAX列数
						//読み込み行があるかCheck
						if (xlsx.getRow(rowIdx) == false)
							continue;
						for (int colIdx=0; colIdx<repColWidth; colIdx++) {
							//読み込みCellがあるかCheck
							if (xlsx.getCell(colIdx) == false)
								continue;
							ctype = xlsx.getCellType(colIdx);
							if (ctype == CellType.STRING) {
								str = xlsx.getStringCellValue();
								if (str.equals("") == true) 
									break;	//カラムは、ブランクで終了
							} else if (ctype == CellType.NUMERIC) {
								int val = (int)xlsx.getNumericCellValue();
								str = Integer.valueOf(val).toString();
							}
							arrList.add(str);	//col追加
						}
						repList.add(arrList);	//row追加
						arrList = new ArrayList<String>();
						res = 0;	//置換情報の読込完了
					}
				} else {
					MyUtils.SystemErrPrint("  定義が存在しませんでした");
				}
				xlsx.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	} else {
    		MyUtils.SystemErrPrint("  置換マスタファイルが見つかりませんでした");
    	}
		if (res == 0) {
			//置換処理
			int repRowWidth2;
			int col2;
			String before = "";
			String after = "";
			String org = "";
			for (col2=0; col2 < repList.size(); col2=col2+2) {
				repRowWidth2 = 0;
				for (int row2 = (repRowWidth-1); row2>0; row2--) {
					if (repList.get(row2).get(col2) != null) {
						repRowWidth2 = row2;
						break;
					}
				}
				//変換値あり
				if (repRowWidth2 > 1) {
					for (int row2=2; row2<=repRowWidth2; row2++) {
						before = repList.get(row2).get(col2);
						after = repList.get(row2).get(col2+1);
						int colIdx;
						for (int rowIdx=0; rowIdx < cnvRowWidth; rowIdx++) {
							colIdx = col2/2;
							org = cnvList.get(rowIdx).get(colIdx);
							if (org.equals("") != true) {
								str = org.replace(before, after);
								MyUtils.SystemLogPrint("  replace: " + org + "→" + str);
								cnvList.get(rowIdx).set(colIdx, str);
							}
						}
					}
				}
			}
		}
		//---------------------------------------
		//数値系の固定のデータ加工設定
		//---------------------------------------
        int colSuryo = ocrData.colSuryo;
        int colTanka = ocrData.colTanka;
        int colKingaku = ocrData.colKingaku;
        int colHinmei = ocrData.colHinmei;
		for (int rowIdx=1; rowIdx<cnvRowWidth; rowIdx++) {
			for (int colIdx=0; colIdx<cnvColWidth; colIdx++) {
				if (colIdx!=0 && ((colIdx==colSuryo) || (colIdx==colTanka) || (colIdx==colKingaku))) {
					//,を消す。Excel出力時は不要のため
					str = cnvList.get(rowIdx).get(colIdx).replace(",", "");
					cnvList.get(rowIdx).set(colIdx, str);
				}
			}
		}
		//---------------------------------------
    	//RirekiDBへの履歴データ書き込み
		//---------------------------------------
    	//Excel形式で保存 / 履歴テーブルDBに書き込み
		OcrRirekiDAO ocrRireki = new OcrRirekiDAO(config);
        try {
			ocrRireki.deleteRirekiData(createdAt, formName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
        String defXlsPath = CURRENT_PATH + "\\templete\\" + documentName + ".xlsx";	//カレントフォルダ
		if (MyFiles.exists(defXlsPath)) {
			//テンプレートから出力ファイル生成
			String tmpXlsPath = CURRENT_PATH + "\\templete\\" + documentName + "_tmp.xlsx";
			try {
				MyFiles.copyOW(defXlsPath, tmpXlsPath);	//上書きコピー
			} catch (IOException e) {
				e.printStackTrace();
			}
			//Excelオープン
			MyUtils.SystemLogPrint("  Excelオープン...: " + tmpXlsPath + " 定義名: " + documentName);
			try {
				MyExcel xlsx = new MyExcel();
				xlsx.open(tmpXlsPath, null, false);	//1シート目
		        boolean resultFlag;
		        String resultMsg;
				String renkeiMsg = "";
		        String strValue;
				double suryo = 0.0;
				double tanka = 0.0;
				double kingaku = 0.0;
		        boolean numFlag;
		        ocrData.checkResult = "";
				ocrData.renkeiResult = "";
		        ocrData.chubanDblFlag = false;
				ocrData.chubanlist = "注文番号(PO)";
		        for (int rowIdx=1; rowIdx<cnvRowWidth; rowIdx++) {	//データ2行目（明細）から開始
		        	resultFlag = false;
		        	resultMsg = "";
		        	String fields = "COL0,COL1,COL2,";
		        	String values = "'" + createdAt + "','" + formName + "','" + rowIdx + "',";
		        	xlsx.createRow(rowIdx);			//行の生成
				    int colIdx;
		        	for (colIdx=0; colIdx<cnvColWidth; colIdx++) {
		        		numFlag = false;
		        		strValue = "";
		        		if (colSuryo != 0 && colTanka != 0 && colKingaku != 0) {
		        			if (colIdx==colSuryo) {
								try {
									suryo = Double.parseDouble(cnvList.get(rowIdx).get(colIdx));
									xlsx.setCellValue(colIdx, suryo);
									strValue = String.valueOf(suryo);
									numFlag = true;
								} catch(NumberFormatException e) {
								}	
		        			}
		        			if (colIdx==colTanka) {
								try {
									tanka = Double.parseDouble(cnvList.get(rowIdx).get(colIdx));
									xlsx.setCellValue(colIdx, tanka);
									strValue = String.valueOf(tanka);
									numFlag = true;
								} catch(NumberFormatException e) {
								}	
		        			}
		        			if (colIdx==colKingaku) {
								try {
									kingaku = Double.parseDouble(cnvList.get(rowIdx).get(colIdx));	//金額も.00のケースあり
									xlsx.setCellValue(colIdx, kingaku);
									strValue = String.valueOf(kingaku);
									numFlag = true;
								} catch(NumberFormatException e) {
								}
		        			}
		        		}
		        		if (numFlag != true ) {	
				        	strValue = cnvList.get(rowIdx).get(colIdx).trim();
				        	cnvList.get(rowIdx).set(colIdx, strValue);
						    xlsx.setCellValue(colIdx, strValue);
		        		}
			        	//通常データは、4列目（+3）から挿入
			        	fields = fields + "COL" + (colIdx+3) + ",";
			        	values = values + "'" + strValue + "',";
			    	}
		        	//---------------------------------------
		        	//数値x単価=金額チェック
					//---------------------------------------
		        	if ((rowIdx != 0) && (cnvList.get(rowIdx).get(0).equals("") != true) && (colSuryo != 0 && colTanka != 0 && colKingaku != 0)) {
		        		int calc = (int) (suryo*tanka);
		        		if (calc != kingaku) {
		        			if (resultFlag != true) {
		        				resultFlag = true;
		        				resultMsg = rowIdx + "行目: ";
		        			}
		        			resultMsg = resultMsg + " 金額不一致";
		        			ocrData.checkResult = ocrData.checkResult + resultMsg; 
						    xlsx.setCellValue(colIdx, resultMsg);
						    MyUtils.SystemLogPrint("  結果: " + resultMsg);
		        		}
		        	}
		        	//---------------------------------------
		        	//品名は 履歴と存在チェック
					//---------------------------------------
		        	if ((colHinmei != 0) && (cnvList.get(rowIdx).get(colHinmei).equals("") != true)) {
		        		int count = 0;
		        		try {
		        			count = ocrRireki.queryCountRirekiHinmei(formName, colHinmei, cnvList.get(rowIdx).get(colHinmei));
						} catch (SQLException e) {
							e.printStackTrace();
						}
		        		if (count == 0) {
		        			if (resultFlag != true) {
		        				resultFlag = true;
		        				resultMsg = rowIdx + "行目: ";
		        			}
		        			resultMsg = resultMsg + " 品名履歴存在なし";
		        			ocrData.checkResult = ocrData.checkResult + resultMsg; 
						    xlsx.setCellValue(colIdx, resultMsg);
						    MyUtils.SystemLogPrint("  結果: " + resultMsg);
		        		} else {
		        			MyUtils.SystemLogPrint("  過去履歴数(品名): " + count);
		        		}
		        	}
					//---------------------------------------
		            //履歴データをDBに書き込み
					//---------------------------------------
		            if (resultFlag == true) {
		            	fields = fields + "COL" + (colIdx+3) + ",";			//最後のカラムはカンマ不要
		            	values = values + "'" + resultMsg + "',";			//最後のカラムはカンマ不要
		            	ocrData.checkResult = ocrData.checkResult + "\n";
		            }
		        	try {
		            	String temp;
		            	int len;
		            	temp = fields;
		            	len = temp.length();
		            	fields = temp.substring(0, (len-1));	//末尾のカンマを外す
		            	temp = values;
		            	len = temp.length();
		            	values = temp.substring(0, (len-1));	//末尾のカンマを外す
		            	ocrRireki.insertRirekiData(fields, values);
					} catch (SQLException e) {
						e.printStackTrace();
					}
					//★履歴テーブルへ書き込み以前にCHECKしたほうがいいかも
					//---------------------------------------
		            //取引先、日付、注番が入ってること(空白はNG)。数量、単価、金額のすべてが数値であること(空白は無視)。
					//---------------------------------------
					if (colOutput != null && ocrData.chubanDblFlag == false) {
						int colConv;
						String colOutputNo = colOutput.replace("COL", "");
						ArrayList<String> No = csvSplit(colOutputNo);	//カンマで分割
						int colLen = cnvList.get(rowIdx).size();
						String hizuke = "";
						String chuban = "";
						String strSuryo = "";
						String strTanka = "";
						String strKingaku = "";
						///String triSaki = No.get(0);
						colConv = Integer.parseInt(No.get(1));	//4
						if (colLen > (colConv-3))
							hizuke = cnvList.get(rowIdx).get(colConv-3);
						//String nonyuSaki = No.get(2);
						colConv = Integer.parseInt(No.get(3));	//6
						if (colLen > (colConv-3))
							chuban = cnvList.get(rowIdx).get(colConv-3);
						//---------------------------------------------------------------//
						//注番の重複チェック 履歴に同注番がある場合は連携しない (注文書の単位で実施)
						String colChuban = ("COL" + String.valueOf(colConv));
						int count = ocrRireki.queryCountRirekiChuban(createdAt, formName, colChuban);
						if (count >= 2) {
							renkeiMsg = "過去にも注番が存在しています。変更注文と識別して新規登録はしておりません。ご確認お願いします。";
							MyUtils.SystemErrPrint(renkeiMsg);
							ocrData.renkeiResult = ocrData.renkeiResult + renkeiMsg + "\n";
							ocrData.chubanDblFlag = true;
							continue;	//以降の処理はスキップして、ループを回す。
							//DblFlagをOcrDataTableへ設定(カラム追加)
						}
						ocrData.chubanlist = ocrData.chubanlist + "\n" + chuban;
						//---------------------------------------------------------------//
						//String hinmei = No.get(4);
						colConv = Integer.parseInt(No.get(5));	//8
						if (colLen > (colConv-3))
							strSuryo = cnvList.get(rowIdx).get(colConv-3);
						colConv = Integer.parseInt(No.get(6));	//9
						if (colLen > (colConv-3))
							strTanka = cnvList.get(rowIdx).get(colConv-3);
						colConv = Integer.parseInt(No.get(7));	//10
						if (colLen > (colConv-3))
							strKingaku = cnvList.get(rowIdx).get(colConv-3);
						//String nouki = No.get(8);
						//String kekka = No.get(9);
						// 日付、注番が入ってること(どちらかブランクはNG)
						ocrData.chumonNGFlag = false;
						if (hizuke.equals("")==true) {
							MyUtils.SystemErrPrint("日付: 空白");
							ocrData.chumonNGFlag = true;
						}
						if (chuban.equals("")==true) {
							MyUtils.SystemErrPrint("注番: 空白");
							ocrData.chumonNGFlag = true;
						}
						ocrData.numericNGFlag = false;
						//数量、単価、金額のすべてが数値であること
						if (strSuryo.equals("") != true) {
							//数値に変換 → 数値CHECK
							try {
								Double.parseDouble(strSuryo);
							} catch(NumberFormatException e) {
								ocrData.numericNGFlag = true;
								MyUtils.SystemErrPrint("数量: 数値NG " + strSuryo);
							}
						}
						if (strTanka.equals("") != true) {
							//数値に変換 → 数値CHECK
							try {
								Double.parseDouble(strTanka);
							} catch(NumberFormatException e) {
								ocrData.numericNGFlag = true;
								MyUtils.SystemErrPrint("単価: 数値NG " + strTanka);
							}
						}
						if (strKingaku.equals("") != true) {
							//数値に変換 → 数値CHECK
							try {
								Double.parseDouble(strKingaku);
							} catch(NumberFormatException e) {
								ocrData.numericNGFlag = true;
								MyUtils.SystemErrPrint("金額: 数値NG " + strKingaku);
							}
						}
						if (ocrData.chumonNGFlag == false && ocrData.numericNGFlag == false) {
							//履歴テーブルから台帳テーブルへinsert/select
							MyUtils.SystemLogPrint("履歴テーブルから台帳テーブルへinsert/select");
							try {
								ocrRireki.insertDaichofromRirekiData(colOutput, createdAt, formName, Integer.valueOf(rowIdx).toString());
							} catch (SQLException e) {
								e.printStackTrace();
							}

						} else {
							MyUtils.SystemErrPrint("注番/日付NG:" + ocrData.chumonNGFlag + " 数値系NG:" + ocrData.numericNGFlag);
							renkeiMsg = "イレギュラーが発生しため、台帳へ連携できませんでした(注番/日付/数値系NG)";
							MyUtils.SystemErrPrint(renkeiMsg);
							ocrData.renkeiResult = ocrData.renkeiResult + renkeiMsg + "\n";
							ocrData.chubanDblFlag = true;
						}
					}
		        }	//rowIdx
	
		        //---------------------------------------
		        //XLSXのファイル保存
				//---------------------------------------
				String outputcsvPath = ocrData.outFolderPath + "\\" + ocrData.csvFileName;
				String outFilePath = outputcsvPath.replace(".csv",".xlsx");
		        MyUtils.SystemLogPrint("  XLSXファイル保存: " + outFilePath);
				xlsx.save(outFilePath);
				xlsx.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			MyUtils.SystemErrPrint("  " + documentName + "テンプレートファイルが見つかりませんでした");
		}
		try {
/* 			//テスト：XLSXファイルは削除
			Path xlsFilePath = Paths.get(outFilePath);
			if (Files.exists(xlsFilePath)){
				MyUtils.SystemLogPrint("  XLSXフファイル削除: " + xlsFilePath.toString());
				Files.delete(xlsFilePath);
			}*/
			//CSVファイル（元ファイル）は削除
			String csvFilePath = ocrData.outFolderPath + "\\" + ocrData.csvFileName;
			MyUtils.SystemLogPrint("  CSVファイル削除: " + csvFilePath);
			MyFiles.existsDelete(csvFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//---------------------------------------

        MyUtils.SystemLogPrint("■convertCSV: end");
	}
	
	//---------------------------------------
	//OCR後処理
	//---------------------------------------
	private void sortMatchPorcess(OcrDataFormBean ocrData) {
		MyUtils.SystemLogPrint("sortMatchPorcess: start");
		//---------------------------------------
		//CSV読み込み
		//---------------------------------------
        String outputCsvFile = ocrData.outFolderPath + "\\" + ocrData.csvFileName;
        ArrayList<ArrayList<String>> list = null;
		try {
			list = MyFiles.parseCSV(outputCsvFile, "SJIS");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
        int maxRow = list.size();
		int maxCol = 0;
        for (int r=1; r<maxRow; r++) {	//ヘッダは除く、明細から
			//MyUtils.SystemLogPrint("");
        	//for (int c=0; c<list.get(r).size(); c++) {
        	//	System.out.print(list.get(r).get(c) + " ");
        	//}
			//System.out.print("\n");
            if (maxCol < list.get(r).size())
            	maxCol = list.get(r).size();	//行ごとにレス数が異なる（現状、ヘッダを全カラム設定しないため）
        }
		//2行目にデータが入っている。
		String readValue = list.get(1).get(0);
		MyUtils.SystemLogPrint("仕分後のdocument: " + ocrData.unitName);
		MyUtils.SystemLogPrint("     OCR読取結果: " + readValue);

		if (ocrData.unitName.equals(readValue) == true) {
			MyUtils.SystemLogPrint("仕分けと読取り結果がマッチング(仕分けOK) ⇒ 注文書外として処理");
			readValue = ""; //メール本文にいれないよう空白に設定
			//送信元なしは、新帳票フォルダへファイルを移動し、ocrData, faxDataのフォルダパスを更新する必要がある。
			scan2.changeFilePath(ocrData);
		} else {
			ocrData.checkResult = "仕分けと読取り結果がアンマッチ(誤仕分け:" + ocrData.unitName + ") ⇒ 仕分け不可として処理";
			MyUtils.SystemLogPrint(ocrData.checkResult);
			//
			try {
				FaxDataDAO.getInstance(config).updateSoshinmotoWithUploadFile("仕分け不可", ocrData.uploadFilePath);
				ocrData.unitName = "00送信元なし";
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		try {
			 scan2.sortMatchMailPorcess(ocrData, readValue);
		} catch (Throwable e) {
			e.printStackTrace();
		}
/* 		Excelを作らないのでCSVは当面削除しない
 * 		try {
			//CSVファイル（元ファイル）は削除
			Path csvFilePath = Paths.get(ocrData.outFolderPath + "\\" + ocrData.csvFileName);
			if (Files.exists(csvFilePath)){
				MyUtils.SystemLogPrint("  CSVファイル削除: " + csvFilePath.toString());
					Files.delete(csvFilePath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}*/	
 		//---------------------------------------
		//完了ステータス更新
		MyUtils.SystemLogPrint("完了ステータス更新(COMPLETE)");
		try {
			ocrData.setUploadFilePath(ocrData.uploadFilePath);
			ocrData.setStatus("COMPLETE");
			//ocrData.setCreatedAt(MyUtils.sdf.format(new Date()));	 //yyyy/MM/dd HH:mm:ss           
			OcrDataFormDAO.getInstance(config).updateWithUploadFile(ocrData);
		} catch (SQLException e) {
			e.printStackTrace();
		}

        MyUtils.SystemLogPrint("sortMatchPorcess: end");
	}

	//---------------------------------------
	//OCR後処理
	//---------------------------------------
	private void postOcrProcess(OcrDataFormBean ocrData) {
        MyUtils.SystemLogPrint("■postOcrProcess: start");

		//---------------------------------------
        //出力先フォルダ取得（なければ作成）
		//---------------------------------------
        String outputFolderPath;
		try {
			outputFolderPath = getTgtFolderPath(ocrData);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
        
		//---------------------------------------
        //読込画像ファイルを取得
		//---------------------------------------
        String uploadFilePath = ocrData.uploadFilePath;	//pdf
  		String fileName = MyFiles.getFileName(uploadFilePath);	//フルパスからファイル名取得
        String copyToFile = outputFolderPath + "\\" + fileName;
        //pdf回転変換
		String[] copyCmd = new String[6];
		copyCmd[0] = "pdftk";	//"C:\\Program Files (x86)\\PDFtk Server\\bin\\pdftk";
		copyCmd[1] = uploadFilePath;	//uploadFilePath.replace("\\", "\\\\");
		copyCmd[2] = "cat";
		copyCmd[3] = ocrData.rotateInfo;	//回転あり
		copyCmd[4] = "output";
		copyCmd[5] = copyToFile;		//copyToFile.replace("\\\\", "\\");	//この変換の必要性については要確認！
		System.out.print("  ");
		for (String cmd : copyCmd)
			System.out.print(cmd + " ");
		System.out.print("\n");
        //MyUtils.SystemLogPrint("  " + copyCmd);
		//https://qiita.com/mitsumizo/items/836ce2e00e91c33fcf95
		ProcessBuilder pdfProcess = new ProcessBuilder(copyCmd);
		try {
			Process process = pdfProcess.start();
			System.out.println("  戻り値：" + process.waitFor());	//応答待ち
			try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
	            String line;
	            while ((line = r.readLine()) != null) {
	                MyUtils.SystemLogPrint(line);
	            }
	        }
			//回転したpdfをバックコピー
			if (ocrData.docsetName.equals(SCAN_CLASS1) == true) {
		    	MyFiles.copyOW(copyToFile, uploadFilePath);	//上書きコピー
				MyUtils.SystemLogPrint("  ファイルバックコピー");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//連絡メール送信
		if (ocrData.type == 0) {
			String fileName2 = MyFiles.getFileName(uploadFilePath);	//フルパスからファイル名取得
			String pdfPath = outputFolderPath + "\\" + fileName2;
			try {
				scan2.sendMailProcess(ocrData, pdfPath);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		//zip圧縮前のExcelファイルを **** フォルダへコピー ⇒ ただ、この時点で、マクロ実行すればよい
		} else if (ocrData.type == 2) {
			String chumonSrc = outputFolderPath + "\\" + ocrData.csvFileName.replace("csv", ".xlsx");
			String chumonDst = OCR_UPLOAD_PATH + ocrData.csvFileName.replace("csv", ".xlsx");
			try {
				MyFiles.copyOW(chumonSrc, chumonDst);	//上書きコピー
			} catch (IOException e) {
				e.printStackTrace();
			}
			MyUtils.SystemLogPrint("  OCR変換結果を注文書取込へ連携");
		}

		//完了ステータス更新
		MyUtils.SystemLogPrint("完了ステータス更新(COMPLETE)");
		try {
			ocrData.setUploadFilePath(uploadFilePath);
			ocrData.setStatus("COMPLETE");
			//ocrData.setCreatedAt(MyUtils.sdf.format(new Date()));	 //yyyy/MM/dd HH:mm:ss           
			OcrDataFormDAO.getInstance(config).updateWithUploadFile(ocrData);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//最後
        //フォルダをzip圧縮し、メール送信（フラグ有効時のみ）
		//Javaのzip圧縮	https://www.lisz-works.com/entry/compression-7zip-command
		String[] zipCmd = new String[4];
		zipCmd[0] = "7z.exe";	//"C:\\Program Files\\7-Zip\\7z.exe";
		zipCmd[1] = "a";
		zipCmd[2] = outputFolderPath + ".zip";	//outputFolderPath.replace("\\", "\\\\") + ".zip";	//圧縮フォルダ
		zipCmd[3] = outputFolderPath;			//outputFolderPath.replace("\\", "\\\\");			//対象フォルダ
		System.out.print("  ");
		for (String cmd : zipCmd) 
			System.out.print(cmd + " ");
		System.out.print("\n");
		ProcessBuilder zipProcess = new ProcessBuilder(zipCmd);
		try {
			Process process = zipProcess.start();
			System.out.println("  戻り値：" + process.waitFor());	//応答待ち
		/*	try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
			    String line;
			    while ((line = r.readLine()) != null) {
			    	if (line.equals("") != true) 
			    		MyUtils.SystemLogPrint(line);
			    }
			}*/
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//不要になったターゲットフォルダは削除	https://codechacha.com/ja/java-delete-files-recursively/
		File rootDir = new File(outputFolderPath);
        deleteFilesRecursively(rootDir);
        
        MyUtils.SystemLogPrint("■postOcrProcess: end");
	}

	private boolean deleteFilesRecursively(File rootFile) {
        File[] allFiles = rootFile.listFiles();
        if (allFiles != null) {
            for (File file : allFiles) {
                deleteFilesRecursively(file);
            }
        }
        System.out.println("  Remove file: " + rootFile.getPath());
        return rootFile.delete();
    }
}
