package org.example.demo.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBUtils {

    private static final String JDBC_MYSQL_HOST = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "injection";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "admin";

    private DBUtils() {
    }

    public static Connection getConnection() {
        // Commented during the implementation of the Connection Pool Homework - TODO - remove this code in final solution
//		try {
//			Class.forName("com.mysql.cj.jdbc.Driver");
//			return DriverManager.getConnection(JDBC_MYSQL_HOST + DB_NAME, USERNAME, PASSWORD);
//		} catch (SQLException | ClassNotFoundException e) {
//			throw new RuntimeException(e);
//		}


        try {
            return DbcpDemo.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}

