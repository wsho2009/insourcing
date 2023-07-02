package common.ocr;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OcrDataFormBean implements Serializable {
	//OcrData
	@JsonProperty("unitId")	String unitId;
	@JsonProperty("unitName") String unitName;
	@JsonProperty("uploadFilePath")	String uploadFilePath;
	@JsonProperty("status")	String status;
	@JsonProperty("csvFileName") String csvFileName;
	@JsonProperty("createdAt")	String createdAt;
	@JsonProperty("linkUrl") String linkUrl;
	@JsonProperty("type") int type;
	
	//OcrForm
	@JsonProperty("No") String No;
    @JsonProperty("formName") String formName;
    @JsonProperty("documentId") String documentId;
    @JsonProperty("documentName") String documentName;
    @JsonProperty("docsetId") String docsetId;
    @JsonProperty("docsetName") String docsetName;
    
    @JsonProperty("rotateInfo") String rotateInfo;
    @JsonProperty("headerNum") int headerNum;
    @JsonProperty("meisaiNum") int meisaiNum;
    @JsonProperty("colChuban") int colChuban;
    @JsonProperty("colHinmei") int colHinmei;
    @JsonProperty("colSuryo") int colSuryo;
    @JsonProperty("colTanka") int colTanka;
    @JsonProperty("colKingaku") int colKingaku;
    @JsonProperty("colOutput") String colOutput;	
    @JsonProperty("colToriCd") String colToriCd;	
    //以下、DBなし
    //@JsonProperty("toAddr") String toAddr;	
    //@JsonProperty("ccAddr") String ccAddr;	
    @JsonProperty("targetPath") String targetPath;
	@JsonProperty("mailFlag") int mailFlag;	
	@JsonProperty("outFoloderPath") String outFolderPath;
	@JsonProperty("checkResult") String checkResult;
	@JsonProperty("renkeiResult") String renkeiResult;
	@JsonProperty("chubanDblFlag") boolean chubanDblFlag;
	@JsonProperty("chumonNGFlag") boolean chumonNGFlag;
	@JsonProperty("numericNGFlag") boolean numericNGFlag;
	@JsonProperty("chubanlist") String chubanlist;
    
	public OcrDataFormBean() {
   }

	public String getUnitId() {return this.unitId;	}
	public String getUnitName() {return this.unitName;	}
	public String getUploadFilePath() {return this.uploadFilePath;	}
	public String getStatus() {return this.status;	}
	public String getCreatedAt() {return this.createdAt;	}
	public String getLinkUrl() {return this.linkUrl;	}
	public String getDocSetName() {return this.docsetName;	}
	public String getTargetPath() {return this.targetPath;	}
	public String getCheckResult() {return this.checkResult;	}
	public String getRenkeiResult() {return this.renkeiResult;	}
	public String getChubanlist() {return this.chubanlist;	}
	public String getRotateInfo()  {return this.rotateInfo = (rotateInfo == null? "" : rotateInfo);}
	public String getName() {return formName;}
	public String getDocumentId() {return this.documentId; }
	public int getHeaderNum()  {return this.headerNum; }
	public int getMeisaiNum()  {return this.meisaiNum; }
	public int getColChuban()  {return this.colChuban; }
	public int getColHinmei()  {return this.colHinmei; }
	public int getColSuryo()  {return this.colSuryo; }
	public int getColTanka()  {return this.colTanka; }
	public int getColKingaku()  {return this.colKingaku; }
	public String getColOutput()  {return this.colOutput; }
	public String getColToriCd()  {return this.colToriCd; }
	
	public void setUnitId(String unitId) {this.unitId = unitId;	}
	public void setUnitName(String unitName) {this.unitName = unitName;	}
	public void setUploadFilePath(String uploadFilePath) {this.uploadFilePath = uploadFilePath;	}
	public void setStatus(String status) {this.status = status;	}
	public void setCsvFileName(String csvFileName) {this.csvFileName = csvFileName;	}
	public void setCreatedAt(String createdAt) {this.createdAt = createdAt;}
	public void setLinkUrl(String linkUrl) {this.linkUrl = linkUrl;}
	public void setType(int type) {this.type = type;}

	public void setNo(String No) {this.No = No;	}
	public void setName(String formName) {this.formName = formName;	}
	public void setDocumentId(String documentId) {this.documentId = documentId;	}
	public void setDocumentName(String documentName)  {this.documentName = documentName;}
	public void setDocsetId(String docsetId) {this.docsetId = docsetId;}
	public void setDocsetName(String docsetName)  {this.docsetName = docsetName; }
	public void setRotateInfo(String rotateInfo)  {this.rotateInfo = (rotateInfo == null? "" : rotateInfo);}
	public void setHeaderNum(int headerNum)  {this.headerNum = headerNum; }
	public void setMeisaiNum(int meisaiNum)  {this.meisaiNum = meisaiNum; }
	public void setColChuban(int colChuban)  {this.colChuban = colChuban; }
	public void setColHinmei(int colHinmei)  {this.colHinmei = colHinmei; }
	public void setColSuryo(int colSuryo)  {this.colSuryo = colSuryo; }
	public void setColTanka(int colTanka)  {this.colTanka = colTanka; }
	public void setColKingaku(int colKingaku)  {this.colKingaku = colKingaku; }
	public void setColOutput(String colOutput)  {this.colOutput = colOutput; }
	public void setColToriCd(String colToriCd)  {this.colToriCd = colToriCd; }

}
