package dbchange;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatusDataBean  {
	//DB接続情報
	String DB_URL;
	String DB_USER;
	String DB_PASS;
	
	@JsonProperty("ID")	int ID;
	@JsonProperty("Name") String Name;
	@JsonProperty("BirthYear")	 int BirthYear;
	@JsonProperty("BirthMonth")	 int BirthMonth;
	@JsonProperty("BirthDay") int BirthDay;
	@JsonProperty("SEX")	String Sex;
	
	public StatusDataBean() {
		//接続情報取得
		ResourceBundle rb = ResourceBundle.getBundle("prop");
		DB_URL = rb.getString("DB_URL");
		DB_USER = rb.getString("DB_USER");
		DB_PASS = rb.getString("DB_PASS");
    }

	public String getTaskName() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public String getTaskStatus() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public String getUserId() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
	public int queryDB() throws SQLException {
		String sql = "select * from USER_DATA where rownum=1 order by ID desc";
		//接続処理
		Connection conn = null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(this.DB_URL, this.DB_USER, this.DB_PASS);
			//System.out.println(sql);

			PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            //1レコードなので、listなし
            rs.next();
            this.ID = rs.getInt("ID");
			this.Name = rs.getString("NAME");
			this.BirthYear = rs.getInt("BIRTH_YEAR");
			this.BirthMonth = rs.getInt("BIRTH_MONTH");
			this.BirthDay = rs.getInt("BIRTH_DAY");
			this.Sex = rs.getString("SEX");
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
		
		return 0;
	}
	
    public String getDataText() {
		String line = this.ID + "\t" + this.Name + "\t" + this.BirthYear + "\t" +
					  this.BirthMonth + "\t" + this.BirthDay + "\t" + this.Sex;

		return line;
	}

}

