package br.edu.icomp.ufam.to_brocado.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private static final String URL = env("TO_BROCADO_DB_URL",
            "jdbc:mysql://localhost:3306/to_brocado?useSSL=false&serverTimezone=America/Manaus&characterEncoding=UTF-8");
    private static final String USER = env("TO_BROCADO_DB_USER", "");
    private static final String PASSWORD = env("TO_BROCADO_DB_PASSWORD", "");

    private Database() { }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
