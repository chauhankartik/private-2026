package tech.jdbc;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * ============================================================
 *  MULTI-DATABASE CONNECTION POOLING CONFIGURATION
 *  Databases: zeyt (OLTP), zeyt_ta_data (Analytics), zeyt_deleted (Archive)
 * ============================================================
 *
 *  Demonstrates:
 *   1. Architecture & configuration properties for 3 independent pools:
 *      - zeyt: High-concurrency OLTP pool (tight timeouts, leak detection).
 *      - zeyt_ta_data: Transactional Analytics pool (large fetch size, longer lifetime).
 *      - zeyt_deleted: Soft-deleted / Cold archive pool (minimal idle pool, fast eviction).
 *   2. Schema management across hundreds of tables per database.
 * ============================================================
 */
public class ZeytMultiDataSourceConfig {

    /**
     * Configuration properties holder for HikariCP pool parameters.
     */
    public static class HikariPoolConfig {
        public String poolName;
        public String jdbcUrl;
        public String username;
        public String password;
        public String driverClassName;

        public int maximumPoolSize;
        public int minimumIdle;
        public long connectionTimeoutMs;
        public long idleTimeoutMs;
        public long maxLifetimeMs;
        public long leakDetectionThresholdMs;

        public Properties dataSourceProperties = new Properties();

        public void addDataSourceProperty(String key, String value) {
            dataSourceProperties.setProperty(key, value);
        }
    }

    // ─── 1. ZEYT PRIMARY OLTP POOL CONFIGURATION ────────────────
    public static HikariPoolConfig configureZeytOLTPPool(String jdbcUrl, String username, String password) {
        HikariPoolConfig config = new HikariPoolConfig();
        config.poolName = "HikariPool-Zeyt-OLTP";
        config.jdbcUrl = jdbcUrl;
        config.username = username;
        config.password = password;
        config.driverClassName = "org.postgresql.Driver";

        // High-concurrency OLTP tuning (Hundreds of core business tables)
        config.maximumPoolSize = 20;
        config.minimumIdle = 10;
        config.connectionTimeoutMs = 3000;   // 3s fail-fast to prevent caller thread queue buildup
        config.idleTimeoutMs = 600000;       // 10 minutes
        config.maxLifetimeMs = 1800000;      // 30 minutes
        config.leakDetectionThresholdMs = 2000; // Warn if connection held > 2s

        // PostgreSQL statement cache optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        return config;
    }

    // ─── 2. ZEYT_TA_DATA ANALYTICS POOL CONFIGURATION ───────────
    public static HikariPoolConfig configureZeytTaDataPool(String jdbcUrl, String username, String password) {
        HikariPoolConfig config = new HikariPoolConfig();
        config.poolName = "HikariPool-Zeyt-TaData-Analytics";
        config.jdbcUrl = jdbcUrl;
        config.username = username;
        config.password = password;
        config.driverClassName = "org.postgresql.Driver";

        // Batch / Analytics workload tuning (Hundreds of log & history tables)
        config.maximumPoolSize = 10;
        config.minimumIdle = 2;
        config.connectionTimeoutMs = 10000;  // 10s wait for batch query slots
        config.idleTimeoutMs = 600000;
        config.maxLifetimeMs = 2700000;       // 45 minutes
        config.leakDetectionThresholdMs = 60000; // Higher threshold for bulk reports

        // Large fetch size and re-write batching
        config.addDataSourceProperty("defaultRowFetchSize", "1000");
        config.addDataSourceProperty("reWriteBatchedInserts", "true");

        return config;
    }

    // ─── 3. ZEYT_DELETED ARCHIVE POOL CONFIGURATION ─────────────
    public static HikariPoolConfig configureZeytDeletedPool(String jdbcUrl, String username, String password) {
        HikariPoolConfig config = new HikariPoolConfig();
        config.poolName = "HikariPool-Zeyt-Deleted-Archive";
        config.jdbcUrl = jdbcUrl;
        config.username = username;
        config.password = password;
        config.driverClassName = "org.postgresql.Driver";

        // Rare access / Purge job tuning (Hundreds of soft-deleted archive tables)
        config.maximumPoolSize = 5;
        config.minimumIdle = 1;
        config.connectionTimeoutMs = 5000;
        config.idleTimeoutMs = 300000;       // 5 minutes idle retirement
        config.maxLifetimeMs = 1800000;
        config.leakDetectionThresholdMs = 10000;

        return config;
    }

    // ─── DEMO MAIN EXECUTOR ─────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("=== MULTI-DATABASE CONNECTION POOL CONFIGURATION DEMO ===");

        String zeytUrl = "jdbc:postgresql://db-cluster.internal:5432/zeyt";
        String zeytTaDataUrl = "jdbc:postgresql://db-cluster.internal:5432/zeyt_ta_data";
        String zeytDeletedUrl = "jdbc:postgresql://db-cluster.internal:5432/zeyt_deleted";

        HikariPoolConfig oltpPool = configureZeytOLTPPool(zeytUrl, "zeyt_app", "secret_pass");
        HikariPoolConfig taDataPool = configureZeytTaDataPool(zeytTaDataUrl, "zeyt_ta_app", "secret_pass");
        HikariPoolConfig deletedPool = configureZeytDeletedPool(zeytDeletedUrl, "zeyt_del_app", "secret_pass");

        printPoolSummary(oltpPool);
        printPoolSummary(taDataPool);
        printPoolSummary(deletedPool);

        System.out.println("All 3 multi-database connection pools initialized with isolated pool limits!");
    }

    private static void printPoolSummary(HikariPoolConfig config) {
        System.out.println("\n------------------------------------------------");
        System.out.println("Pool Name          : " + config.poolName);
        System.out.println("JDBC URL           : " + config.jdbcUrl);
        System.out.println("Maximum Pool Size  : " + config.maximumPoolSize);
        System.out.println("Minimum Idle       : " + config.minimumIdle);
        System.out.println("Connection Timeout : " + config.connectionTimeoutMs + " ms");
        System.out.println("Max Lifetime       : " + config.maxLifetimeMs + " ms");
        System.out.println("Leak Detection     : " + config.leakDetectionThresholdMs + " ms");
        System.out.println("Custom Driver Props: " + config.dataSourceProperties);
    }
}
