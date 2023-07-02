package common.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MyExcel {
	
	Workbook book;
	Sheet sheet;
	Cell cell;
	Row row;
	
	//public MyExcel() {
	//}
	
	public Boolean sheetExist() {
		if (this.sheet == null)
			return false;
		else
			return true;
	}

	public void openXlsm(String xlsPath, String sheetName) throws IOException {
		book = WorkbookFactory.create(new File(xlsPath));
	}

	public void open(String xlsPath, String sheetName) throws IOException {
		//拡張子は、xlsxのみ
		//Workbook book = WorkbookFactory.create(new File(xlsPath));	//使えない？
		//既存ファイルのオープン
		try {
			book = new XSSFWorkbook(new File(xlsPath));
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		}
		if (sheetName == null)
			sheet = book.getSheetAt(0);
		else
			sheet = book.getSheet(sheetName);
		//新規作成のケース
		//Workbook book = new XSSFWorkbook();		//xlsx形式ブックの生成
		//Sheet sheet = book.createSheet();			//シートの生成
		//Row row;
		//Cell cell;
	}

	public void setSheet(String string) {
		this.sheet = book.getSheet(string);
		if (this.sheet != null)
			System.out.println("setSheet: " + this.sheet.getSheetName());
		else
			System.err.println("setSheet: error!");
	}
	
	public Boolean getRow(int rowIdx) {
		row = sheet.getRow(rowIdx);
		if (row == null)
			return false;
		else
			return true;
	}

	public void createRow(int rowIdx) {
		row = sheet.createRow(rowIdx);
	}

	public boolean getCell(int colIdx) {
		cell = row.getCell(colIdx);
		if (cell == null)
			return false;
		else
			return true;
	}

	public void createCell(int colIdx) {
		cell = row.createCell(colIdx);
	}

	public CellType getCellType(int colIdx) {
		return cell.getCellType();
	}
	
	private Object getCellValue(Cell cell) {
		CellType ctype;
		ctype = cell.getCellType();
		if (ctype == CellType.STRING) {
            return cell.getRichStringCellValue().getString();
		} else if (ctype == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            } else {
                return cell.getNumericCellValue();
            }
		} else if (ctype == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
		} else if (ctype == CellType.FORMULA) {
            return cell.getCellFormula();
		} else {
			return null;
		}
    } 

	public String getStringCellValue() {
		return cell.getStringCellValue();
	}
	
	public String getStringCellValue(int rowIdx, int colIdx) {
		row = sheet.getRow(rowIdx);
		cell = row.getCell(colIdx);
		return cell.getStringCellValue();
	}

	public double getNumericCellValue() {
		return cell.getNumericCellValue();
	}
	
	public void setCellValue(int colIdx, String strValue) {
		cell = row.createCell(colIdx);
		cell.setCellValue(strValue);
	}

	public void setCellValue(int colIdx, double dblValue) {
		cell = row.createCell(colIdx);
		cell.setCellValue(dblValue);
	}

	//ブックの別名保存
	public void save(String saveXlsPath) throws IOException {
		FileOutputStream out;
		try {
			out = new FileOutputStream(saveXlsPath);
			book.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	//ブックのクローズ
	public void close() throws IOException {
		book.close();
	}
	
	//結果をrowにセット。マッチしなければ最下行をセット。
	public String search(int colIdx, String target) {
		System.out.println("search sheet: " + this.sheet.getSheetName());
		//setSheet()されていることが前提
		String strValue = null;
		CellType ctype;
		boolean match = false;
		//https://plus-idea.net/how_to_read_excel_java_apache_poi/#i-2
		for (Row row2 : this.sheet) {
			cell = row2.getCell(colIdx);	//マッチング対象列を設定
			//ctype = cell.getCellType();
			if (cell.getStringCellValue() != null) {
				strValue = cell.getStringCellValue();
				strValue = strValue.trim();	//前後に空白が入っていたら除去
			} else {
				strValue = "";
			}
			if (strValue.equals(target) == true) {
				match = true;
				break;
			}
			row = row2;
		} //for
		//matchなし(最下行)
		if (match == false) {
			cell = row.getCell(colIdx);		//マッチング対象列を設定
			if (cell.getStringCellValue() != null) {
				strValue = cell.getStringCellValue();
				strValue = strValue.trim();	//前後に空白が入っていたら除去
			} else {
				strValue = "";
			}
			strValue = "00送信元なし";
		}
		
		return strValue;
	}
}
