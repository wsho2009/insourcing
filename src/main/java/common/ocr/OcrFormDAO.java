package common.ocr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.demo.InsourcingConfig;

public class OcrFormDAO {
	String DB_URL;
	String DB_USER;
	String DB_PASS;
	String DB_DRIVER;
	
	public OcrFormDAO(InsourcingConfig config) {
        //接続情報取得
		DB_URL = config.getDBUrl();
		DB_USER = config.getDBUsername();
		DB_PASS = config.getDBPassword();
		DB_DRIVER = config.getDBDriverClassName();
	}

	// インスタンスオブジェクトの生成->返却（コードの簡略化）
	public static OcrFormDAO getInstance(InsourcingConfig config) {
		return new OcrFormDAO(config);
	}

	public OcrFormBean queryOcrFormInfoDocumentId(String documentId) throws SQLException {
		
		if (documentId.equals("") || documentId == null) {
			return null;
		}
		String sql = "select NAME,DOCUMENT_ID,DOCUMENT_NAME,DOCSET_NAME from OCRFORMTABLE where DOCUMENT_ID=?";
		
		//接続処理
		Connection conn = null;
        OcrFormBean form = new OcrFormBean();
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
			System.out.println(sql);

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, documentId);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
				//form.setNo(rs.getString("NO"));
				form.setFormName(rs.getString("NAME"));
				form.setDocumentId(rs.getString("DOCUMENT_ID"));
				form.setDocumentName(rs.getString("DOCUMENT_NAME"));
				form.setDocsetName(rs.getString("DOCSET_NAME"));
			}			
		} catch(SQLException sql_e) {
			// エラーハンドリング
			System.out.println("sql実行失敗");
			sql_e.printStackTrace();
			
		} catch(ClassNotFoundException e) {
			// エラーハンドリング
			System.out.println("JDBCドライバ関連エラー");
			e.printStackTrace();
			
		} finally {
			// DB接続を解除
			if (conn != null) {
				conn.close();
			}
		}
		// リストを返す
		return form;
	}
	
	public OcrFormBean queryOcrFormImportPath(int sortFlag, String importPath) throws SQLException {
		String sql = "select NAME,DOCUMENT_ID,DOCUMENT_NAME,DOCSET_NAME from ocrFormTable where IMPORT_PATH=?";
		if (sortFlag != 0) {
			sql = sql + " and DOCUMENT_NAME IS NULL";
		}

		//接続処理
		Connection conn = null;
        OcrFormBean form = new OcrFormBean();
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(this.DB_URL, this.DB_USER, this.DB_PASS);
			System.out.println("  sortFlag: " + sortFlag + "  importPath: " + importPath);
			System.out.println("  sql: " + sql);

			PreparedStatement ps = conn.prepareStatement(sql);
			int i=1;
			ps.setString(i++, importPath);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
				form.setFormName(rs.getString("NAME"));
				form.setDocumentId(rs.getString("DOCUMENT_ID"));
				form.setDocumentName(rs.getString("DOCUMENT_NAME"));
				form.setDocsetName(rs.getString("DOCSET_NAME"));
				//this.setOcrFlag(rs.getString("OCR_FLAG"));
			}
		} catch(SQLException sql_e) {
			// エラーハンドリング
			System.out.println("sql実行失敗");
			sql_e.printStackTrace();

		} catch(ClassNotFoundException e) {
			// エラーハンドリング
			System.out.println("JDBCドライバ関連エラー");
			e.printStackTrace();

		} finally {
			// DB接続を解除
			if (conn != null) {
				conn.close();
			}
		}

		return form;
	}
}
