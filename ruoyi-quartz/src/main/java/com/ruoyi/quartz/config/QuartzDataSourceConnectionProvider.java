package com.ruoyi.quartz.config;

import lombok.extern.slf4j.Slf4j;
import org.quartz.utils.ConnectionProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Quartz 数据源连接提供者
 */
@Slf4j
public class QuartzDataSourceConnectionProvider implements ConnectionProvider {

    private static DataSource staticDataSource;

    private DataSource dataSource;

    public static void setStaticDataSource(DataSource ds) {
        staticDataSource = ds;
        log.info("✅ 静态数据源已设置: {}", ds.getClass().getName());
    }

    public QuartzDataSourceConnectionProvider() {
        this.dataSource = staticDataSource;
        if (this.dataSource == null) {
            log.error("❌ 构造时数据源为 null");
            throw new RuntimeException("DataSource is null");
        }
        log.info("✅ ConnectionProvider 实例创建成功");
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is null");
        }
        return dataSource.getConnection();
    }

    @Override
    public void shutdown() throws SQLException {
        log.info("ConnectionProvider shutdown (数据源由 Spring 管理)");
    }

    @Override
    public void initialize() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is null in initialize()");
        }
        log.info("✅ ConnectionProvider initialized");
    }
}
