package com.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelReader {

    public List<Map<String, String>> readExcelFile(InputStream inputStream, String sheetName, String tableName) {
        List<Map<String, String>> dataList = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                throw new IllegalArgumentException("הגיליון בשם " + sheetName + " לא נמצא.");
            }

            // בדיקה של השורות והעמודות ב-Excel
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Map<String, String> rowMap = new HashMap<>();
                sheet.getRow(i).forEach(cell -> {
                    String header = sheet.getRow(0).getCell(cell.getColumnIndex()).getStringCellValue();
                    String value = getCellValueAsString(cell);
                    rowMap.put(header, value);
                });
                dataList.add(rowMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataList;
    }

    // פונקציה שמחזירה את הערך של תא בתור סטרינג בהתאם לסוג התא
    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
