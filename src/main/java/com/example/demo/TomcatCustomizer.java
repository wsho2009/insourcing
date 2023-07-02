package com.example.demo;

import java.net.InetAddress;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

//https://qiita.com/nmaolks/items/5476da5fccab1134320a
@Component
public class TomcatCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

	//@Value("${data.protcol}")
	private String protcol="AJP";
 
	@Override
    public void customize(TomcatServletWebServerFactory factory) {
		if (protcol.equals("AJP")) {
			System.out.println("Tomcat protocol：AJP");
	    	try {
	            InetAddress address = InetAddress.getByName("127.0.0.1");
		    	factory.setAddress(address); //ローカルを指定
		        factory.setProtocol("AJP/1.3"); //AJPを使用
		        factory.setPort(8010); //ポートを8009番で受付
		        factory.getTomcatConnectorCustomizers()
		        .add(c -> c.setProperty("secretRequired", "false")); //シークレットキーをFalseに変更
	    	} catch (Exception e) {
	    		//Tomcat編集エラー処理
	    	}
    	} else {
			System.out.println("Tomcat protocol：HTTP");
    	}
   	}
}
