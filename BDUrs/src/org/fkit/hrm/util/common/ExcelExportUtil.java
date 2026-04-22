package org.fkit.hrm.util.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.fkit.hrm.dto.UserExportDTO;

public class ExcelExportUtil {

	private static final String[] HEADERS = {"姓名", "部门", "岗位", "角色"};
	private static final int COLUMN_WIDTH = 20;
	
	public static byte[] exportUsersToExcel(List<UserExportDTO> users) throws IOException {
		Workbook workbook = createWorkbook(users, "用户列表");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		workbook.write(bos);
		workbook.close();
		return bos.toByteArray();
	}
	
	public static byte[] exportUsersToZip(Map<String, List<UserExportDTO>> deptUserMap) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(bos);
		
		for (Map.Entry<String, List<UserExportDTO>> entry : deptUserMap.entrySet()) {
			String deptName = entry.getKey();
			List<UserExportDTO> users = entry.getValue();
			
			Workbook workbook = createWorkbook(users, deptName + "用户列表");
			ByteArrayOutputStream excelBos = new ByteArrayOutputStream();
			workbook.write(excelBos);
			workbook.close();
			
			ZipEntry zipEntry = new ZipEntry(deptName + "用户列表.xlsx");
			zos.putNextEntry(zipEntry);
			zos.write(excelBos.toByteArray());
			zos.closeEntry();
		}
		
		zos.close();
		return bos.toByteArray();
	}
	
	private static Workbook createWorkbook(List<UserExportDTO> users, String sheetName) {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet(sheetName);
		
		for (int i = 0; i < HEADERS.length; i++) {
			sheet.setColumnWidth(i, COLUMN_WIDTH * 256);
		}
		
		CellStyle headerStyle = createHeaderStyle(workbook);
		CellStyle dataStyle = createDataStyle(workbook);
		
		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < HEADERS.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(HEADERS[i]);
			cell.setCellStyle(headerStyle);
		}
		
		int rowNum = 1;
		for (UserExportDTO user : users) {
			Row row = sheet.createRow(rowNum++);
			
			Cell cell0 = row.createCell(0);
			cell0.setCellValue(user.getUsername() != null ? user.getUsername() : "");
			cell0.setCellStyle(dataStyle);
			
			Cell cell1 = row.createCell(1);
			cell1.setCellValue(user.getDeptName() != null ? user.getDeptName() : "");
			cell1.setCellStyle(dataStyle);
			
			Cell cell2 = row.createCell(2);
			cell2.setCellValue(user.getJobName() != null ? user.getJobName() : "");
			cell2.setCellStyle(dataStyle);
			
			Cell cell3 = row.createCell(3);
			cell3.setCellValue(user.getRoleName() != null ? user.getRoleName() : "");
			cell3.setCellStyle(dataStyle);
		}
		
		return workbook;
	}
	
	private static CellStyle createHeaderStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		
		return style;
	}
	
	private static CellStyle createDataStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		return style;
	}
	
	public static String generateZipFileName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return "用户导出_" + sdf.format(new Date()) + ".zip";
	}
	
	public static String generateExcelFileName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return "用户列表_" + sdf.format(new Date()) + ".xlsx";
	}
}
