package common.fax;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.example.demo.InsourcingConfig;

public class FaxDataDAO {
	String DB_URL;
	String DB_USER;
	String DB_PASS;
	String DB_DRIVER;
	
	public FaxDataDAO(InsourcingConfig config) {
        //接続情報取得
		DB_URL = config.getDBUrl();
		DB_USER = config.getDBUsername();
		DB_PASS = config.getDBPassword();
		DB_DRIVER = config.getDBDriverClassName();
	}

	// インスタンスオブジェクトの生成->返却（コードの簡略化）
	public static FaxDataDAO getInstance(InsourcingConfig config) {
		return new FaxDataDAO(config);
	}
	
	// 検索処理
	// 戻り値		：ArrayList<Beanクラス>
	public ArrayList<FaxDataBean> read(String formName, String date_fr, String date_to, String soushinMotoNashi, String kyoten) throws SQLException {
        String sql = "select NO,FAX_NO,CDATE,CTIME,JOB_NO,FILE_NAME,SOUSHN_MOTO,FULLPATH,UNIT_ID from "
     			   + "(select f.NO,f.FAX_NO,f.CDATE,f.CTIME,f.JOB_NO,f.FILE_NAME,f.SOUSHN_MOTO,f.FULLPATH,o.UNIT_ID from faxDataTable f left outer join ocrDataTable o on f.FULLPATH=o.UPLOAD_PATH where f.KYOTEN='" + kyoten + "'";
        if (date_fr != null && date_fr.equals("") != true) {
        	date_fr = date_fr.replace("-", "/");	//(yyyy-MM-dd) → (yyyy/MM/dd)
        	sql = sql + " and f.CDATE >= '" + date_fr + "'";
        }
        if (formName != null && formName.equals("") != true && soushinMotoNashi.equals("true") == false) {
        	sql = sql + " and f.SOUSHN_MOTO like '%" + formName + "%'";
        } else if (soushinMotoNashi != null && soushinMotoNashi.equals("true") == true) {
			sql = sql + " and f.FULLPATH like '%送信元なし%'";
		}
        sql = sql + " order by NO DESC) where rownum <= 30";
        
		//接続処理
		Connection conn = null;
		ArrayList<FaxDataBean> fax_dao = new ArrayList<FaxDataBean>();
		try {
			Class.forName(this.DB_DRIVER);
			conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
			System.out.println(sql);

			PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            FaxDataBean fax = new FaxDataBean();
			while(rs.next()) {
				// ユーザIDと名前をBeanクラスへセット
				fax.setNo(rs.getInt("NO"));
				fax.setFaxNo(rs.getString("FAX_NO"));
				fax.setCDate(rs.getString("CDATE"));
				fax.setCTime(rs.getString("CTIME"));
				fax.setJobNo(rs.getString("JOB_NO"));
				fax.setFileName(rs.getString("FILE_NAME"));
				fax.setSoshinMoto(rs.getString("SOUSHN_MOTO"));
				fax.setFullPath(rs.getString("FULLPATH"));
				fax.setKyoten(rs.getString("UNIT_ID"));

            	// リストにBeanクラスごと格納
				fax_dao.add(fax);
				//Beanクラスを初期化
				fax = new FaxDataBean();
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
		return fax_dao;
	}
	
	public void insertDB(FaxDataBean fax) throws SQLException {
		String sql = "INSERT INTO FAXDATATABLE VALUES(SEQ_FAX_NO.nextval,?,?,?,?,?,?,?,?)";
		
		//接続処理
		Connection conn = null;
        try {
			Class.forName(this.DB_DRIVER);
			conn = DriverManager.getConnection(this.DB_URL, this.DB_USER, this.DB_PASS);
			System.out.println("  faxNo: " + fax.faxNo + "  cDate: " + fax.cDate + "  cTime: " + fax.cTime + "  jobNo: " + fax.jobNo + "  fileName: " + fax.fileName
							 + "  soshinMoto: " + fax.soshinMoto + "  fullPath: " + fax.fullPath + "  soshinMoto: " + fax.soshinMoto + "  kyoten: " + fax.kyoten);
			System.out.println("  sql: " + sql);
            conn.setAutoCommit(false);
            
            PreparedStatement ps = conn.prepareStatement(sql);
            int i=1;
            //ps.setString(i++, this.no);
            ps.setString(i++, fax.faxNo);
            ps.setString(i++, fax.cDate);
            ps.setString(i++, fax.cTime);
            ps.setString(i++, fax.jobNo);
            ps.setString(i++, fax.fileName);
            ps.setString(i++, fax.soshinMoto);
            ps.setString(i++, fax.fullPath);
            ps.setString(i++, fax.kyoten);
            
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
	
	public void updateSoshinmotoWithUploadFile(String soshinMoto, String uploadFilePath) throws SQLException {
		String sql = "UPDATE FAXDATATABLE SET SOUSHN_MOTO=? WHERE FULLPATH=?";
	
		//接続処理
		Connection conn = null;
		try {
			Class.forName(this.DB_DRIVER);
			conn = DriverManager.getConnection(this.DB_URL, this.DB_USER, this.DB_PASS);
			System.out.println("  soshinMoto: " + soshinMoto + "  uploadFilePath: " + uploadFilePath);
			System.out.println("  sql: " + sql);
			conn.setAutoCommit(false);
	       
			PreparedStatement ps = conn.prepareStatement(sql);
			int i=1;
			ps.setString(i++, soshinMoto);
			ps.setString(i++, uploadFilePath);
			
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
		String sql = "UPDATE FAXDATATABLE SET FULLPATH=REPLACE(FULLPATH, ?, ?) "
				   + "WHERE FULLPATH=?";
		
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
}
