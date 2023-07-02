package common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyUtils {
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	//public MyUtils() {
	//}
	public static void SystemLogPrint(String msg) {
        System.out.printf("[%s]%s\n", sdf.format(new Date()), msg);
	}

	public static void SystemErrPrint(String msg) {
        System.err.printf("[%s]%s\n", sdf.format(new Date()), msg);
	}
	
	public static String getDateStr() {
		return sdf.format(new Date());
	}	
}
