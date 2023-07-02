package common.ocr;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OcrFormBean implements Serializable {
    //@JsonProperty("No")	String No;
    @JsonProperty("Name") String formName;
    @JsonProperty("documentId")	String documentId;
    @JsonProperty("documentName") String documentName;
    //@JsonProperty("docsetId")	String docsetId;
    @JsonProperty("docsetName") String docsetName;
        
	public OcrFormBean() {
	}

	//public void setNo(String No) {this.No = No;	}
	public void setFormName(String Name) {this.formName = Name;	}
	public void setDocumentId(String documentId) {this.documentId = documentId;	}
	public void setDocumentName(String documentName)  {this.documentName = documentName;}
	//public void setDocsetId(String docsetId) {this.docsetId = docsetId;}
	public void setDocsetName(String docsetName)  {this.docsetName = docsetName; }
	
	//public String getNo() { return this.No;	}
	public String getFormName() { return this.formName;	}
	public String getDocumentId() { return this.documentId;	}
	public String getDocumentName()  { return this.documentName;}
	//public String getDocsetId() { return this.docsetId;}
	public String getDocsetName()  { return this.docsetName; }
}
