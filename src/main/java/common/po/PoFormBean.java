package common.po;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PoFormBean implements Serializable {

    @JsonProperty("no")	private int no;
    @JsonProperty("code")		private String code;
    @JsonProperty("formId")		private String formId;
    @JsonProperty("formName")	private String formName;
    @JsonProperty("member")		private String member;
	
	public PoFormBean() {
	}

	public void setNo(int no) { this.no = no; }
	public void setCode(String code) { this.code = code; }
	public void setFormId(String FormId) { this.formId = FormId; }
	public void setFormName(String FormName) { this.formName = FormName; }
	public void setMember(String member) { this.member = member; }

	public int getId() { return this.no; }
	public String getCode()  { return this.code; }
	public String getFormId() { return this.formId; }
	public String getFormName() { return this.formName; }
	public String getMember() { return this.member; }
}
