package common.fax;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FaxDataBean implements Serializable {
	//FaxData
	@JsonProperty("No") int no;
    @JsonProperty("faxNo") String faxNo;
    @JsonProperty("cDate") String cDate;
    @JsonProperty("cTime") String cTime;
    @JsonProperty("jobNo") String jobNo;
    @JsonProperty("fileName") String fileName;
    @JsonProperty("soshinMoto") String soshinMoto;
    @JsonProperty("fullPath") String fullPath;
    @JsonProperty("kyoten") String kyoten;
	
	public FaxDataBean() {
    }

	public void setNo(int no) { this.no = no; }
	public void setFaxNo(String faxNo) { this.faxNo = faxNo; }
	public void setCDate(String cDate) { this.cDate = cDate; }
	public void setCTime(String cTime) { this.cTime = cTime; }
	public void setJobNo(String jobNo) { this.jobNo = jobNo; }
	public void setFileName(String fileName) { this.fileName = fileName; }
    public void setSoshinMoto(String soshinMoto) {this.soshinMoto = soshinMoto;	}
    public void setFullPath(String fullPath) {this.fullPath = fullPath;	}
    public void setKyoten(String kyoten) {this.kyoten = kyoten;	}
    public void setFaxData(String faxNo, String cDate, String cTtime, String jobNo, String fileName, String soshinMoto, String fullPath, String kyoten) {
        this.faxNo = faxNo; this.cDate = cDate; this.cTime = cTtime; this.jobNo = jobNo; this.fileName = fileName; this.soshinMoto = soshinMoto; 
        this.fullPath = fullPath; this.kyoten = kyoten; }

    public int getNo() { return this.no; }
    public String getFaxNo() { return this.faxNo; }
    public String getCDate() { return this.cDate; }
    public String getCTime() { return this.cTime; }
    public String getJobNo() { return this.jobNo; }
    public String getFileName() { return this.fileName; }
    public String getSoshinMoto() { return this.soshinMoto; }
    public String getFullPath() { return this.fullPath; }
    public String getKyoten() { return this.kyoten; }

    public String getFaxDataText() {
		String line  =
		this.faxNo + "\t" + this.cDate + "\t" + this.cTime + "\t" + this.jobNo + "\t" + 
		this.fileName + "\t" + this.soshinMoto + "\t" + this.fullPath + "\t" + this.kyoten;

		return line;
	}
}
