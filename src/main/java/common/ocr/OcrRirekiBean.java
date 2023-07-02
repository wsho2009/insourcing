package common.ocr;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OcrRirekiBean implements Serializable {
	
    @JsonProperty("COL0")	private String COL0;
    @JsonProperty("COL1")	private String COL1;
    @JsonProperty("COL2")	private String COL2;
    @JsonProperty("COL3")	private String COL3;
    @JsonProperty("COL4")	private String COL4;
    @JsonProperty("COL5")	private String COL5;
    @JsonProperty("COL6")	private String COL6;
    @JsonProperty("COL7")	private String COL7;
    @JsonProperty("COL8")	private String COL8;
    @JsonProperty("COL9")	private String COL9;
    //@JsonProperty("COL10")	private String COL10;
    //@JsonProperty("COL11")	private String COL11;
    //@JsonProperty("COL12")	private String COL12;
    //@JsonProperty("COL13")	private String COL13;

	public OcrRirekiBean() {
		//COL = new String[4];
	}
	
	public void setCOL(int index, String col) { 
		switch (index) {
		case 0: 
			this.COL0 = col; 
			break;
		case 1: 
			this.COL1 = col; 
			break;
		case 2: 
			this.COL2 = col; 
			break;
		case 3: 
			this.COL3 = col; 
			break;
		case 4: 
			this.COL4 = col; 
			break;
		case 5: 
			this.COL5 = col; 
			break;
		case 6: 
			this.COL6 = col; 
			break;
		case 7: 
			this.COL7 = col; 
			break;
		case 8: 
			this.COL8 = col; 
			break;
		case 9: 
			this.COL9 = col; 
			break;
/* 		case 10: 
			this.COL10 = col; 
			break;
		case 11: 
			this.COL11 = col; 
			break;
		case 12: 
			this.COL12 = col; 
			break;
		case 13: 
			this.COL13 = col; 
			break;*/
		}
	}
	
	public String getCOL(int index)  {
		switch (index) {
		case 0: 
			return this.COL0; 
		case 1: 
			return this.COL1; 
		case 2: 
			return this.COL2; 
		case 3: 
			return this.COL3; 
		case 4: 
			return this.COL4; 
		case 5: 
			return this.COL5; 
		case 6: 
			return this.COL6; 
		case 7: 
			return this.COL7; 
		case 8: 
			return this.COL8; 
		case 9: 
			return this.COL9; 
/*		case 10: 
			return this.COL10; 
		case 11: 
			return this.COL11; 
		case 12: 
			return this.COL12; 
		case 13: 
			return this.COL13;  */
		}
		return null;
	}
}
