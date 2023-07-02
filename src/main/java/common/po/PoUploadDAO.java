package common.po;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.example.demo.InsourcingConfig;

public class PoUploadDAO {
	//DB接続情報
	String DB_URL;
	String DB_USER;
	String DB_PASS;
	String DB_DRIVER;

	public PoUploadDAO(InsourcingConfig config) {
        //接続情報取得
		DB_URL = config.getDBUrl();
		DB_USER = config.getDBUsername();
		DB_PASS = config.getDBPassword();
		DB_DRIVER = config.getDBDriverClassName();
	}

	// インスタンスオブジェクトの生成->返却（コードの簡略化）
	public static PoUploadDAO getInstance(InsourcingConfig config) {
		return new PoUploadDAO(config);
	}
	
	public PoUploadBean read(String userId) throws SQLException {
		String sql = "select * from POUPLOADTABLE where LOGIN_ID = ?";
		
		//接続処理
		Connection conn = null;
		ArrayList<PoUploadBean> list = new ArrayList<PoUploadBean>();
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
			System.out.println(sql);

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            
            PoUploadBean upload = new PoUploadBean();
			while(rs.next()) {
				upload.setUserId(rs.getString("LOGIN_ID"));
				upload.setUserName(rs.getString("USER_NAME"));
				upload.setDatetime(rs.getString("CREATED_DATE"));
				upload.setToriCd(rs.getString("TORIHIKISAKI_CD"));
				upload.setUploadPath(rs.getString("UPLOAD_PATH"));
				upload.setCode(rs.getString("CODE"));
            	// リストにBeanクラスごと格納
				//list.add(upload);
				//Beanクラスを初期化
				//upload = new PoUploadBean();
			}
			return upload;

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

		return null;
	}

	public PoUploadBean quertyWithUploadPath(String uploadPath) throws SQLException {
		String sql = "select * from POUPLOADTABLE where UPLOAD_PATH = ?";
		
		//接続処理
		Connection conn = null;
		ArrayList<PoUploadBean> list = new ArrayList<PoUploadBean>();
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
			System.out.println("  uploadPath: " + uploadPath);
			System.out.println("  sql: " + sql);

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, uploadPath);
            ResultSet rs = ps.executeQuery();
            
            PoUploadBean upload = new PoUploadBean();
			while (rs.next()) {
				// ユーザIDと名前をBeanクラスへセット
				upload.setUserId(rs.getString("LOGIN_ID"));
				upload.setUserName(rs.getString("USER_NAME"));
				upload.setDatetime(rs.getString("CREATED_DATE"));
				upload.setToriCd(rs.getString("TORIHIKISAKI_CD"));
				upload.setUploadPath(rs.getString("UPLOAD_PATH"));
				upload.setCode(rs.getString("CODE"));
            	// リストにBeanクラスごと格納
				//list.add(upload);
				//Beanクラスを初期化
				//upload = new PoUploadBean();
			}
			return upload;
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
		
		return null;
	}

	public void registData_new(PoUploadBean poUpload) {
		
		try {
			int count = query_count(poUpload.getUploadPath());
			if (count == 0) {
				insert(poUpload);
			} else {
				update(poUpload);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void registData(PoUploadBean poUpload) {
		
		try {
			int count = query_count(poUpload.getUploadPath());
			if (count == 0) {
				insert(poUpload);
			} else {
				update(poUpload);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private int query_count(String uploadPath) throws SQLException {
		int count = -1;
		String sql = "select count(*) from POUPLOADTABLE where UPLOAD_PATH=?";
		
		//接続処理
		Connection conn = null;
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
			System.out.println("  UploadPath: " + uploadPath);
			System.out.println("  sql: " + sql);

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, uploadPath);
            ResultSet rs = ps.executeQuery();
            
            //1レコードなので、listなし
            rs.next();
            count = rs.getInt("COUNT(*)");
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
		
		return count;
	}

	private void insert(PoUploadBean poUpload) throws SQLException {
		String sql = "INSERT INTO POUPLOADTABLE(LOGIN_ID,USER_NAME,CREATED_DATE,TORIHIKISAKI_CD,UPLOAD_PATH,CODE,INPUT_PATH) " + 
					 "VALUES(?,?,TO_DATE(?,'YYYY/MM/DD HH24:MI:SS'),?,?,?,?)";
		
		//接続処理
		Connection conn = null;
        try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
			System.out.println(sql);
            conn.setAutoCommit(false);
            
            PreparedStatement ps = conn.prepareStatement(sql);
            int i=1;
            ps.setString(i++, poUpload.getUserId());
            ps.setString(i++, poUpload.getUserName());
            ps.setString(i++, poUpload.getDatetime());
            ps.setString(i++, poUpload.getToricd());
            ps.setString(i++, poUpload.getUploadPath());
            ps.setString(i++, poUpload.getCode());
            ps.setString(i++, poUpload.getInputPath());
            
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

	private void update(PoUploadBean poUpload) throws SQLException {
		String sql = "UPDATE POUPLOADTABLE SET LOGIN_ID=?,USER_NAME=?,CREATED_DATE=TO_DATE(?,'YYYY/MM/DD HH:MM:SS'),TORIHIKISAKI_CD=?,CODE=?,INPUT_PATH=? " + 
				 	 "WHERE UPLOAD_PATH=?";
		
		//接続処理
		Connection conn = null;
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
			System.out.println(sql);
			conn.setAutoCommit(false);
			
			PreparedStatement ps = conn.prepareStatement(sql);
			int i=1;
            ps.setString(i++, poUpload.getUserId());
            ps.setString(i++, poUpload.getUserName());
            ps.setString(i++, poUpload.getDatetime());
            ps.setString(i++, poUpload.getToricd());
            ps.setString(i++, poUpload.getCode());
            ps.setString(i++, poUpload.getInputPath());
            ps.setString(i++, poUpload.getUploadPath());
			
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
}
