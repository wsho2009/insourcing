package common.utils;

import static java.nio.file.StandardCopyOption.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MyFiles {
	//ファイルの存在チェック
	public static Boolean exists(String srcPath) {
		Path src = Paths.get(srcPath);
		return Files.exists(src);
	}
	
	//存在を確認し、なければディレクトリ作成
    public static void notExistsCreateDirectory(String path) throws IOException {
		Path p = Paths.get(path);
    	if (!Files.exists(p)) {
			Files.createDirectory(p);
    	}
    }

	//ファイルコピー(上書き)
	public static void copyOW(String srcPath, String dstPath) throws IOException {
		Path src = Paths.get(srcPath);
		Path dst = Paths.get(dstPath);
		Files.copy(src, dst, REPLACE_EXISTING);	//上書き
	}
	
	//ファイル移動(上書き)
	public static void moveOW(String srcPath, String dstPath) throws IOException {
		Path src = Paths.get(srcPath);
		Path dst = Paths.get(dstPath);
		Files.move(src, dst, REPLACE_EXISTING);	//上書き
	}

	//ファイル削除
	public static void delete(String path) throws IOException {
		Path p = Paths.get(path);
		Files.delete(p);
	}

	//存在を確認し、あればファイル削除
	public static void existsDelete(String path) throws IOException {
		Path p = Paths.get(path);
		if (Files.exists(p)){
			Files.delete(p);
		}
	}

	//CSVパーサー
    //https://4engineer.net/java/parse-difficult-csv/
	public static ArrayList<ArrayList<String>> parseCSV(String filePath, String charSet) throws IOException {
		ArrayList<ArrayList<String>> list = null;
		
	    try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), Charset.forName(charSet))) {
	        StringBuilder sb = new StringBuilder();
	        list = new ArrayList<ArrayList<String>>();

	        String line;
	        while ((line = br.readLine()) != null) {
	            // ファイルから読み取った行を連結する
	            sb.append(line);
	            
	            if (line.substring(line.length()-1, line.length()).equals("\"")==false) {
	                // 行末がダブルクォート以外の場合、改行コードが含まれているのでcontinueして次の行へ
	                continue;
	            }

	            // 行の先頭と末尾のダブルクォートを除去した後、"," でsplitする。
	            /*String[] columns = StringUtils.removeStart(
	                            StringUtils.removeEnd(sb.toString(), "\""), "\"")
	                    .split("\",\"");*/
	            line = sb.toString();	//連結
	            if (line.substring(0,1).equals("\"")==true)
	            	line = line.substring(1);	//行の先頭のダブルクォートを除去
	            if (line.substring(line.length()-1, line.length()).equals("\"")==true)
	            	line = line.substring(0, line.length() - 1);	//行の末尾のダブルクォートを除去
	            String[] columns = line.split("\",\"");	//除去した後、"," でsplitする。
	            if (columns != null) {
		            ArrayList<String> data = new ArrayList<String>();
		            for (String str : columns) {
		            	data.add(str);
		            }
		            list.add(data);
	            }
	            // 結果出力
	            //System.out.println(StringUtils.join(columns, "|"));

	            // StringBuilderの初期化
	            sb.setLength(0);
	        }
	    }
        return list;
	}

	//TSVパーサー
	public static ArrayList<ArrayList<String>> parseTSV(String filePath, String charSet) throws IOException {
		//ダブルクォーテーション、途中改行 はなしとする。
		ArrayList<ArrayList<String>> list = null;
		
	    try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), Charset.forName(charSet))) {
	        StringBuilder sb = new StringBuilder();
	        list = new ArrayList<ArrayList<String>>();

	        String line;
	        while ((line = br.readLine()) != null) {
	            // ファイルから読み取った行を連結する
	            sb.append(line);
	            String[] columns = line.split("\t");	//"\t" でsplitする。
	            if (columns != null) {
		            ArrayList<String> data = new ArrayList<String>();
		            for (String str : columns) {
		            	data.add(str);
		            }
		            list.add(data);
	            }
	            // StringBuilderの初期化
	            sb.setLength(0);
	        }
	    }
        return list;
	}
	
	public static JsonNode parseJson(String jsonPath) throws IOException {
		Path path = Paths.get(jsonPath);
		String content = Files.readString(path);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jnode = mapper.readTree(content);
		
		return jnode;
	}

	//フルパスからファイル名取得
    public static String getFileName(String path) {
		Path p = Paths.get(path);
        return p.getFileName().toString();
    }

	//フルパスからフォルダ名取得
	public static String getParent(String path) {
		Path p = Paths.get(path);
		return p.getParent().toString() + "\\";
	}

	//カレントパス取得
    public static String getCurrentPath() {
		Path p1 = Paths.get("");
		Path p2 = p1.toAbsolutePath();

		return p2.toString();
    }
    
    //テキストファイルから文字列を読み込む
	public static String readAllText(String sqlPath) throws IOException {
		String text = "";
		Path path = Paths.get(sqlPath);
		ArrayList<String> lines = (ArrayList<String>)Files.readAllLines(path);
		for (String str : lines) {
			text = text + str;
		}
		return text;
	}
	
	//List<String>を新規作成したファイルに書き込む。
    public static int WriteList2File(ArrayList<String> list, String writePath) throws IOException {
		File file = new File(writePath);
		FileWriter filewriter = new FileWriter(file);
    	
		String line;
		//データ
		for (int i=0; i<list.size(); i++) {
			line = list.get(i);
  			filewriter.write(line);
		}
		filewriter.close();
		
    	return 0;
    }
}
