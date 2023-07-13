package com.example.demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.login.SecuritySession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.fax.FaxDataBean;
import common.fax.FaxDataDAO;
import common.fax.FaxDeleteFile;
import common.ocr.OcrColmunsBean;
import common.ocr.OcrDaichoBean;
import common.ocr.OcrDataFormBean;
import common.ocr.OcrDataFormDAO;
import common.ocr.OcrFormBean;
import common.ocr.OcrFormDAO;
import common.ocr.OcrRirekiBean;
import common.ocr.OcrRirekiDAO;
import common.po.PoFormBean;
import common.po.PoFormDAO;
import common.po.PoUploadBean;
import common.po.PoUploadDAO;
import common.utils.MyMail;
import common.utils.MyUtils;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class InsourcingController {
	
    @Autowired
    private InsourcingConfig config;

    @Autowired
    private SecuritySession securitySession;
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/fax")
    public String fax(Model model) {
		String title = "FAXデータリスト";
		String kyoten = config.getScanDefTgt2();
        System.out.println("kyoten: " + kyoten);
		
		ArrayList<FaxDataBean> list = null;
		try {
	        list = FaxDataDAO.getInstance(config).read(null, null, null, null, kyoten);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// 次の画面に値を渡す
		model.addAttribute("title", title);
		model.addAttribute("list", list);
		
		// 次の画面に遷移
		return "faxlist";
    }
    
    @PostMapping("/fax")
    public String faxPost(Model model, @RequestParam("form") String form, 
   			@RequestParam("date_fr") String date_fr, @RequestParam("date_to") String date_to, String soushimotonashi) {
		String kyoten = config.getScanDefTgt2();
        System.out.println("form: " + form);
        System.out.println("date_fr: " + date_fr);
        System.out.println("date_to: " + date_to);
        System.out.println("soushimotonashi: " + soushimotonashi);
        System.out.println("kyoten: " + kyoten);

		String title = "FAXデータリスト";
		
		ArrayList<FaxDataBean> list = null;
		try {
	        list = FaxDataDAO.getInstance(config).read(form, date_fr, date_to, soushimotonashi, kyoten);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// 次の画面に値を渡す
		model.addAttribute("title", title);
		model.addAttribute("list", list);
		
		// 次の画面に遷移
		return "faxlist";
    }
    
    @RequestMapping("/ocr/list")
    public String ocr(Model model){
		String title = "OCR結果一覧表";
		
		ArrayList<OcrDataFormBean> list = null;
		try {
	        list = OcrDataFormDAO.getInstance(config).read(null, null, null);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// 次の画面に値を渡す
		model.addAttribute("title", title);
		model.addAttribute("list", list);
		
		// 次の画面に遷移
		return "ocrlist";
    }
    
    @PostMapping("/ocr/list")
    public String ocrPost(Model model, @RequestParam("form") String form, 
   			@RequestParam("date_fr") String date_fr, @RequestParam("date_to") String date_to, String soushimotonashi) {
        System.out.println("form: " + form);
        System.out.println("date_fr: " + date_fr);
        System.out.println("date_to: " + date_to);

		String title = "OCR結果一覧表";
		
		ArrayList<OcrDataFormBean> list = null;
		try {
	        list = OcrDataFormDAO.getInstance(config).read(form, date_fr, date_to);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// 次の画面に値を渡す
		model.addAttribute("title", title);
		model.addAttribute("list", list);
		
		// 次の画面に遷移
		return "ocrlist";
    }

    @RequestMapping("/ocr/result")
    public String ocrResultGet(Model model, @RequestParam("unitId") String unitId, 
			@RequestParam("type") int type) {
		System.out.println("  unitId: " + unitId + "  type: " + type);

		String title = "OCR変換結果";
		// 次の画面に値を渡す
		model.addAttribute("title", title);
		model.addAttribute("unitId", unitId);
		model.addAttribute("type", type);
		
		// 次の画面に遷移
        return "result";
    }
    
    @PostMapping("/ocr/result")
    @ResponseBody	//＠ResponseBody アノテーションを付けることで、戻り値を HTTP レスポンスのコンテンツとすることができます。
    public Map<String, Object> ocrResultPost(
			@RequestParam("type") int type, @RequestParam("unitId") String unitId) {
        //レスポンス
		System.out.println("  unitId: " + unitId + "  type: " + type);
		
		ArrayList<OcrDataFormBean> dao = null;
		try {
	        dao = OcrDataFormDAO.getInstance(config).queryWithUnitId(unitId);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		OcrDataFormBean ocrDataForm = (OcrDataFormBean)dao.get(0);
		//String status = ocrDataForm.getStatus();
		String createdAt = ocrDataForm.getCreatedAt();
		String formName = ocrDataForm.getName();
		String documentId = ocrDataForm.getDocumentId();
		String colOutput = ocrDataForm.getColOutput();
		int colSuryo = ocrDataForm.getColSuryo();
		int colTanka = ocrDataForm.getColTanka();
		int colKingaku = ocrDataForm.getColKingaku();
		String fields = "";
		int dataWidth = 0;
		if (colOutput.equals("") != true) {
			dataWidth = 10;
			fields = colOutput;
		} else {
			int headerNum = ocrDataForm.getHeaderNum();
			int meisaiNum = ocrDataForm.getMeisaiNum();

			dataWidth = headerNum + meisaiNum + 1;	//結果列分 ＋１
			for (int c=0; c<dataWidth; c++)
				fields = fields + "COL" + (c+3) + ",";
			fields = fields.substring(0, fields.length() - 1);	//末尾 , 削除
		}
		int col_width[] = new int[dataWidth];

		ArrayList<OcrColmunsBean> columns = new ArrayList<OcrColmunsBean>();
        try {
			OcrRirekiBean header = OcrRirekiDAO.getInstance(config).readHeader(fields, documentId);
			for (int j=0; j<dataWidth; j++) {
				int len = header.getCOL(j).getBytes("Shift_JIS").length;	//S-JIS文字長
				if (col_width[j] < len) {
					col_width[j] = len;
				}
			}

			//System.out.println("header width");
			//for (int i=0; i<dataWidth; i++) {
			//	System.out.print(i + ":" + col_width[i] + " ");	//各カラムの文字数
			//}
			//System.out.println("");

	        //Javaオブジェクトに値をセット
			ArrayList<OcrRirekiBean> datalist;
			String title;
			if (type == 0) {
				datalist = OcrRirekiDAO.getInstance(config).readDataUnit(fields, unitId, 0);	//unitId			 
				title = "OCR変換結果: " + formName + "@" + createdAt;
			} else {
				datalist = OcrRirekiDAO.getInstance(config).readDataUnit(fields, formName, 1);//unitName			 
				title = "OCR変換結果: " + formName;
			}
			for (int i=0; i<datalist.size(); i++) {
				OcrRirekiBean rireki = datalist.get(i);
				for (int j=0; j<dataWidth; j++) {
					int len = rireki.getCOL(j).getBytes("Shift_JIS").length;
					if (col_width[j] < len) {
						col_width[j] = len;
					}
				}
			}
			//System.out.println("data width");
			//for (int i=0; i<dataWidth; i++) {
			//	System.out.print(i + ":" + col_width[i] + " ");	//各カラムの文字数
			//}
			System.out.println("");

			String width;
			OcrColmunsBean cols;
			for (int i=0; i<dataWidth; i++) {
				int colNo = i;
				if ((colSuryo != 0) && (colNo == colSuryo)) {
					width = Integer.valueOf(col_width[i]*11).toString();
					cols = new OcrColmunsBean(header.getCOL(i), "COL"+i, width, "numeric", "#,##");
				} else if ((colTanka != 0) && (colNo == colTanka)) {
					width = Integer.valueOf(col_width[i]*11).toString();	
					cols = new OcrColmunsBean(header.getCOL(i), "COL"+i, width, "numeric", "#,##.00");
				} else if ((colKingaku != 0) && (colNo == colKingaku)) {
					width = Integer.valueOf(col_width[i]*11).toString();
					cols = new OcrColmunsBean(header.getCOL(i), "COL"+i, width, "numeric", "#,##");
				} else {
					//文字列
					width = Integer.valueOf(col_width[i]*10).toString();
					cols = new OcrColmunsBean(header.getCOL(i), "COL"+i, width, "text");
				}

				columns.add(cols);
			}
	        ObjectMapper mapper = new ObjectMapper();
	        // 戻り値用のオブジェクト作成
	        Map<String, Object> resMap = new HashMap<>();
			resMap.put("title", title);
			resMap.put("columns", columns);
	        resMap.put("datalist", datalist);
	        try {
	            //JavaオブジェクトからJSONに変換
	        	String json = mapper.writeValueAsString(resMap);
	        	//String json = mapper.writeValueAsString(rireki);	//単体jsonのケース
	            //JSONの出力
	            System.out.println(json);
	            //return json;
	            return resMap;	//Stringで渡すのではなく、Objectで渡す。
	        } catch (JsonProcessingException e) {
	            e.printStackTrace();
	        }
            return null;
		} catch (SQLException e) {
			e.printStackTrace();
            return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
            return null;
		}
        //return null;
    }

	@PostMapping("/siwake/")
    //@ResponseBody	//＠ResponseBody アノテーションを付けることで、戻り値を HTTP レスポンスのコンテンツとすることができます。
    public void ocrSiwakePost(
			@RequestParam("uploadPath") String uploadPath) {
        //レスポンス
		System.out.println("  uploadPath: " + uploadPath);
		
		ArrayList<OcrDataFormBean> dao;
		dao = OcrDataFormDAO.getInstance(config).updateReShiwake(uploadPath);
    }

    @RequestMapping("/upload")
    public String upload(Model model){
		String userId = securitySession.getUsername();
		String userName = securitySession.getName();
		String code = securitySession.getCode();
		String title = "アップロード";

		// 次の画面(jsp)に値を渡す
		model.addAttribute("title", title);
		model.addAttribute("userId", userId);
		model.addAttribute("userName", userName);
		model.addAttribute("code", code);

		// 次の画面に遷移
		return "upload";
    }
    
    @PostMapping("/select")
    @ResponseBody	//＠ResponseBody アノテーションを付けることで、戻り値を HTTP レスポンスのコンテンツとすることができます。
    public ArrayList<PoFormBean> selectPost(@RequestParam("type") String type, @RequestParam("userId") String userId) {
        System.out.println("type: " + type + "  userId: " + userId);
        //Form list 
		try {
	        //Javaオブジェクトに値をセット
			ArrayList<PoFormBean> select = PoFormDAO.getInstance(config).read(userId);
	        ObjectMapper mapper = new ObjectMapper();
	        try {
	            //JavaオブジェクトからJSONに変換
	            String json = mapper.writeValueAsString(select);
	            //JSONの出力
	            System.out.println(json);
	            //out.write(json);
	            return select;
	        } catch (JsonProcessingException e) {
	            e.printStackTrace();
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return null;
    }
    
    @PostMapping("/rireki")	//アップロード履歴
    @ResponseBody	//＠ResponseBody アノテーションを付けることで、戻り値を HTTP レスポンスのコンテンツとすることができます。
    public PoUploadBean rirekiPost(@RequestParam("type") String type, @RequestParam("userId") String userId) {
        System.out.println("type: " + type + "  userId: " + userId);
	    //History list
		try {
	        //Javaオブジェクトに値をセット
			PoUploadBean rireki = PoUploadDAO.getInstance(config).read(userId);
	        ObjectMapper mapper = new ObjectMapper();
	        try {
	            //JavaオブジェクトからJSONに変換
	            String json = mapper.writeValueAsString(rireki);
	            //JSONの出力
	            System.out.println(json);
	            //out.write(json);
	            return rireki;
	        } catch (JsonProcessingException e) {
	            e.printStackTrace();
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return null;
    }
    
    @PostMapping("/upload")
    @ResponseBody	//＠ResponseBody アノテーションを付けることで、戻り値を HTTP レスポンスのコンテンツとすることができます。
    public String uploadPost(
    	@RequestParam("type") String type,
    	@RequestParam("file") final MultipartFile uploadFile,
    	@RequestParam("userId") String userId, 
    	@RequestParam("userName") String userName,
    	@RequestParam("code") String code,
    	@RequestParam("dt") String dt,
    	@RequestParam("formId") String formId,
    	@RequestParam("formName") String formName
    ) {
    	MyUtils.SystemLogPrint("■post:/upload start");
		// Upload file
    	//https://qiita.com/tseno/items/6f86dea212089d52ce28
    	if (uploadFile.isEmpty()) {
            return null;
        }
        System.out.println("filename: " + uploadFile.getOriginalFilename());      
        //userName = new String(userName.getBytes("iso-8859-1"), "utf-8");	//https://www.prime-architect.co.jp/myblog/google-app-engine-260
        System.out.println("userName: " + userName);      
        System.out.println("code: " + code);
        System.out.println("dt: " + dt);
        System.out.println("formId: " + formId);
        System.out.println("formName: " + formName);
        
		String ch = formId.substring(5, 6);		//6桁目
		String toriCd;
		if (ch.equals("") == true) {
			toriCd = formId.substring(0, 5);	//5桁
			formId = formId.substring(5, 10);	//5桁
		} else {
			toriCd = formId.substring(0, 6);	//6桁
			formId = formId.substring(6, 11);	//5桁
		}
		
		String dtStr = dt.substring(0,4) + dt.substring(5,7) + dt.substring(8,10) + dt.substring(11,13) + dt.substring(14,16) + dt.substring(17,19);
		String ext = uploadFile.getOriginalFilename().substring(uploadFile.getOriginalFilename().lastIndexOf("."));
		String contentType = uploadFile.getContentType();
		System.out.println("content-type: " + contentType);
		String fileName = toriCd + "_" + dtStr + ext;
		if (contentType.equals("application/pdf") == true) {
			String OCR_INPUT_PATH = config.getOcrInputPath();
			String inputFilePath = OCR_INPUT_PATH + fileName;
	        final Path path = Paths.get(OCR_INPUT_PATH, fileName);
	        final byte[] bytes;
	        try {
	            // アップロードされたファイルのバイナリを取得
	            bytes = uploadFile.getBytes();
	            // ファイルの格納
	            Files.write(path, bytes);
	        } catch (IOException e) {
	            return null;
	        }
	        System.out.println("inputfilePath: " + path.toString());

			//pdfファイルは、OCR登録
			registOcrProcess(formId, inputFilePath);
			
			//ファイルをアップロード後、履歴を登録。その際、OCR変換後のxlsxに変更する。
			ext = ".xlsx";
			String PO_UPLOAD_PATH = config.getOcrUploadPath();
			fileName = toriCd + "_" + dtStr + ext;
			String uploadFilePath = PO_UPLOAD_PATH + fileName;
			registUploadTable(userId, userName, dt, toriCd, uploadFilePath, code, inputFilePath);
		} else {
	        // アップロードされたファイルを格納するためのPathを取得
			String PO_UPLOAD_PATH = config.getOcrUploadPath();
	        final Path path = Paths.get(PO_UPLOAD_PATH, fileName);
	        final byte[] bytes;
	        try {
	            // アップロードされたファイルのバイナリを取得
	            bytes = uploadFile.getBytes();
	            // ファイルの格納
	            Files.write(path, bytes);
	        } catch (IOException e) {
	            return null;
	        }
	        System.out.println("filePath: " + path.toString());
	        
			//ファイルをアップロード後、履歴を登録
			String uploadFilePath = PO_UPLOAD_PATH + fileName;
			registUploadTable(userId, userName, dt, toriCd, uploadFilePath, code, "NO-OCR");

			/*			
			//uploadフォルダへ直接コピー
	    	src = Paths.get(inputFilePath);
			dst = Paths.get(uploadFilePath);
	    	try {
				Files.copy(src, dst, REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
			*/		
		}
		
		//ユーザーがアップロードしたことを通知するメール送信
		MyMail mailConf = new MyMail();
		mailConf.host = config.getMailHost();
		mailConf.port = config.getMailPort();
		mailConf.username = config.getMailUsername();
		mailConf.password = config.getMailPassword();
		mailConf.smtpAuth = config.getMailSmtpAuth();
		mailConf.starttlsEnable = config.getMailSmtpStarttlsEnable();

		mailConf.fmAddr = mailConf.username;
		mailConf.toAddr = mailConf.username;
		mailConf.ccAddr = "";
		mailConf.bccAddr = "";
		
		mailConf.subject = "アップロードされました。";
		mailConf.body = "ID: " + userId + "\n"
					  + "dt: " + dt  + "\n"
				   	  + "toriCd: " + toriCd + "\n"
				   	  + "file: " + fileName + "\n";
		String attach = null;
		MyUtils.SystemLogPrint("  メール送信...");
		MyUtils.SystemLogPrint("  MAIL FmAddr: " + mailConf.fmAddr);
		MyUtils.SystemLogPrint("  MAIL ToAddr: " + mailConf.toAddr);
		MyUtils.SystemLogPrint("  MAIL CcAddr: " + mailConf.ccAddr);
		MyUtils.SystemLogPrint("  MAIL BcAddr: " + mailConf.bccAddr);
		MyUtils.SystemLogPrint("  MAIL Subject: " + mailConf.subject);
		MyUtils.SystemLogPrint("  MAIL Body: " + mailConf.body);
		mailConf.sendRawMail();
        
        //レスポンス
		return "{\"result\":\"ok\"}";
    }
    
	//OCR登録処理
	private void registOcrProcess(String documentId, String uploadFilePath) {
		OcrFormBean ocrForm = null;
		try {
	        //Javaオブジェクトに値をセット
			ocrForm = OcrFormDAO.getInstance(config).queryOcrFormInfoDocumentId(documentId);
	        ObjectMapper mapper = new ObjectMapper();
	        try {
	            //JavaオブジェクトからJSONに変換
	            String json = mapper.writeValueAsString(ocrForm);
	            //JSONの出力
	            System.out.println(json);
	        } catch (JsonProcessingException e) {
	            e.printStackTrace();
	        }
			
			OcrDataFormBean ocrData = new OcrDataFormBean();
			//ocrData.setUnitId("");
			ocrData.setUnitName(ocrForm.getFormName());
			ocrData.setUploadFilePath(uploadFilePath);
			ocrData.setStatus("REGIST");
			//ocrData.setCsvFileName("");
			//ocrData.setDocsetId("");
			ocrData.setDocsetName(ocrForm.getDocsetName());
			ocrData.setDocumentId(documentId);
			ocrData.setDocumentName(ocrForm.getDocumentName());
			//ocrData.setCreatedAt("");
			ocrData.setType(2);
			OcrDataFormDAO.getInstance(config).insertReadingUnitDB(ocrData);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void registUploadTable(String userId, String userName, String dt, String toriCd, String uploadFilePath, String code, String inputFilePath) {
		PoUploadBean poUpload = new PoUploadBean();
		poUpload.setUserId(userId);
		poUpload.setUserName(userName);
		poUpload.setDatetime(dt);
		poUpload.setToriCd(toriCd);
		poUpload.setUploadPath(uploadFilePath);
		poUpload.setCode(code);
		poUpload.setInputPath(inputFilePath);
        ObjectMapper mapper = new ObjectMapper();
        try {
            //JavaオブジェクトからJSONに変換
            String json = mapper.writeValueAsString(poUpload);
            //JSONの出力
            System.out.println(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
		PoUploadDAO.getInstance(config).registData(poUpload);
	}
	
    @RequestMapping("/daicho")
    public String daicho(Model model){
		String title = "台帳データリスト";
		
		ArrayList<OcrDaichoBean> list;
		try {
	        list = OcrRirekiDAO.getInstance(config).getDaicho();
		} catch (SQLException e) {
			list = null;
			e.printStackTrace();
		}
		// 次の画面に値を渡す
		model.addAttribute("title", title);
		model.addAttribute("list", list);
		
		// 次の画面に遷移
		return "daicholist";
    }
	
    //curl -X POST http://localhost/fax/delete
    @RequestMapping(value = "/fax/delete", method = RequestMethod.POST)
    public String faxDeletePost() {
		
    	String scanPath = config.getScanPath2();
    	FaxDeleteFile delete = new FaxDeleteFile(scanPath);
    	delete.run();
    	
    	return "post_ok";
    }
    
    @RequestMapping(value="/api/daicho", method = RequestMethod.GET)
    public void apiDaicho(HttpServletResponse response){
		ArrayList<OcrDaichoBean> list;
		try {
	        list = OcrRirekiDAO.getInstance(config).getDaicho();
	        
			response.setContentType("text/tsv;charset=UTF8");
			String fileName = new String("daicho.tsv".getBytes("Shift_JIS"), "ISO-8859-1");
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
			PrintWriter writer = response.getWriter();
  			String line;
			//ヘッダ
			line = "ID\t日時\tOCR\tNo\t取引先\t日付\t送付先\t注番\t品名\t数量\t単価\t金額\t備考\r\n";
			writer.write(line);
			//データ
			for (int i=0; i<list.size(); i++) {
				OcrDaichoBean dto = (OcrDaichoBean)list.get(i);
	  			line = dto.getAllData();
	  			writer.write(line);
			}
			writer.close();
		} catch (SQLException | IOException e) {
			list = null;
			e.printStackTrace();
		}	
    }
}
	