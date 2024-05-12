package org.example.utils;



import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;



public class DbcpDemo {

    private static BasicDataSource ds = new BasicDataSource();

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        ds.setUrl("jdbc:mysql://localhost:3306/injection");
        ds.setUsername("root");
        ds.setPassword("admin");
        ds.setMinIdle(3);
        ds.setTimeBetweenEvictionRunsMillis(1000);
        ds.setMaxIdle(20);
        ds.setMaxOpenPreparedStatements(200);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = getConnection();
        try (conn) {
            System.out.println(conn);
        }
        System.out.println(conn);
    }


}

