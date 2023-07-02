package common.po;

public class TestPoScanFile {

	public static void main(String[] args) {
		String uploadPath = "D:\\pleiades\\input\\";
		PoScanFile scan = new PoScanFile(uploadPath);
		
		new Thread(scan).start();
	}

}
