package common.po;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.SQLException;

import com.example.demo.InsourcingConfig;

import common.utils.MyFiles;
import common.utils.MyUtils;

public class PoScanFile implements Runnable {
	String scanPath;
	InsourcingConfig config;

	public PoScanFile(InsourcingConfig argConfig) {
		MyUtils.SystemLogPrint("■PoScanFileコンストラクタ");
		config = argConfig;
		this.scanPath = config.getOcrUploadPath();
	}
	
	//public static void main(String[] args) throws Exception {
	@Override
	public void run() {
		ScanRemainedFile(this.scanPath);	//すでにフォルダにあるpdfをScan
		//指定ディレクトリ配下のファイルのみ(またはディレクトリ)を取得
		// https://qiita.com/fumikomatsu/items/67f012b364dda4b03bf1
		Path dir = Paths.get(this.scanPath);
	    WatchService watcher;
		try {
			watcher = FileSystems.getDefault().newWatchService();
			dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
			MyUtils.SystemLogPrint("■PoScanFile: scan: " + this.scanPath);
	        for (;;) {
	            WatchKey watchKey = watcher.take();
	            for (WatchEvent<?> event: watchKey.pollEvents()) {
	                if (event.kind() == OVERFLOW) continue;
	                //新規作成
	                if (event.kind() == ENTRY_CREATE) {
	                    WatchEvent<Path> ev = cast(event);
	                    Path name = ev.context();		//ファイル名
	                    Path src = dir.resolve(name);	//フルパス
	                    String fileName = name.toString();
	                    System.out.format("%s: %s %s\n", event.kind().name(), src, name);
	                    //String extension = fileName.substring(fileName.lastIndexOf("."));	//
	                    //String extension = fileName.substring(fileName.length()-3);	//拡張子：後ろから3文字
	                    //if (extension.equals("pdf") == true) {
	                		MyUtils.SystemLogPrint("  ファイル検出...: " + fileName);
							uploadProcess(src.toString());
						//}
	                }
	            }
	            watchKey.reset();
	        }
		} catch (IOException | InterruptedException e1) {
			e1.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

	void ScanRemainedFile(String scanPath) {
		//指定ディレクトリ配下のファイルのみ(またはディレクトリのみ)を取得
	    File file = new File(scanPath);
	    File fileArray[] = file.listFiles();
	    
		try {
			for (File f: fileArray){
				if (f.isFile()) {
					String fileName = MyFiles.getFileName(f.toString());
					MyUtils.SystemLogPrint("  ファイル検出...: " + fileName);
					uploadProcess(f.toString());
					importData(f.toString());
	            }
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	void uploadProcess(String uploadFilePath) throws Throwable {
	    //------------------------------------------------------
	    //取り込み実行
	    //------------------------------------------------------
		//https://blog.goo.ne.jp/xmldtp/e/beb03fb01fb1d1a2c37db8d69b43dcdd
		//コマンドラインから****.vbsを呼び出せる。
		String[] cmdList = new String[6];
		cmdList[0]	=	"cmd";
		cmdList[1]	=	"/c";
		cmdList[2]	=	"importChumon.vbs";		//VBSファイル指定
		cmdList[3]	=	"/file:importChumon.xlsm";
		cmdList[4]	=	"/method:run";
		cmdList[5]	=	"/importFilePath:" + uploadFilePath;
		System.out.print("  ");
		for (String cmd : cmdList)
			System.out.print(cmd + " ");
		System.out.print("\n");
		//https://qiita.com/mitsumizo/items/836ce2e00e91c33fcf95
		ProcessBuilder pb = new ProcessBuilder(cmdList);
		try {
			Process process = pb.start();
			System.out.println("  戻り値：" + process.waitFor());	//応答待ち
			try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
	            String line;
	            while ((line = r.readLine()) != null) {
	                MyUtils.SystemLogPrint(line);
	            }
	        }
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		//------------------------------------------------------
	    //フォルダ整理（フォルダ存在を確認し、なければフォルダ作成）
	    //------------------------------------------------------
	    String dstDirPath = this.scanPath + "done\\";
	    MyFiles.notExistsCreateDirectory(dstDirPath);
	    
	    //------------------------------------------------------
	    //フォルダへ退避（doneフォルダへ移動）
	    //------------------------------------------------------
		String fileName = MyFiles.getFileName(uploadFilePath);
	    String dstPath = this.scanPath + "done\\" + fileName;
		try {
			MyFiles.moveOW(uploadFilePath, dstPath);	//上書き移動
		} catch (NoSuchFileException e) {
			e.printStackTrace();
			return;	//★あやしいので、いったん、登録処理はしない。
		} catch (IOException e) {
			e.printStackTrace();
			return;	//★あやしいので、いったん、登録処理はしない。
		}
    	MyUtils.SystemLogPrint("  ファイル移動: " + dstPath);
	}

	void importData(String uploadFilePath) {
		PoUploadBean upload = null;
		try {
			upload = PoUploadDAO.getInstance(config).quertyWithUploadPath(uploadFilePath);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String userId = upload.getUserId();
		String datetime = upload.getDatetime();
		String triCd = upload.getToricd();
		String code = upload.getCode();
		//⇒引数として渡す
	}
}
