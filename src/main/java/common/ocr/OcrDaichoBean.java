package common.ocr;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OcrDaichoBean implements Serializable {
	@JsonProperty("objectId")	String objectId;
	@JsonProperty("createdDate") String createdDate;
	@JsonProperty("ocrId") String ocrId;
	@JsonProperty("meisaiNo")	String meisaiNo;
	@JsonProperty("toriMei")	String toriMei;
	@JsonProperty("chumonBi") String chumonBi;
	@JsonProperty("sofuMei")	String sofuMei;
	@JsonProperty("chumonBango") String chumonBango;
	@JsonProperty("hinmei") String hinmei;
	@JsonProperty("suryo") String suryo;
	@JsonProperty("seizoTanka") String seizoTanka;
	@JsonProperty("kingaku") String kingaku;
	@JsonProperty("youkyuNouki") String youkyuNouki;
	@JsonProperty("biko") String biko;
	
    public String getObjectId() { return this.objectId; }
    public String getCreatedDate() { return this.createdDate; }
    public String getOcrId() { return this.ocrId; }
    public String getMeisaiNo() { return this.meisaiNo; }
    public String getToriMei() { return this.toriMei; }
    public String getChumonBi() { return this.chumonBi; }
    public String getSofuMei() { return this.sofuMei; }
    public String getChumonBango() { return this.chumonBango; }
    public String getHinmei() { return this.hinmei; }
    public String getSuryo() { return this.suryo; }
    public String getSeizoTanka() { return this.seizoTanka; }
    public String getKingaku() { return this.kingaku; }
    public String getYoukyuNouki() { return this.youkyuNouki; }
    public String getBiko() { return this.biko; }
	public String getAllData() {
		return this.objectId + "\t"
				+ this.createdDate + "\t"
				+ this.ocrId + "\t"
				+ this.meisaiNo + "\t"
				+ this.toriMei + "\t"
				+ this.chumonBi + "\t"
				+ this.sofuMei + "\t"
				+ this.chumonBango + "\t"
				+ this.hinmei + "\t"
				+ this.suryo + "\t"
				+ this.seizoTanka + "\t"
				+ this.kingaku + "\t"
				+ this.biko + "\r\n";		
	}    
	public OcrDaichoBean() {
   	}
}
