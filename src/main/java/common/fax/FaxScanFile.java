package common.fax;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.ss.usermodel.CellType;

import com.example.demo.InsourcingConfig;
import com.fasterxml.jackson.databind.JsonNode;

import common.ocr.OcrDataFormBean;
import common.ocr.OcrDataFormDAO;
import common.ocr.OcrFormBean;
import common.ocr.OcrFormDAO;
import common.utils.MyExcel;
import common.utils.MyFiles;
import common.utils.MyMail;
import common.utils.MyUtils;

public class FaxScanFile implements Runnable {
	String kyoten;
	String targetPath;
	String SCAN_CLASS1;
	String SCAN_CLASS2;
	String OCR_INPUT_PATH;
	static InsourcingConfig config;
	static MyMail mailConf;

	public FaxScanFile(InsourcingConfig argConfig, String argKyoten) {
		config = argConfig;
		this.kyoten = argKyoten;

		mailConf = new MyMail();
		mailConf.host = config.getMailHost();
		mailConf.port = config.getMailPort();
		mailConf.username = config.getMailUsername();
		mailConf.password = config.getMailPassword();
		mailConf.smtpAuth = config.getMailSmtpAuth();
		mailConf.starttlsEnable = config.getMailSmtpStarttlsEnable();
		
		this.SCAN_CLASS1 = config.getScanDefTgt1();
		this.SCAN_CLASS2 = config.getScanDefTgt2();

		if (this.kyoten.equals(this.SCAN_CLASS1) == true ) {
			this.targetPath = config.getScanPath1();
		} else if (this.kyoten.equals(this.SCAN_CLASS2) == true ) {
			this.targetPath = config.getScanPath2();
		} else {
			MyUtils.SystemErrPrint("  Scan場所が登録されていません");
			this.targetPath = "";			
		}
	}

	//public static void main(String[] args) throws Exception {
	@Override
	public void run() {
		ScanRemainedFile(this.targetPath);	//すでにフォルダにあるpdfをScan
		//指定ディレクトリ配下のファイルのみ(またはディレクトリ)を取得
		// https://qiita.com/fumikomatsu/items/67f012b364dda4b03bf1
		try {
	        WatchService watcher;
			watcher = FileSystems.getDefault().newWatchService();
			Path dir = Paths.get(this.targetPath);
			dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
			MyUtils.SystemLogPrint("■FaxScanFile: scan " + this.kyoten + ": " + this.targetPath);
	        for (;;) {
	            WatchKey watchKey = watcher.take();
	            for (WatchEvent<?> event: watchKey.pollEvents()) {
	                if (event.kind() == OVERFLOW) continue;
	                //新規作成
	                if (event.kind() == ENTRY_CREATE) {
	                    WatchEvent<Path> ev = cast(event);
	                    Path name = ev.context();		//ファイル名
	                    Path src = dir.resolve(name);	//フルパス
	                    String fileName = name.toString();
	                    System.out.format("%s: %s %s\n", event.kind().name(), src, name);
	                    //String extension = fileName.substring(fileName.lastIndexOf("."));	//
	                    String extension = fileName.substring(fileName.length()-3);	//拡張子：後ろから3文字
	                    if (extension.equals("pdf") == true) {
                    		MyUtils.SystemLogPrint("  ファイル検出...: " + fileName);
							scanProcess(fileName);
	                    }
	                }
	            }
	            watchKey.reset();
	        }
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	void ScanRemainedFile(String targetPath) {
		//指定ディレクトリ配下のファイルのみ(またはディレクトリのみ)を取得
        File file = new File(targetPath);
        File fileArray[] = file.listFiles();
        
		try {
			for (File f: fileArray){
				if (f.isFile()) {
					String fileName = MyFiles.getFileName(f.toString());	//フルパスからファイル名取得
					String extension = fileName.substring(fileName.length()-3);	//拡張子：後ろから3文字
					if (extension.equals("pdf") == true) {
						//String extension = fileName.substring(fileName.lastIndexOf("."));	//
						MyUtils.SystemLogPrint("  ファイル検出...: " + fileName);
						scanProcess(fileName);
					}
                }
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	void scanProcess(String fileName) throws Throwable {
		String faxNo = "";
		String createdAt = "";
		String dateStr = "";
		String timeStr = "";
		String jobNo = "";
		String soshinMoto = "";
		
		//1 ファイル名をパース
		if (this.kyoten.equals(this.SCAN_CLASS1) == true) {
			faxNo = fileName.substring(3, (fileName.length() -20 -4));	//doc + datetimeJOB + .pdf分差し引く
			createdAt = fileName.substring((fileName.length() -24), (fileName.length() -10));	//2020 0727 143359 041084 の 20桁
			dateStr = createdAt.substring(0,4) + "/" + createdAt.substring(4,6) + "/" + createdAt.substring(6,8); 
			timeStr = createdAt.substring(8,10) + ":" + createdAt.substring(10,12) + ":" + createdAt.substring(12,14); 
			jobNo = fileName.substring((3 + faxNo.length() + 14), (3 + faxNo.length() + 19));
		} else if (this.kyoten.equals(this.SCAN_CLASS2) == true) {	//★逆にしている
			faxNo = fileName.substring(3, (fileName.length() -20 -4));	//doc + datetimeJOB + .pdf分差し引く
			createdAt = fileName.substring((fileName.length() -18), (fileName.length() -4));	//2020 0727 143359 041084 の 20桁
			dateStr = createdAt.substring(0,4) + "/" + createdAt.substring(4,6) + "/" + createdAt.substring(6,8); 
			timeStr = createdAt.substring(8,10) + ":" + createdAt.substring(10,12) + ":" + createdAt.substring(12,14); 
			jobNo = fileName.substring((3 + faxNo.length()), (3 + faxNo.length() + 6));
		}
		if (faxNo == "") faxNo = "送信元なし";
		MyUtils.SystemLogPrint("  pdfファイル検出...: faxNo " + faxNo + " datetime: " + dateStr + " " + timeStr + " jobNo " + jobNo);
		
		String mailBody1 = "";
		String mailBody2 = "";
		String flag = "0";
		String syubetsu = "";
		//2 Excelオープン
    	try {
			CellType ctype;
			String listfaxNo = "";
			
			// Excelファイルへアクセス(eclipse上でパスをしていないとプロジェクトパスになる)
			String xlsPath = this.targetPath + this.kyoten + "-pdf管理表.xlsm";
			MyExcel xlsx = new MyExcel();
			xlsx.open(xlsPath, null);
			MyUtils.SystemLogPrint("  Excelオープン..." + xlsPath);
			// シートを取得
			xlsx.setSheet("MAIL");
			flag = "0"; 
			//配信メール情報:From
			mailConf.fmAddr = xlsx.getStringCellValue(1, 1);
			//配信メール情報:Bcc
			mailConf.bccAddr = xlsx.getStringCellValue(4, 1);
			//配信メール情報:Body1,2
			mailBody1 = xlsx.getStringCellValue(6, 1);
			mailBody2 = xlsx.getStringCellValue(7, 1);
			mailConf.body = mailBody1 + "\n" + mailBody2 + "\n" + fileName + "\n";
			
			//------------------------------------------------------
			//FAX送信元 検索
			xlsx.setSheet("マスタ");
			soshinMoto = xlsx.search(2, listfaxNo);	//結果をrowにセット。マッチしなければ最下行をセット。
			xlsx.getCell(3);			//D列：SOSHIN_MOTO
			if (xlsx.getStringCellValue() != null) {
				soshinMoto = xlsx.getStringCellValue();
				soshinMoto = soshinMoto.trim();		//前後に空白が入っていたら除去
			} else {
				soshinMoto = "";
			}
			xlsx.getCell(6);			//G列
			mailConf.toAddr = xlsx.getStringCellValue();
			xlsx.getCell(7);			//H列
			mailConf.ccAddr = xlsx.getStringCellValue();
			xlsx.getCell(0);			//A列:FLAG
			ctype = xlsx.getCellType(0);
			if (ctype == CellType.NUMERIC) {
				int val = (int)xlsx.getNumericCellValue();
				flag =  String.valueOf(val);
			} else {
				flag = xlsx.getStringCellValue();
			}
			xlsx.getCell(1);			//B列:種別
			syubetsu = xlsx.getStringCellValue();
			//------------------------------------------------------
			if (faxNo == "なし") faxNo = "";	//表示は空白
			xlsx.close();
    	} catch (Throwable t) {
    	    //LOG.error("Failure during static initialization", t);
    	    throw t;
    	}
    	
    	//------------------------------------------------------
        //フォルダ整理（フォルダ存在を確認し、なければフォルダ作成）
        //------------------------------------------------------
    	String dstDirPath = this.targetPath + soshinMoto;
		MyFiles.notExistsCreateDirectory(dstDirPath);

        //------------------------------------------------------
        //送信元フォルダへ移動(上書き)
        //------------------------------------------------------
        String srcPath = this.targetPath + fileName;
        String dstPath = dstDirPath + "\\" + fileName;
    	MyUtils.SystemLogPrint("  ファイル移動: " + dstPath);
		try {
			MyFiles.moveOW(srcPath, dstPath);	//上書き
		} catch (NoSuchFileException e) {
			return;	//★あやしいので、いったん、登録処理はしない。
		}
    	
        //------------------------------------------------------
        //Fax登録処理
        //------------------------------------------------------
    	FaxDataBean fax = new FaxDataBean();
    	fax.setFaxData(faxNo, dateStr, timeStr, jobNo, fileName, soshinMoto, dstPath, this.kyoten);
    	FaxDataDAO.getInstance(config).insertDB(fax);
        
        //------------------------------------------------------
        //OCR登録処理(注文書)	★NEXTで、A,B列を変更する必要があり
        //------------------------------------------------------
		if (flag.equals("1") == true) {
			MyUtils.SystemLogPrint("  OCR登録...");
			int type = -1;
			if (this.kyoten.equals(this.SCAN_CLASS1) == true) {
				type = 2;
			} else if (this.kyoten.equals(this.SCAN_CLASS2) == true) {
				type = 0;
			} else {
				return;
			}
			registOcrProcess(dstPath, type);
			
			return;
		}
		//注文書以外、番号登録あり
		if (flag.equals("1") != true) {
			//パターン①
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			mailConf.subject = "TEST " + this.kyoten + "送信連絡" + "("+ dateFormat.format(new Date()) + " " + soshinMoto + ")";
			mailConf.attach = dstPath;
			sendScanMail(this.kyoten);
			
			return;
		}
	}

	private int registOcrProcess(String uploadFilePath, int type) {
		MyUtils.SystemLogPrint("registOcrProcess: start uploadFilePath: " + uploadFilePath.toString());
		if (uploadFilePath.equals("") == true) {
			return -1;
		}
		String importPath = MyFiles.getParent(uploadFilePath);	//パスからフォルダ名取得

		int sortFlag = 0;
		String status = "";
		OcrDataFormBean ocrData = new OcrDataFormBean();
		if (importPath.toString().contains("送信元なし") != true) {
			sortFlag = 0;	//定義名確定
			status = "REGIST";
		} else if (importPath.toString().contains("送信元なし") == true) {
			sortFlag = 1;	//仕分けユニット行き
			status = "SORT";
		} 
		try {
			OcrFormBean ocrForm = OcrFormDAO.getInstance(config).queryOcrFormImportPath(sortFlag, importPath);
			ocrData.setUnitName(ocrForm.getFormName());
			ocrData.setDocumentId(ocrForm.getDocumentId());
			ocrData.setDocumentName(ocrForm.getDocumentName());
			ocrData.setDocsetName(ocrForm.getDocsetName());
			ocrData.setUploadFilePath(uploadFilePath);
			ocrData.setStatus(status);
			ocrData.setType(type);
			OcrDataFormDAO.getInstance(config).insertReadingUnitDB(ocrData);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		MyUtils.SystemLogPrint("registOcrProcess: end");
		
		return 0;		
	}
	
	@SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
	
	public void changeFilePath(OcrDataFormBean ocrData) {
    	MyUtils.SystemLogPrint("changeFilePath: start");
		String srcPath = ocrData.getUploadFilePath();
		
		File file = new File(srcPath);
		String fileName  = file.getName() ;
        String dstDirPath = ocrData.getTargetPath() + ocrData.getUnitName();
		String dstPath = dstDirPath + "\\" + fileName;
		
    	//------------------------------------------------------
        //フォルダ整理（フォルダ存在を確認し、なければフォルダ作成）
        //------------------------------------------------------
		try {
			MyFiles.notExistsCreateDirectory(dstPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        //------------------------------------------------------
        //送信元フォルダへ移動(上書き)
        //------------------------------------------------------
    	MyUtils.SystemLogPrint("  ファイル移動: " + dstPath);
		try {
			MyFiles.moveOW(srcPath, dstPath);	//上書き
		} catch (NoSuchFileException e) {
			e.printStackTrace();
			return;	//★あやしいので、いったん、登録処理はしない。
		} catch (IOException e) {
			e.printStackTrace();
			return;	//★あやしいので、いったん、登録処理はしない。
		}
		
		//ファイル移動後、uploadFilePathを更新
		ocrData.setUploadFilePath(dstDirPath + "\\" + fileName);
		//各DBテーブルの更新
		try {
			OcrDataFormDAO.getInstance(config).updateUploadFilePath("00送信元なし", ocrData.getUnitName(), srcPath);
			FaxDataDAO.getInstance(config).updateUploadFilePath("00送信元なし", ocrData.getUnitName(), srcPath);
		} catch (SQLException e) {
			e.printStackTrace();
			return;	
		}
    	MyUtils.SystemLogPrint("changeFilePath: end");
	}

	//unitNameから情報引き当て、メール送信
	public void sendMailProcess(OcrDataFormBean ocrData, String pdfPath) throws Throwable {
		String unitName = ocrData.getUnitName();
		String soshinMoto = "";
		String mailBody1 = "";
		String mailBody2 = "";
		String flag = "0";
		String syubetsu = "";

		//フルパスからファイル名取得
		File file = new File(pdfPath);
		String fileName  = file.getName() ;
		//2 Excelオープン
		try {
			CellType ctype;
			// Excelファイルへアクセス(eclipse上でパスをしていないとプロジェクトパスになる)
			String xlsPath = ocrData.getTargetPath() + ocrData.getDocSetName() + "-pdf管理表.xlsm";
			MyExcel xlsx = new MyExcel();
			xlsx.openXlsm(xlsPath, null);
			MyUtils.SystemLogPrint("  Excelオープン..." + xlsPath);
			// シートを取得
			xlsx.setSheet("MAIL");
			flag = "0"; 
			//配信メール情報:From
			mailConf.fmAddr = xlsx.getStringCellValue(1, 1);
			//配信メール情報:Bcc
			mailConf.bccAddr = xlsx.getStringCellValue(4, 1);
			//配信メール情報:Body1,2
			mailBody1 = xlsx.getStringCellValue(6, 1);
			xlsx.getCell(7);
			xlsx.getCell(1);
			mailBody2 = xlsx.getStringCellValue(7, 1);
			mailConf.body = mailBody1 + "\n" + mailBody2 + "\n" + fileName + "\n";
			
			//FAX送信元 検索
			xlsx.setSheet("マスタ");
			soshinMoto = xlsx.search(3, unitName);	//結果をrowにセット。マッチしなければ最下行をセット。
			xlsx.getCell(6);				//G列
			mailConf.toAddr = xlsx.getStringCellValue();
			xlsx.getCell(7);				//H列
			mailConf.ccAddr = xlsx.getStringCellValue();
			xlsx.getCell(0);				//A列:FLAG
			ctype = xlsx.getCellType(0);
			if (ctype == CellType.NUMERIC) {
				int val = (int)xlsx.getNumericCellValue();
				flag =  String.valueOf(val);
			} else {
				flag = xlsx.getStringCellValue();
			}
			xlsx.getCell(1);				//B列:種別
			syubetsu = xlsx.getStringCellValue();
			//★toAddrが ブランクの時、送信元なしの全員宛先に 変更すること
			
			xlsx.close();
    	} catch (Throwable t) {
    	    //LOG.error("Failure during static initialization", t);
    	    throw t;
    	}

		//パターン①
		mailConf.subject = "TEST " + ocrData.getDocSetName() + "送信連絡" + "(" + ocrData.getCreatedAt() + " " + ocrData.getUnitName() + ")";
		mailConf.attach = pdfPath;
		if (ocrData.getRenkeiResult().equals("") != true) {
			mailConf.body = mailConf.body + "\n<OCR読取り結果>\n" + ocrData.getRenkeiResult();
		} else if (ocrData.getCheckResult().equals("") != true) {
			mailConf.body = mailConf.body + "\n<OCR読取り結果>\n" + ocrData.getCheckResult();
		}
		sendScanMail(ocrData.getDocSetName());

		//メール送信第2弾
		//本文に ocrData.getChubanlist()
		//sendScanMail(ocrData.getDocSetName());
	}

	public void sortMatchMailPorcess(OcrDataFormBean ocrData, String readValue) throws Throwable {
		String unitName = ocrData.getUnitName();
		String uploadFilePath = ocrData.getUploadFilePath();
		String soshinMoto = "";
		String mailBody1="";
		String mailBody2="";
		String flag = "0";
		String syubetsu = "";
		
    	MyUtils.SystemLogPrint("sortMatchMailPorcess: start");
		//仕分け帳票フォルダへファイル移動、uploadFilePathを更新
		String fileName = MyFiles.getFileName(uploadFilePath);	//パスからファイル名取得
		//2 Excelオープン
    	try {
			CellType ctype;
			// Excelファイルへアクセス(eclipse上でパスをしていないとプロジェクトパスになる)
			String xlsPath = ocrData.getTargetPath() + ocrData.getDocSetName() + "-pdf管理表.xlsm";
			MyExcel xlsx = new MyExcel();
			xlsx.openXlsm(xlsPath, null);
			MyUtils.SystemLogPrint("  Excelオープン..." + xlsPath);
			// シートを取得
			xlsx.setSheet("MAIL");
			flag = "0"; 
			//配信メール情報:From
			mailConf.fmAddr = xlsx.getStringCellValue(1, 1);
			//配信メール情報:Bcc
			mailConf.bccAddr = xlsx.getStringCellValue(4, 1);
			//配信メール情報:Body1,2
			mailBody1 = xlsx.getStringCellValue(6, 1);
			mailBody2 = xlsx.getStringCellValue(7, 1);
			mailConf.body = mailBody1 + "\n" + mailBody2 + "\n" + fileName + "\n";
			
			//FAX送信元 検索
			xlsx.setSheet("マスタ");
			soshinMoto = xlsx.search(3, unitName);	//結果をrowにセット。マッチしなければ最下行をセット。
			xlsx.getCell(6);				//G列
			mailConf.toAddr = xlsx.getStringCellValue();
			xlsx.getCell(7);				//H列
			mailConf.ccAddr = xlsx.getStringCellValue();
			xlsx.getCell(0);				//A列:FLAG
			ctype = xlsx.getCellType(0);
			if (ctype == CellType.NUMERIC) {
				int val = (int)xlsx.getNumericCellValue();
				flag =  String.valueOf(val);
			} else {
				flag = xlsx.getStringCellValue();
			}
			xlsx.getCell(1);				//B列:種別
			syubetsu = xlsx.getStringCellValue();
			//★toAddrが ブランクの時、送信元なしの全員宛先に 変更すること
			
			xlsx.close();
    	} catch (Throwable t) {
    	    //LOG.error("Failure during static initialization", t);
    	    throw t;
    	}

		//パターンx
		mailConf.subject = "TEST " + ocrData.getDocSetName() + "送信連絡" + "(" + ocrData.getCreatedAt() + " " + ocrData.getUnitName() + ")";
		mailConf.attach = uploadFilePath;
		if (readValue.equals("") == true) {
			if (ocrData.getRenkeiResult().equals("") != true) {
				mailConf.body = mailConf.body + "\n<OCR読取り結果>\n" + ocrData.getRenkeiResult() + "\n";
			} else if (ocrData.getCheckResult().equals("") != true) {
				mailConf.body = mailConf.body + "\n<OCR読取り結果>\n" + ocrData.getCheckResult() + "\n";
			}
		} else {
			if (ocrData.getRenkeiResult().equals("") != true) {
				mailConf.body = mailConf.body + "\n<OCR読取り結果>\n"
											  + readValue + "\n"
											  + ocrData.getRenkeiResult() + "\n";
			} else if (ocrData.getCheckResult().equals("") != true) {
				mailConf.body = mailConf.body + "\n<OCR読取り結果>\n"
											  + readValue + "\n"
											  + ocrData.getCheckResult() + "\n";
			} else {
				mailConf.body = mailConf.body + "\n<OCR読取り結果>\n"
											  + readValue + "\n";
			}
		}
		sendScanMail(ocrData.getDocSetName());
		MyUtils.SystemLogPrint("sortMatchMailPorcess: end");
	}

	public void sendSorting990Mail(OcrDataFormBean ocrData) {
		mailConf.fmAddr = mailConf.username;
		mailConf.toAddr = mailConf.username;
		mailConf.ccAddr = "";
		mailConf.bccAddr = "";
		mailConf.fmAddr =  "";
		
		mailConf.subject = "TEST " + ocrData.getDocSetName() + "仕分けNG連絡" + "(" + ocrData.getCreatedAt() + ")";
		mailConf.body = "各位\n\nいつもお世話になっております。プログラムからの自動送信です。\n\n"
					  + "ファイル: " + ocrData.getUploadFilePath() + "\n"
					  + "\nOCR仕分け結果: " + ocrData.getUnitName() + "\n";
		mailConf.attach =  "";
		
		sendScanMail(ocrData.getDocSetName());
	}

	//------------------------------------------------------
	//ファイル添付メール送信
	//------------------------------------------------------
	public int sendScanMail(String kyoten) {
		MyUtils.SystemLogPrint("  メール送信...");
		MyUtils.SystemLogPrint("  MAIL FmAddr: " + mailConf.fmAddr);
		MyUtils.SystemLogPrint("  MAIL ToAddr: " + mailConf.toAddr);
		MyUtils.SystemLogPrint("  MAIL CcAddr: " + mailConf.ccAddr);
		MyUtils.SystemLogPrint("  MAIL BcAddr: " + mailConf.bccAddr);
		MyUtils.SystemLogPrint("  MAIL Subject: " + mailConf.subject);
		if (kyoten.equals(this.SCAN_CLASS2) == true) {
			try {
				String jsonPath = ".\\data\\deleteinfo.json";
				JsonNode deleteInfo = MyFiles.parseJson(jsonPath);
				String diskInfo = "\n※サイズ情報: \n フォルダ " + this.targetPath
								+ " / サイズ: " + deleteInfo.get("TotalMBSize2").asText()
				 				+ "MB（2か月以前のpdfファイルは自動削除しています。）\n";
				mailConf.body = mailConf.body + diskInfo;
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
		MyUtils.SystemLogPrint("  MAIL Body: " + mailConf.body);
		MyUtils.SystemLogPrint("  MAIL Attach: " + mailConf.attach);
		mailConf.toAddr = mailConf.fmAddr;	//for debug
		mailConf.ccAddr = "";				//for debug
		mailConf.bccAddr = mailConf.fmAddr;	//for debug
		mailConf.sendRawMail();
		
		return 0;
	}
}
