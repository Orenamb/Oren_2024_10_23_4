package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class FormController {

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private ExcelReader excelReader;

    @GetMapping("/form")
    public String showForm() {
        // לוג: טוען את הדף form.html
        System.out.println("טוען את הדף form.html");
        return "form";
    }

    @PostMapping("/submit-form")
    public String submitForm(
            @RequestParam("dbUrl") String dbUrl,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("tableName") String tableName,
            @RequestParam("excelFilePath") String excelFilePath,
            @RequestParam("sheetName") String sheetName,
            Model model) {

        try {
            // לוג: שמירת הנתונים של המשתמש במודל
            System.out.println("שומר את פרטי החיבור לבסיס הנתונים ומיקום קובץ האקסל.");
            model.addAttribute("dbUrl", dbUrl);
            model.addAttribute("username", username);
            model.addAttribute("password", password);
            model.addAttribute("tableName", tableName);
            model.addAttribute("excelFilePath", excelFilePath);
            model.addAttribute("sheetName", sheetName);

            // לוג: בודק אם הטבלה קיימת בבסיס הנתונים
            System.out.println("בודק אם הטבלה '" + tableName + "' קיימת בבסיס הנתונים.");
            if (databaseService.doesTableExist(tableName)) {
                System.out.println("הטבלה קיימת.");

                List<Map<String, String>> dataList;
                File excelFile = new File(excelFilePath);

                if (!excelFile.exists()) {
                    System.out.println("קובץ ה-Excel לא נמצא בנתיב: " + excelFilePath);
                    model.addAttribute("message", "קובץ ה-Excel לא נמצא בנתיב שסיפקת.");
                    return "error"; // דף שגיאה אם הקובץ לא נמצא
                }

                try (FileInputStream excelFileStream = new FileInputStream(excelFile)) {
                    dataList = excelReader.readExcelFile(excelFileStream, sheetName, tableName);
                }

                Set<String> excelColumns = dataList.get(0).keySet();

                // לוג: בודק התאמת שמות העמודות בין קובץ ה-Excel והטבלה
                System.out.println("בודק התאמת שמות העמודות בין קובץ ה-Excel לטבלה '" + tableName + "'.");

                Set<String> dbColumns = databaseService.getTableColumns(tableName);

                Set<String> missingColumns = excelColumns.stream()
                        .filter(column -> !dbColumns.contains(column))
                        .collect(Collectors.toSet());

                if (!missingColumns.isEmpty()) {
                    // לוג: עמודות חסרות
                    System.out.println("עמודות חסרות בטבלה קיימת: " + missingColumns);

                    model.addAttribute("missingColumns", missingColumns);
                    model.addAttribute("message", "העמודות הבאות חסרות בטבלה: " + missingColumns + ". האם תרצה להקים טבלה חדשה או לסיים?");
                    return "table-exists";
                }

                // לוג: כל העמודות בקובץ ה-Excel תואמות לטבלה
                System.out.println("כל העמודות תואמות לטבלה '" + tableName + "'.");

                System.out.println("מנסה להכניס נתונים לטבלה '" + tableName + "'.");
                databaseService.createTableAndInsertData(tableName, dataList);

            } else {
                System.out.println("הטבלה '" + tableName + "' לא קיימת. יוצר טבלה חדשה ומכניס נתונים.");

                File excelFile = new File(excelFilePath);
                if (!excelFile.exists()) {
                    System.out.println("קובץ ה-Excel לא נמצא בנתיב: " + excelFilePath);
                    model.addAttribute("message", "קובץ ה-Excel לא נמצא בנתיב שסיפקת.");
                    return "error";
                }

                try (FileInputStream excelFileStream = new FileInputStream(excelFile)) {
                    List<Map<String, String>> dataList = excelReader.readExcelFile(excelFileStream, sheetName, tableName);
                    databaseService.createTableAndInsertData(tableName, dataList);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "error";  // דף שגיאה אם יש בעיה
        }

        System.out.println("הכנסת הנתונים לטבלה '" + tableName + "' הסתיימה בהצלחה.");
        return "success";
    }

    @PostMapping("/create-new-table")
    public String createNewTable(
            @RequestParam("newTableName") String newTableName,
            @RequestParam("dbUrl") String dbUrl,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("excelFilePath") String excelFilePath,
            @RequestParam("sheetName") String sheetName,
            Model model) {

        try {
            System.out.println("יוצר טבלה חדשה בשם '" + newTableName + "'.");

            model.addAttribute("dbUrl", dbUrl);
            model.addAttribute("username", username);
            model.addAttribute("password", password);
            model.addAttribute("excelFilePath", excelFilePath);
            model.addAttribute("sheetName", sheetName);

            System.out.println("בודק אם הטבלה '" + newTableName + "' כבר קיימת.");
            if (databaseService.doesTableExist(newTableName)) {
                Set<String> dbColumns = databaseService.getTableColumns(newTableName);

                List<Map<String, String>> dataList;
                try (FileInputStream excelFileStream = new FileInputStream(new File(excelFilePath))) {
                    dataList = excelReader.readExcelFile(excelFileStream, sheetName, newTableName);
                }

                Set<String> excelColumns = dataList.get(0).keySet();

                Set<String> missingColumns = excelColumns.stream()
                        .filter(column -> !dbColumns.contains(column))
                        .collect(Collectors.toSet());

                if (!missingColumns.isEmpty()) {
                    System.out.println("עמודות בטבלה החדשה '" + newTableName + "' לא תואמות: " + missingColumns);

                    model.addAttribute("missingColumns", missingColumns);
                    model.addAttribute("message", "העמודות הבאות לא תואמות בטבלה '" + newTableName + "': " + missingColumns + ". אנא בחר שם טבלה אחר.");
                    return "table-exists";
                }

                System.out.println("מכניס נתונים לטבלה '" + newTableName + "' הקיימת.");
                databaseService.createTableAndInsertData(newTableName, dataList);
                model.addAttribute("tableName", newTableName);
                return "success";
            }

            File excelFile = new File(excelFilePath);
            if (!excelFile.exists()) {
                model.addAttribute("message", "קובץ ה-Excel לא נמצא בנתיב שסיפקת.");
                return "error";
            }

            try (FileInputStream excelFileStream = new FileInputStream(excelFile)) {
                List<Map<String, String>> dataList = excelReader.readExcelFile(excelFileStream, sheetName, newTableName);

                System.out.println("יוצר את הטבלה החדשה '" + newTableName + "' ומכניס את הנתונים.");
                databaseService.createTableAndInsertData(newTableName, dataList);
            }

            model.addAttribute("tableName", newTableName);

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }

        System.out.println("הטבלה '" + newTableName + "' נוצרה בהצלחה והנתונים הוכנסו.");
        return "success";
    }

    @PostMapping("/cancel-creation")
    public String cancelTableCreation(Model model) {
        System.out.println("המשתמש בחר שלא ליצור טבלה חדשה.");

        model.addAttribute("message", "המשתמש בחר שלא ליצור טבלה חדשה.");
        return "cancelled";
    }
}
