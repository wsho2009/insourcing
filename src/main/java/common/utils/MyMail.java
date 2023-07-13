package common.utils;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MyMail {
    public String host;
    public String port;
    public String username;
    public String password;
    public String smtpAuth;
    public String starttlsEnable;

    public String fmAddr;
    public String toAddr;
    public String ccAddr;
    public String bccAddr;
    public String subject;
    public String body;
    public String attach;
    //PasswordAuthentication用
    static class MailAuth {
    	String username;
    	String password;
    }
    static MailAuth auth = new MyMail.MailAuth();

    public void sendRawMail() 
	{
        // メール送信のプロパティ設定
        Properties properties = new Properties();
        properties.put("mail.smtp.host", this.host);	// ホスト
        properties.put("mail.smtp.port", this.port);	// ポート指定（サブミッションポート）
		properties.put("mail.smtp.auth", this.smtpAuth);	// 認証
        properties.put("mail.smtp.starttls.enable", this.starttlsEnable);		// STARTTLSによる暗号化
        //properties.put("mail.smtp.debug", "true");
        // タイムアウト
        properties.put("mail.smtp.connectiontimeout", "5000");
        properties.put("mail.smtp.timeout", "5000");
        
        auth.username = username;
        auth.password = password;
        try {
            // セッションの作成。
            final Session session = Session.getInstance(properties , new javax.mail.Authenticator(){
                protected PasswordAuthentication getPasswordAuthentication(){
                    // Gmailなどを使う場合はユーザ名とパスワードを引数に指定。
                    return new PasswordAuthentication(auth.username, auth.password);
                }
            });
            final Message message = new MimeMessage(session);

            // 基本情報
            message.setFrom(new InternetAddress(this.fmAddr));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(this.toAddr, false));
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(this.ccAddr, false));
            message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(this.bccAddr, false));

            // タイトル
            message.setSubject(this.subject);

			if (this.attach != null && this.attach.equals("")!= true) {
				// メッセージ本文
				MimeBodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setText(this.body);
				
				// １：添付ファイル１を添付するボディパートを取得
				MimeBodyPart attachedFilePart1 = new MimeBodyPart();
				// ２：添付ファイル１のデータソースを取得
				FileDataSource fs = new FileDataSource(this.attach);
				// ３：ボディパート１に添付ファイル１を添付
				attachedFilePart1.setDataHandler(new DataHandler(fs));
				//attachedFilePart1.setFileName(MimeUtility.encodeWord(fs1.getName()));
				//java mailの添付ファイル名が文字化けした際の解決策
				//https://qiita.com/esgr_dxx/items/08ac2e5e0e2be99482f5
				attachedFilePart1.setFileName(fs.getName());
				System.setProperty("mail.mime.encodefilename", "true");
				System.setProperty("mail.mime.charset", "UTF-8");
				
				// ７：メールに、本文・添付１・添付２の３つを添付
				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);
				multipart.addBodyPart(attachedFilePart1);
				message.setContent(multipart);
			} else {
				message.setText(this.body);
			}
			message.setHeader("Content-Transfer-Encoding", "base64");

			// ８：メールを送信する
			Transport.send(message);

        } catch (Exception e) {
        	System.out.print("例外が発生！\r\n");
            e.printStackTrace();
            //throw new InternalServerErrorException(e);
        }
        fmAddr = "";    //誤送信防ぐために送信後クリア
        toAddr = "";
        ccAddr = "";
        bccAddr = "";
        subject = "";
        body = "";
        attach = "";
    
    }
}
