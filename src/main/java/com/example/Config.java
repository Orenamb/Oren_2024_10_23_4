package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;

@Configuration
public class Config {

    // פרטי החיבור לבסיס הנתונים
    private String url = "jdbc:mysql://192.168.1.53:3306/world";
    private String username = "Oren";
    private String password = "Orenamb1!";

    @Bean
    public DataSource getDataSource() {
        return DataSourceBuilder.create()
                .url(url)  // כתובת ה-URL של בסיס הנתונים
                .username(username)  // שם המשתמש
                .password(password)  // סיסמה
                .driverClassName("com.mysql.cj.jdbc.Driver")  // הנהג של MySQL
                .build();
    }

    // הוספת Bean של ExcelReader
    @Bean
    public ExcelReader excelReader() {
        return new ExcelReader();  // מחזיר מופע של ExcelReader
    }

    // פונקציות גישה (Getters) לפרטי החיבור אם תצטרך
    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
