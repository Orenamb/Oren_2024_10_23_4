package com.example;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DatabaseService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // בדיקת קיום הטבלה בבסיס הנתונים
    public boolean doesTableExist(String tableName) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, tableName, null);
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // בדיקת קיום כל העמודות מה-Excel בטבלה בבסיס הנתונים
    public boolean doColumnsExistInTable(String tableName, Set<String> excelColumnNames) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, null);
            Set<String> tableColumnNames = new HashSet<>();

            // אוסף את שמות כל העמודות בטבלה
            while (columns.next()) {
                tableColumnNames.add(columns.getString("COLUMN_NAME"));
            }

            // השוואה בין שמות העמודות מהטבלה לאלו שבקובץ ה-Excel
            return tableColumnNames.containsAll(excelColumnNames);

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // הדפסת פרטי החיבור לבסיס הנתונים עם טיפול בחריגה
    public void printDatabaseDetails() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            System.out.println("URL: " + connection.getMetaData().getURL());
            System.out.println("Username: " + connection.getMetaData().getUserName());
        } catch (SQLException e) {
            System.out.println("Failed to get database details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // יצירת טבלה והכנסת הנתונים לתוכה
    public void createTableAndInsertData(String tableName, List<Map<String, String>> dataList) {
        if (dataList.isEmpty()) {
            System.out.println("No data to insert.");
            return;
        }

        // יצירת הטבלה על סמך העמודות מהנתונים
        Map<String, String> firstRow = dataList.get(0);
        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE IF NOT EXISTS `" + tableName + "` (");

        for (String columnName : firstRow.keySet()) {
            createTableQuery.append("`").append(columnName).append("` VARCHAR(255), ");
        }
        createTableQuery.setLength(createTableQuery.length() - 2);  // הסרת הפסיק האחרון
        createTableQuery.append(");");

        jdbcTemplate.execute(createTableQuery.toString());

        // הכנסת הנתונים לטבלה
        for (Map<String, String> rowData : dataList) {
            StringBuilder insertQuery = new StringBuilder("INSERT INTO `" + tableName + "` (");

            for (String columnName : rowData.keySet()) {
                insertQuery.append("`").append(columnName).append("`, ");
            }
            insertQuery.setLength(insertQuery.length() - 2);  // הסרת הפסיק האחרון
            insertQuery.append(") VALUES (");

            for (String value : rowData.values()) {
                insertQuery.append("'").append(value).append("', ");
            }
            insertQuery.setLength(insertQuery.length() - 2);  // הסרת הפסיק האחרון
            insertQuery.append(");");

            jdbcTemplate.execute(insertQuery.toString());
        }

        System.out.println("Table " + tableName + " created and data inserted successfully.");
    }

    // מתודה שמחזירה את שמות העמודות בטבלה
    public Set<String> getTableColumns(String tableName) {
        Set<String> tableColumnNames = new HashSet<>();

        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, null);

            // איסוף שמות העמודות
            while (columns.next()) {
                tableColumnNames.add(columns.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tableColumnNames;
    }
}
