package dbchange;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;

import common.utils.MyMail;
import common.utils.MyQueue;
import common.utils.MyUtils;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleDriver;
import oracle.jdbc.OracleStatement;
import oracle.jdbc.dcn.DatabaseChangeEvent;
import oracle.jdbc.dcn.DatabaseChangeListener;
import oracle.jdbc.dcn.DatabaseChangeRegistration;
 
//https://docs.oracle.com/cd/E16338_01/java.112/b56281/dbchgnf.htm
public class DBChangeNotification {

	static String DB_URL;
	static String DB_USER;
	static String DB_PASS;
	static String DB_DRIVER;
	static MyMail mailConf;
	
	public static void main(String[] argv) {
		ResourceBundle rb = ResourceBundle.getBundle("prop");
		DB_URL = rb.getString("DB_URL");
		DB_USER = rb.getString("DB_USER");
		DB_PASS = rb.getString("DB_PASS");
		DB_DRIVER = rb.getString("DB_DRIVER");
		
		mailConf = new MyMail();
		mailConf.host = rb.getString("MAIL_HOST");
		mailConf.port = rb.getString("MAIL_PORT");
		mailConf.username = rb.getString("MAIL_USER");
		mailConf.password = rb.getString("MAIL_PASS");
		mailConf.smtpAuth = rb.getString("MAIL_SMTP_AUTH");
		mailConf.starttlsEnable = rb.getString("MAIL_SMTP_STARTTLS_ENABLE");
		
	    DBChangeNotification demo = new DBChangeNotification();
	    try {
	      	demo.run();
	    } catch(SQLException mainSQLException ) {
	      	mainSQLException.printStackTrace();
	    }
	}   
	    
    void run() throws SQLException
    {
        OracleConnection conn = connect();
          
        // first step: create a registration on the server:
        Properties prop = new Properties();
        
        // if connected through the VPN, you need to provide the TCP address of the client.
        // For example:
        // prop.setProperty(OracleConnection.NTF_LOCAL_HOST,"14.14.13.12");
   	
        // Ask the server to send the ROWIDs as part of the DCN events (small performance
        // cost):
        prop.setProperty(OracleConnection.DCN_NOTIFY_ROWIDS, "true");
	    // 
	    //Set the DCN_QUERY_CHANGE_NOTIFICATION option for query registration with finer granularity.
        prop.setProperty(OracleConnection.DCN_QUERY_CHANGE_NOTIFICATION, "true");
	    
        // The following operation does a roundtrip to the database to create a new
        // registration for DCN. It sends the client address (ip address and port) that
        // the server will use to connect to the client and send the notification
        // when necessary. Note that for now the registration is empty (we haven't registered
        // any table). This also opens a new thread in the drivers. This thread will be
        // dedicated to DCN (accept connection to the server and dispatch the events to 
        // the listeners).
        DatabaseChangeRegistration dcr = conn.registerDatabaseChangeNotification(prop);
        MyQueue MyQueue = new MyQueue();
        try {
            // add the listenerr:
            DCNDemoListener list = new DCNDemoListener(this, MyQueue);
            dcr.addListener(list);
             
            // second step: add objects in the registration:
            Statement stmt = conn.createStatement();
            // associate the statement with the registration:
            ((OracleStatement)stmt).setDatabaseChangeRegistration(dcr);
            ResultSet rs = stmt.executeQuery("select NAME from USER_DATA");
            while (rs.next())
            {}
            String[] tableNames = dcr.getTables();
            for(int i=0;i<tableNames.length;i++)
            	System.out.println(tableNames[i]+" is part of the registration.");
            rs.close();
            stmt.close();
        } catch(SQLException ex) {
            // if an exception occurs, we need to close the registration in order
            // to interrupt the thread otherwise it will be hanging around.
            if (conn != null)
              	conn.unregisterDatabaseChangeNotification(dcr);
            throw ex;
        } finally {
            try {
                // Note that we close the connection!
                conn.close();
            } catch(Exception innerex){ innerex.printStackTrace(); }
        }
        
        while (true) {
        	Object obj = MyQueue.get();
            System.out.println(obj);
            //MyUtils.SystemLogPrint((String)obj);
            
            StatusDataBean slist = new StatusDataBean();
            slist.queryDB();
            MyUtils.SystemLogPrint(slist.getDataText());
            String taskName = slist.getTaskName();
            String taskStatus = slist.getTaskStatus();
            if (taskStatus.equals("0")==true || taskStatus.equals("1")==true) {
            	//0:待機中 / 1:実行中
            	if (taskName.equals("連携1")==true || taskName.equals("連携2")==true) {
            		;
            	}
            	return;
            } else if (taskStatus.equals("2")==true) {
            	//2:正常終了
            	taskStatus = "正常終了";
            } else if (taskStatus.equals("3")==true) {
            	//3:エラー停止
            	taskStatus = "エラー停止";
            } else if (taskStatus.equals("2")==true) {
            	//4:警告終了
            	taskStatus = "警告終了";
            } else {
            	taskStatus = "????";
            }
            String task;
            if (taskName.equals("一括登録")) {
            	task = taskName + "/" + slist.getUserId();
            } else if (taskName.equals("データ取り込み")) {
            	task = taskName + " 件数:" + "?"; 
            } else if (taskName.equals("連携1")==true || taskName.equals("連携2")==true) {
            	task = taskName;
            } else {
            	task = taskName;
            }
            String msg = MyUtils.sdf.format(new Date()) + "TASK " + task + ", " + taskStatus+ ", "; 
            mailConf.fmAddr = mailConf.username;
            mailConf.toAddr = mailConf.fmAddr;	//for debug
    		mailConf.ccAddr = "";				//for debug
    		mailConf.bccAddr = mailConf.fmAddr;	//for debug
    		mailConf.subject = "[システム]" + msg;
    		mailConf.body = msg;
    		mailConf.attach =  "";
    		mailConf.sendRawMail();            
        }
        
        // At the end: close the registration (comment out these 3 lines in order
        // to leave the registration open).
        //OracleConnection conn2 = connect();
        //conn2.unregisterDatabaseChangeNotification(dcr);
        //conn2.close();
    }
    
	// Creates a connection the database.
    OracleConnection connect() throws SQLException {
        OracleDriver dr = new OracleDriver();
        Properties prop = new Properties();
        prop.setProperty("user", DBChangeNotification.DB_USER);
        prop.setProperty("password", DBChangeNotification.DB_PASS);
        
        return (OracleConnection)dr.connect(DBChangeNotification.DB_URL, prop);
    }			    
}

//DCN listener: it prints out the event details in stdout.
class DCNDemoListener implements DatabaseChangeListener {
    DBChangeNotification demo;
    MyQueue MyQueue;
	int count = 0;
    DCNDemoListener(DBChangeNotification dem, MyQueue queu) { 
        demo = dem;
        MyQueue = queu;
    }
    
    public void onDatabaseChangeNotification(DatabaseChangeEvent e) {
        //Thread t = Thread.currentThread();
        //MyUtils.SystemLogPrint("DCNDemoListener: got an event ("+this+" running on thread "+t+")");
    	count++;
        MyQueue.put(count);
    }
}
