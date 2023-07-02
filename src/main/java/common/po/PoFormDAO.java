package common.po;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.example.demo.InsourcingConfig;

public class PoFormDAO {
	//DB接続情報
	String DB_URL;
	String DB_USER;
	String DB_PASS;
	String DB_DRIVER;
		
	public PoFormDAO(InsourcingConfig config) {
        //接続情報取得
		DB_URL = config.getDBUrl();
		DB_USER = config.getDBUsername();
		DB_PASS = config.getDBPassword();
		DB_DRIVER = config.getDBDriverClassName();
	}

	// インスタンスオブジェクトの生成->返却（コードの簡略化）
	public static PoFormDAO getInstance(InsourcingConfig config) {
		return new PoFormDAO(config);
	}
	
	// 検索処理
	// 戻り値		：ArrayList<Beanクラス>
	public ArrayList<PoFormBean> read(String userId) throws SQLException {
		//String sql = "select * from POFORMTABLE where MEMBER like '%?%'";
        String sql = "select * from POFORMTABLE where MEMBER like '%" + userId + "%'";
		
		//接続処理
		Connection conn = null;
		ArrayList<PoFormBean> list = new ArrayList<PoFormBean>();
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
			System.out.println(sql);

			PreparedStatement ps = conn.prepareStatement(sql);
			//ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            PoFormBean poForm = new PoFormBean();
			while(rs.next()) {
				// ユーザIDと名前をBeanクラスへセット
				poForm.setNo(rs.getInt("NO"));
				poForm.setCode(rs.getString("CODE"));
				poForm.setFormId(rs.getString("FORM_ID"));
				poForm.setFormName(rs.getString("FORM_NAME"));
				poForm.setMember(rs.getString("MEMBER"));
				list.add(poForm);
				//Beanクラスを初期化
				poForm = new PoFormBean();
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
}
