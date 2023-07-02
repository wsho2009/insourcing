package common.ocr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.example.demo.InsourcingConfig;

public class OcrDataFormDAO {
	//DB接続情報
	String DB_URL;
	String DB_USER;
	String DB_PASS;
	String DB_DRIVER;

	public OcrDataFormDAO(InsourcingConfig config) {
        //接続情報取得
		DB_URL = config.getDBUrl();
		DB_USER = config.getDBUsername();
		DB_PASS = config.getDBPassword();
		DB_DRIVER = config.getDBDriverClassName();
	}

	// インスタンスオブジェクトの生成->返却（コードの簡略化）
	public static OcrDataFormDAO getInstance(InsourcingConfig config) {
		return new OcrDataFormDAO(config);
	}

	public ArrayList<OcrDataFormBean> read(String formName, String date_fr, String date_to) throws SQLException {
		String sql = "select UNIT_ID,UNIT_NAME,UPLOAD_PATH,STATUS,CSV_FILENAME,CREATEDAT,LINK_URL,TYPE from " +
				 	 "(select UNIT_ID,UNIT_NAME,UPLOAD_PATH,STATUS,CSV_FILENAME,CREATEDAT,LINK_URL,TYPE " + 
				 	 " from OCRDATATABLE where STATUS IN ('ENTRY','COMPLETE') and UNIT_NAME<>'仕分け不可' ";

        if (date_fr != null && date_fr.equals("") != true) {
        	date_fr = date_fr.replace("-", "/");	//(yyyy-MM-dd) → (yyyy/MM/dd)
        	sql = sql + " and CREATEDAT >= '" + date_fr + " 00:00:00'";
        }
        if (formName != null && formName.equals("") != true) {
        	sql = sql + " and UNIT_NAME like '%" + formName + "%'";
		}
		sql = sql +  "order by CREATEDAT DESC) where rownum <= 30";
		
		//接続処理
		Connection conn = null;
		ArrayList<OcrDataFormBean> list = new ArrayList<OcrDataFormBean>();
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
			System.out.println(sql);	//for debug

			PreparedStatement ps = conn.prepareStatement(sql);
			int i=1;
			//ps.setString(i++, type);
            ResultSet rs = ps.executeQuery();
            
            OcrDataFormBean dataform = new OcrDataFormBean();
    		while(rs.next()) {
    			// ユーザIDと名前をBeanクラスへセット
    			dataform.unitId = rs.getString("UNIT_ID");
    			dataform.unitName = rs.getString("UNIT_NAME");
    			dataform.uploadFilePath = rs.getString("UPLOAD_PATH");
    			dataform.status = rs.getString("STATUS");
    			dataform.csvFileName = rs.getString("CSV_FILENAME");
    			dataform.createdAt = rs.getString("CREATEDAT");
    			if (dataform.createdAt != null)
    				dataform.createdAt = dataform.createdAt.replace("-", "/");	//(yyyy-MM-dd HH:mm:ss) → (yyyy/MM/dd HH:mm:ss)
    			dataform.linkUrl = rs.getString("LINK_URL");
    			dataform.type = rs.getInt("TYPE");
            	// リストにBeanクラスごと格納
    			list.add(dataform);
    			//Beanクラスを初期化
    			dataform = new OcrDataFormBean();
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
		return list;
	}
	
	public ArrayList<OcrDataFormBean> queryNotComplete(String type) throws SQLException {
		
		String sql = "select o.UNIT_ID,o.UNIT_NAME,o.UPLOAD_PATH,o.STATUS,CSV_FILENAME,CREATEDAT,LINK_URL,TYPE, " + 
					 "t.*, o.DOCUMENT_ID DOCUMENT_ID2, o.DOCUMENT_NAME DOCUMENT_NAME2, o.DOCSET_NAME DOCSET_NAME2 from OCRDATATABLE o, OCRFORMTABLE t " +
					 "where o.STATUS <> 'COMPLETE' and o.UNIT_NAME=t.NAME(+) and o.Type=? order by o.CREATEDAT";
		
		//接続処理
		Connection conn = null;
		ArrayList<OcrDataFormBean> list = new ArrayList<OcrDataFormBean>();
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
			//System.out.println(sql);	//for debug

			PreparedStatement ps = conn.prepareStatement(sql);
			int i=1;
			ps.setString(i++, type);
            ResultSet rs = ps.executeQuery();
            
            OcrDataFormBean dataform = new OcrDataFormBean();
    		while(rs.next()) {
    			// ユーザIDと名前をBeanクラスへセット
    			dataform.unitId = rs.getString("UNIT_ID");
    			dataform.unitName = rs.getString("UNIT_NAME");
    			dataform.uploadFilePath = rs.getString("UPLOAD_PATH");
    			dataform.status = rs.getString("STATUS");
    			dataform.csvFileName = rs.getString("CSV_FILENAME");
    			dataform.createdAt = rs.getString("CREATEDAT");
    			if (dataform.createdAt != null)
    				dataform.createdAt = dataform.createdAt.replace("-", "/");	//(yyyy-MM-dd HH:mm:ss) → (yyyy/MM/dd HH:mm:ss)
    			dataform.linkUrl = rs.getString("LINK_URL");
				dataform.type = rs.getInt("TYPE");
				if (rs.getString("NAME") != null) {
					dataform.setNo(rs.getString("NO"));
					dataform.setName(rs.getString("NAME"));
					dataform.setDocumentId(rs.getString("DOCUMENT_ID"));
					dataform.setDocumentName(rs.getString("DOCUMENT_NAME"));
					//dataform.setDocsetId(rs.getString("DOCSET_ID"));
					dataform.setDocsetName(rs.getString("DOCSET_NAME"));
					dataform.setRotateInfo(rs.getString("ROTATE_INFO"));
					dataform.setHeaderNum(rs.getInt("COL_HEADER"));
					dataform.setMeisaiNum(rs.getInt("COL_MEISAI"));
					dataform.setColChuban(rs.getInt("COL_CHUBAN"));
					dataform.setColHinmei(rs.getInt("COL_HINMEI"));
					dataform.setColSuryo(rs.getInt("COL_SURYO"));
					dataform.setColTanka(rs.getInt("COL_TANKA"));
					dataform.setColKingaku(rs.getInt("COL_KINGAKU"));
					dataform.setColOutput(rs.getString("COL_OUTPUT"));
					dataform.setColToriCd(rs.getString("TORIHIKISAKI_CD"));
				} else {
					dataform.setDocumentId(rs.getString("DOCUMENT_ID2"));
					dataform.setDocumentName(rs.getString("DOCUMENT_NAME2"));
					dataform.setDocsetName(rs.getString("DOCSET_NAME2"));
				}
            	// リストにBeanクラスごと格納
    			list.add(dataform);
    			//Beanクラスを初期化
    			dataform = new OcrDataFormBean();
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
		return list;
	}

	public ArrayList<OcrDataFormBean> queryWithUnitId(String unitId) throws SQLException {
		
		String sql = "select o.UNIT_ID,o.UNIT_NAME,o.UPLOAD_PATH,o.STATUS,CSV_FILENAME,CREATEDAT,LINK_URL,TYPE, " + 
					 "t.*, o.DOCUMENT_ID DOCUMENT_ID2, o.DOCUMENT_NAME DOCUMENT_NAME2, o.DOCSET_NAME DOCSET_NAME2 from OCRDATATABLE o, OCRFORMTABLE t " +
					 "where o.UNIT_ID=? and o.UNIT_NAME=t.NAME(+) ";
		
		//接続処理
		Connection conn = null;
		ArrayList<OcrDataFormBean> list = new ArrayList<OcrDataFormBean>();
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
			//System.out.println(sql);	//for debug

			PreparedStatement ps = conn.prepareStatement(sql);
			int i=1;
			ps.setString(i++, unitId);
            ResultSet rs = ps.executeQuery();
            
            OcrDataFormBean dataform = new OcrDataFormBean();
    		while(rs.next()) {
    			// ユーザIDと名前をBeanクラスへセット
    			dataform.unitId = rs.getString("UNIT_ID");
    			dataform.unitName = rs.getString("UNIT_NAME");
    			dataform.uploadFilePath = rs.getString("UPLOAD_PATH");
    			dataform.status = rs.getString("STATUS");
    			dataform.csvFileName = rs.getString("CSV_FILENAME");
    			dataform.createdAt = rs.getString("CREATEDAT");
    			if (dataform.createdAt != null)
    				dataform.createdAt = dataform.createdAt.replace("-", "/");	//(yyyy-MM-dd HH:mm:ss) → (yyyy/MM/dd HH:mm:ss)
    			dataform.linkUrl = rs.getString("LINK_URL");
				dataform.type = rs.getInt("TYPE");
				if (rs.getString("NAME") != null) {
					dataform.setNo(rs.getString("NO"));
					dataform.setName(rs.getString("NAME"));
					dataform.setDocumentId(rs.getString("DOCUMENT_ID"));
					dataform.setDocumentName(rs.getString("DOCUMENT_NAME"));
					//dataform.setDocsetId(rs.getString("DOCSET_ID"));
					dataform.setDocsetName(rs.getString("DOCSET_NAME"));
					dataform.setRotateInfo(rs.getString("ROTATE_INFO"));
					dataform.setHeaderNum(rs.getInt("COL_HEADER"));
					dataform.setMeisaiNum(rs.getInt("COL_MEISAI"));
					dataform.setColChuban(rs.getInt("COL_CHUBAN"));
					dataform.setColHinmei(rs.getInt("COL_HINMEI"));
					dataform.setColSuryo(rs.getInt("COL_SURYO"));
					dataform.setColTanka(rs.getInt("COL_TANKA"));
					dataform.setColKingaku(rs.getInt("COL_KINGAKU"));
					dataform.setColOutput(rs.getString("COL_OUTPUT"));
					dataform.setColToriCd(rs.getString("TORI_CD"));
				} else {
					dataform.setDocumentId(rs.getString("DOCUMENT_ID2"));
					dataform.setDocumentName(rs.getString("DOCUMENT_NAME2"));
					dataform.setDocsetName(rs.getString("DOCSET_NAME2"));
				}
            	// リストにBeanクラスごと格納
    			list.add(dataform);
    			//Beanクラスを初期化
    			dataform = new OcrDataFormBean();
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
		return list;
	}
	
	public void insertReadingUnitDB(OcrDataFormBean ocrData) throws SQLException {
		String sql = "INSERT INTO OCRDATATABLE(UNIT_ID,UNIT_NAME,UPLOAD_PATH,STATUS,CSV_FILENAME,DOCSET_ID,DOCSET_NAME,DOCUMENT_ID,DOCUMENT_NAME,CREATEDAT,TYPE) " + 
					"VALUES(?,?,?,?,?,?,?,?,?,TO_CHAR(sysdate, 'YYYY/MM/DD HH24:MI:SS'),?)";
		
		//接続処理
		Connection conn = null;
        try {
			Class.forName(this.DB_DRIVER);
			conn = DriverManager.getConnection(this.DB_URL, this.DB_USER, this.DB_PASS);
			System.out.println(sql);
            conn.setAutoCommit(false);
            
            PreparedStatement ps = conn.prepareStatement(sql);
            int i=1;
            ps.setString(i++, ocrData.unitId);
            ps.setString(i++, ocrData.unitName);
            ps.setString(i++, ocrData.uploadFilePath);
            ps.setString(i++, ocrData.status);
            ps.setString(i++, ocrData.csvFileName);
            ps.setString(i++, ocrData.docsetId);
            ps.setString(i++, ocrData.docsetName);
            ps.setString(i++, ocrData.documentId);
            ps.setString(i++, ocrData.documentName);
            //ps.setString(i++, this.createdAt);	//sysdate
            ps.setInt(i++, ocrData.type);
            
            ps.executeUpdate();
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
			// DB接続を解除
			if (conn != null) {
				conn.close();
			}
        }
	}

	public void updateWithUploadFile(OcrDataFormBean ocrData) throws SQLException {
		String sql = "UPDATE OCRDATATABLE SET UNIT_ID=?,UNIT_NAME=?,STATUS=?,CSV_FILENAME=?,DOCUMENT_ID=?,DOCUMENT_NAME=?,"
				   + "DOCSET_ID=?,CREATEDAT=?,LINK_URL=? "
				   + "WHERE UPLOAD_PATH=?";
	
		//接続処理
		Connection conn = null;
	   	try {
			Class.forName(this.DB_DRIVER);
			conn = DriverManager.getConnection(this.DB_URL, this.DB_USER, this.DB_PASS);
			System.out.println("  unitId: " + ocrData.unitId + "  unitName: " + ocrData.unitName + "  status: " + ocrData.status + "  csvFileName: " + ocrData.csvFileName + "  documentId: " + ocrData.documentId
								+ "  documentName: " + ocrData.documentName + "  docsetId: " + ocrData.docsetId + "  createdAt: " + ocrData.createdAt + "  linkUrl: " + ocrData.linkUrl + "  uploadFilePath: " + ocrData.uploadFilePath);
			System.out.println("  sql: " + sql);
			conn.setAutoCommit(false);
			
			PreparedStatement ps = conn.prepareStatement(sql);
			int i=1;
			ps.setString(i++, ocrData.unitId);
			ps.setString(i++, ocrData.unitName);
			ps.setString(i++, ocrData.status);
			ps.setString(i++, ocrData.csvFileName);
			ps.setString(i++, ocrData.documentId);
			ps.setString(i++, ocrData.documentName);
			ps.setString(i++, ocrData.docsetId);
			ps.setString(i++, ocrData.createdAt);
			ps.setString(i++, ocrData.linkUrl);
			ps.setString(i++, ocrData.uploadFilePath);
			
			ps.executeUpdate();
			conn.commit();
	   	} catch (Exception e) {
	    	e.printStackTrace();
	   	} finally {
			// DB接続を解除
			if (conn != null) {
				conn.close();
			}
	   	}
	}

	public void updateUploadFilePath(String before, String after, String updateFilePath) throws SQLException {
		String sql = "UPDATE OCRDATATABLE SET UPLOAD_PATH=REPLACE(UPLOAD_PATH, ?, ?) "
				   + "WHERE UPLOAD_PATH=?";
	
		//接続処理
		Connection conn = null;
	   	try {
			Class.forName(this.DB_DRIVER);
			conn = DriverManager.getConnection(this.DB_URL, this.DB_USER, this.DB_PASS);
			System.out.println("  uploadFile: " + updateFilePath);
			System.out.println("  before: " + before + "--> after: " + after);
			System.out.println("  sql: " + sql);
			conn.setAutoCommit(false);
	       
			PreparedStatement ps = conn.prepareStatement(sql);
			int i=1;
			ps.setString(i++, before);
			ps.setString(i++, after);
			ps.setString(i++, updateFilePath);
			
			ps.executeUpdate();
	       	conn.commit();
	   	} catch (Exception e) {
	       	e.printStackTrace();
	   	} finally {
			// DB接続を解除
			if (conn != null) {
				conn.close();
			}
	   	}
	}

	public ArrayList<OcrDataFormBean> updateReShiwake(String uploadPath) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
