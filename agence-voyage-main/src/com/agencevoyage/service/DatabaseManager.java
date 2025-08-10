package com.agencevoyage.service;

import java.nio.file.*;
import java.io.File;
import java.net.URISyntaxException;
import java.sql.*;

public class DatabaseManager {
    // Resolve an app folder we control (works for jar, IDE, jpackage)
    private static final Path APP_DIR = resolveAppDir();
    private static final Path DATA_DIR = APP_DIR.resolve("data");
    private static final String JDBC_URL = "jdbc:h2:file:" + DATA_DIR.resolve("agenceDB").toString().replace('\\','/')
            + ";AUTO_SERVER=TRUE;MODE=PostgreSQL;DATABASE_TO_UPPER=TRUE";
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASSWORD = "";

    static {
        try {
            // Ensure data/ exists wherever the app runs
            Files.createDirectories(DATA_DIR);

            // Ensure H2 driver is visible even if running from plain cmd
            Class.forName("org.h2.Driver");

            initializeSchema();
        } catch (Exception e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    private static void initializeSchema() {
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS CLIENT(
                  ID VARCHAR(36) PRIMARY KEY,
                  NOM VARCHAR(100) NOT NULL,
                  PRENOM VARCHAR(100) NOT NULL,
                  EMAIL VARCHAR(200),
                  TELEPHONE VARCHAR(50),
                  ISDELETED BOOLEAN DEFAULT FALSE
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS PRESTATION(
                  ID VARCHAR(36) PRIMARY KEY,
                  DESIGNATION VARCHAR(150) NOT NULL,
                  TYPE VARCHAR(100),
                  HOTEL VARCHAR(150),
                  VILLE VARCHAR(100),
                  PAYS VARCHAR(100),
                  DATEDEBUT DATE NOT NULL,
                  DATEFIN DATE NOT NULL,
                  PRIX DOUBLE,
                  PLACESDISPONIBLES INT
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS RESERVATION(
                  ID VARCHAR(36) PRIMARY KEY,
                  CLIENT_ID VARCHAR(36) NOT NULL,
                  PRESTATION_ID VARCHAR(36) NOT NULL,
                  DATE_RESERVATION DATE NOT NULL,
                  CONSTRAINT FK_RES_CLIENT FOREIGN KEY (CLIENT_ID) REFERENCES CLIENT(ID),
                  CONSTRAINT FK_RES_PRESTATION FOREIGN KEY (PRESTATION_ID) REFERENCES PRESTATION(ID)
                )
            """);

            // Useful indexes + uniqueness to prevent duplicates
            st.execute("CREATE INDEX IF NOT EXISTS IDX_RES_CLIENT ON RESERVATION(CLIENT_ID)");
            st.execute("CREATE INDEX IF NOT EXISTS IDX_RES_PRESTATION ON RESERVATION(PRESTATION_ID)");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS UQ_CLIENT_EMAIL ON CLIENT(EMAIL)");
            st.execute("CREATE UNIQUE INDEX IF NOT EXISTS UQ_CLIENT_PHONE ON CLIENT(TELEPHONE)");
            st.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS UQ_PRESTATION_KEY
                ON PRESTATION(DESIGNATION, HOTEL, VILLE, PAYS, DATEDEBUT, DATEFIN)
            """);
            st.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS UQ_RES_CLIENT_PREST
                ON RESERVATION(CLIENT_ID, PRESTATION_ID)
            """);

            // Add missing column if old DB copied in
            ensureColumn(c, "CLIENT", "ISDELETED", "BOOLEAN DEFAULT FALSE");
        } catch (SQLException e) {
            throw new RuntimeException("Schema init error", e);
        }
    }

    private static void ensureColumn(Connection c, String table, String column, String ddlType) throws SQLException {
        String q = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME=? AND UPPER(COLUMN_NAME)=?";
        try (PreparedStatement ps = c.prepareStatement(q)) {
            ps.setString(1, table.toUpperCase());
            ps.setString(2, column.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (Statement st = c.createStatement()) {
                        st.execute("ALTER TABLE " + table + " ADD " + column + " " + ddlType);
                    }
                }
            }
        }
    }

    private static Path resolveAppDir() {
        try {
            // folder of running JAR / app image; fallback to working dir in IDE
            File codeLoc = new File(DatabaseManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            File dir = codeLoc.isFile() ? codeLoc.getParentFile() : codeLoc; // jar -> parent, dir -> itself
            return dir.toPath().toAbsolutePath();
        } catch (URISyntaxException e) {
            return Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        }
    }
}
