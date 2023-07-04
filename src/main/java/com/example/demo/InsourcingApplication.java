package com.example.demo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import common.fax.FaxScanFile;
import common.ocr.OcrProcess;
import common.po.PoScanFile;
import common.utils.MyUtils;

@SpringBootApplication
public class InsourcingApplication implements CommandLineRunner {

    @Autowired
    private InsourcingConfig config;
    private FaxScanFile scan1;
    private FaxScanFile scan2;
    private PoScanFile scan3;
    private boolean ocrExlusiveFlag;

	public static void main(String[] args) {
		SpringApplication.run(InsourcingApplication.class, args);
	}
	 
    @Override
    public void run(String... strings) throws Exception {
    	scan1 = new FaxScanFile(config, config.getScanDefTgt1()); 
    	new Thread(scan1).start();
		scan2 = new FaxScanFile(config, config.getScanDefTgt2()); 
		//new Thread(scan2).start();
		scan3 = new PoScanFile(config); 
		//new Thread(scan3).start();
		OcrProcess process = new OcrProcess(config, scan2);
        Timer timer = new Timer(); // 今回追加する処理
        ocrExlusiveFlag = false;
        TimerTask task = new TimerTask() {
            int count = 0;
            public void run() {
            // 定期的に実行したい処理
            count++;
            //MyUtils.SystemLogPrint(count + "回目のタスクが実行されました。");
            if (ocrExlusiveFlag == true ) {
            	MyUtils.SystemLogPrint("wait...");
            	return;
            }
            ocrExlusiveFlag = true;
            process.pollingReadingUnit();
            ocrExlusiveFlag = false;
            //---------------------------------
            //watchdog 書き込み処理
            try {
            	File file = new File(".\\data\\watchdog.dat");
            	FileWriter filewriter = new FileWriter(file);
            	filewriter.write(MyUtils.sdf.format(new Date())+"\n");
            	filewriter.write(count+"\n");
            	filewriter.close();
        	} catch (IOException e){
        		MyUtils.SystemErrPrint(e.toString());
        	}                
            //---------------------------------
    	}};
        timer.schedule(task, 0, 30000); // 30000ms間隔
   	}
}
