package common.fax;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
//import java.util.ResourceBundle;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.utils.MyUtils;

public class FaxDeleteFile {
	static String targetPath;
	static int daysOfDeletion;
	static String backupPath;
	static long deleteCount;
	static long totalCount;
	static long deleteSize;
	static long totalSize;
	static int mbSize;
	
	public FaxDeleteFile() {
		daysOfDeletion = 60;	//日
		backupPath = "\\backup";
		deleteCount = 0;
		totalCount = 0;
		deleteSize = 0;
		totalSize = 0;
		mbSize = 0;
	}
	
	public static void main(String[] args) {
		if (args.length > 0) {
			targetPath = args[0];
		} else {
			MyUtils.SystemErrPrint("対象フォルダを指定してください。");
			return; 
		}
		FaxDeleteFile delete = new FaxDeleteFile();
		
		MyUtils.SystemLogPrint("「" + targetPath + "」フォルダ内のファイル更新日が"
							+ daysOfDeletion + "日以上のものを削除します。");
		scanDeleteFile(targetPath, targetPath + backupPath, true);
		MyUtils.SystemLogPrint("「" + targetPath + "」フォルダ内のファイル更新日"
				+ daysOfDeletion + "日以上のものを削除しました(ファイル数: " + deleteCount +")。");
		totalCount = totalCount - deleteCount;
		totalSize = totalSize - deleteSize;
		mbSize = Math.round(totalSize/(1024*1024));
		MyUtils.SystemLogPrint("トータルファイル数:" + totalCount + " 削除ファイル数: " + deleteCount
				+ " サイズ(MB): " + mbSize);
		
		DeleteInfo info = new DeleteInfo();
		info.TotalMBSize2 = mbSize;
		info.TotalCount2 = totalCount;
		ObjectMapper mapper = new ObjectMapper();
		String json = null;
		try {
			//JavaオブジェクトからJSONに変換
			json = mapper.writeValueAsString(info);
			MyUtils.SystemLogPrint(json);	//JSONの出力
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		try {
			File file = new File(".\\data\\deleteinfo.json");
			FileWriter filewriter = new FileWriter(file);
			filewriter.write(json);
			filewriter.close();
		} catch (IOException e){
			MyUtils.SystemErrPrint(e.toString());
		}                
	}

	private static void scanDeleteFile(String targetPath, String backupPath, boolean deleteFlag) {
		//指定ディレクトリ肺のファイルのみ(またはディレクトリのみ)を取得
        File file1 = new File(targetPath);
        File fileArray1[] = file1.listFiles();
        
        for (File f: fileArray1){
            // フォルダ
            if(f.isDirectory()) {
                MyUtils.SystemLogPrint(f.toString());//フォルダを表示
                scanDeleteFile(f.toString(), backupPath, true);
            }
            
            // ファイル
            if(f.isFile()) {
                MyUtils.SystemLogPrint(f.toString());//ファイルを表示
                String fileName = f.toString();
                String extension = fileName.substring(fileName.length()-3);	//拡張子：後ろから3文字
                if (extension.equals("pdf") == true) {
                    totalSize = totalSize + f.length();
                    totalCount++;
                    if (deleteFlag == true) {
						//https://qiita.com/fumikomatsu/items/b98cc4d0dee782323096
                    	// 現在日時を取得
                        Calendar st = Calendar.getInstance();//Calendarクラスで現在日時を取得
                        st.add(Calendar.DATE, -60);          //現在値を取得(60日前)
                        Date start = st.getTime();           //Dateに直す
                        // ファイルの更新日時
                        Long lastModified = f.lastModified();
                        Date koushin = new Date(lastModified);
                        
                        //int diff = start.compareTo(koushin);
                        if (start.compareTo(koushin) > 0) {//compareToで比較
                        	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            String update_time = simpleDateFormat.format(koushin);
                            MyUtils.SystemLogPrint(update_time+"： " + fileName);
                        }   
                    }
                }
            }
        }
	}
}

class DeleteInfo {
	@JsonProperty("UpdatedDate") String UpdatedDate;
    @JsonProperty("TotalMBSize1") int TotalMBSize1;
    @JsonProperty("TotalCount1") long TotalCount1;
    @JsonProperty("TotalMBSize2") int TotalMBSize2;
    @JsonProperty("TotalCount2") long TotalCount2;

	DeleteInfo () {
		UpdatedDate = MyUtils.sdf.format(new Date());
		TotalMBSize1 = 0;
		TotalCount1 = 0;
		TotalMBSize2 = 0;
		TotalCount2 = 0;
	}
}
