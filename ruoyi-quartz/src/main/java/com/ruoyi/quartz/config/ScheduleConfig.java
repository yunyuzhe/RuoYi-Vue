package com.ruoyi.quartz.config;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

/**
 * 定时任务配置
 */
@Slf4j
@Configuration
public class ScheduleConfig {

    /**
     * 配置 SchedulerFactoryBean
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) throws IOException {
        log.info("========================================");
        log.info("开始初始化 SchedulerFactoryBean");
        log.info("========================================");

        // 测试数据源
        try (Connection conn = dataSource.getConnection()) {
            log.info("✅ DataSource 连接测试成功");
            log.info("DataSource 类型: {}", dataSource.getClass().getName());
            log.info("数据库 URL: {}", conn.getMetaData().getURL());
        } catch (Exception e) {
            log.error("❌ DataSource 连接测试失败", e);
            throw new RuntimeException("数据源连接失败", e);
        }

        // ✅✅✅ 关键：在创建 SchedulerFactoryBean 之前设置静态数据源
        QuartzDataSourceConnectionProvider.setStaticDataSource(dataSource);

        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        // ❌ 不要使用 setDataSource
        // factory.setDataSource(dataSource);

        // 延迟启动
        factory.setStartupDelay(10);

        // 应用上下文
        factory.setApplicationContextSchedulerContextKey("applicationContext");

        // 覆盖已存在的任务
        factory.setOverwriteExistingJobs(true);

        // 自动启动
        factory.setAutoStartup(true);

        // ✅ 设置 Quartz 属性
        factory.setQuartzProperties(quartzProperties());

        log.info("========================================");
        log.info("SchedulerFactoryBean 配置完成");
        log.info("========================================");

        return factory;
    }

    /**
     * Quartz 属性配置
     */
    private Properties quartzProperties() {
        Properties prop = new Properties();

        // 调度器配置
        prop.put("org.quartz.scheduler.instanceName", "RuoyiScheduler");
        prop.put("org.quartz.scheduler.instanceId", "AUTO");
        prop.put("org.quartz.scheduler.skipUpdateCheck", "true");

        // 线程池配置
        prop.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        prop.put("org.quartz.threadPool.threadCount", "10");
        prop.put("org.quartz.threadPool.threadPriority", "5");
        prop.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true");

        // JobStore 配置
        prop.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        prop.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        prop.put("org.quartz.jobStore.tablePrefix", "QRTZ_");

        // ✅✅✅ 集群配置
        prop.put("org.quartz.jobStore.isClustered", "true");
        prop.put("org.quartz.jobStore.clusterCheckinInterval", "20000");

        // 其他配置
        prop.put("org.quartz.jobStore.useProperties", "false");
        prop.put("org.quartz.jobStore.misfireThreshold", "60000");
        prop.put("org.quartz.jobStore.acquireTriggersWithinLock", "true");

        // ✅ 配置数据源
        prop.put("org.quartz.jobStore.dataSource", "myDataSource");
        prop.put("org.quartz.dataSource.myDataSource.connectionProvider.class",
                "com.ruoyi.quartz.config.QuartzDataSourceConnectionProvider");

        log.info("Quartz 属性配置完成");
        return prop;
    }
}
