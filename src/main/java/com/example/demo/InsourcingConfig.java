package com.example.demo;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("insourcing")
public class InsourcingConfig {
 
    private Map<String,String> mail;
    private Map<String,String> datasource;
    private Map<String,String> proxy;
    private Map<String,String> scan;
    private Map<String,String> ocr;
    
    public Map<String, String> getMail() {	return mail;	}
    public Map<String, String> getDatasource() {	return datasource;	}
    public Map<String, String> getProxy() {	return proxy;	}
    public Map<String, String> getScan() {	return scan;	}
    public Map<String, String> getOcr() {	return ocr;	}

    public void setMail(Map<String, String> mail) {	this.mail = mail;	}
    public void setDatasource(Map<String, String> datasource) {	this.datasource = datasource;	}
    public void setProxy(Map<String, String> proxy) {	this.proxy = proxy;	}
    public void setScan(Map<String, String> scan) {	this.scan = scan;	}
    public void setOcr(Map<String, String> ocr) {	this.ocr = ocr;	}
    
    public String getMailHost(){	return mail.get("host");	}
    public String getMailPort(){	return mail.get("port");    }
    public String getMailUsername(){	return mail.get("username");    }
    public String getMailPassword(){	return mail.get("password");    }
    public String getMailSmtpAuth(){	return mail.get("smtp_auth");    }
    public String getMailSmtpStarttlsEnable(){	return mail.get("smtp_starttls_enable");    }
    
    public String getDBUrl(){	return datasource.get("url");	}
    public String getDBUsername(){	return datasource.get("username");    }
    public String getDBPassword(){	return datasource.get("password");    }
    public String getDBDriverClassName(){	return datasource.get("driverClassName");    }
    
    public String getProxyHost(){	return proxy.get("host");	}
    public String getProxyPort(){	return proxy.get("port");    }
    public String getProxyUsername(){	return proxy.get("username");    }
    public String getProxyPassword(){	return proxy.get("password");    }
      
    public String getScanDefTgt1(){	return scan.get("def_tgt1");	}
    public String getScanDefTgt2(){	return scan.get("def_tgt2");    }
    public String getScanPath1(){	return scan.get("path1");    }
    public String getScanPath2(){	return scan.get("path2");    }
    
    public String getOcrUploadPath(){	return ocr.get("upload_path");	}
    public String getOcrInputPath(){	return ocr.get("input_path");    }
    public String getOcrOutputPath(){	return ocr.get("output_path");    }
    public String getOcrHostUrl(){	return ocr.get("host_url");    }
    public String getOcrAddPage(){	return ocr.get("add_page");    }
    public String getOcrAddSort(){	return ocr.get("add_sort");    }
    public String getOcrReadUnit(){	return ocr.get("read_unit");    }
    public String getOcrReadSort(){	return ocr.get("read_sort");    }
    public String getOcrUnitExport(){	return ocr.get("unit_export");    }
    public String getOcrLinkEntry(){	return ocr.get("link_entry");    }
    public String getOcrApiKey(){	return ocr.get("api_key");    }
    public String getOcrApiKeyValye(){	return ocr.get("api_key_value");    }
    public String getOcrUserId(){	return ocr.get("user_id");    }
    
}