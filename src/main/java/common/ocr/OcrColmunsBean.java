package common.ocr;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;


public class OcrColmunsBean implements Serializable {
    @JsonProperty("title")	private String Title;
    @JsonProperty("name")	private String Name;
    @JsonProperty("width")	private String Width;
    @JsonProperty("type")	private String Type;
    @JsonProperty("mask")	private String Mask;
    //@JsonProperty("decimal")	private String decimal;

	public OcrColmunsBean(String title, String name, String width, String type) {
		this.Title = title;
		this.Name = name;
		this.Width = width;
		this.Type = type;
		this.Mask = null;
	}

	public OcrColmunsBean(String title, String name, String width, String type, String mask) {
		this.Title = title;
		this.Name = name;
		this.Width = width;
		this.Type = type;
		this.Mask = mask;
	}
}
