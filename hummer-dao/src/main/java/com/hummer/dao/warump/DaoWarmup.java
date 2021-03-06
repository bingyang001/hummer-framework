package com.hummer.dao.warump;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hummer.common.warmup.Warmup;
import com.hummer.common.warmup.WarmupResponse;
import com.hummer.dao.configuration.DataSourceInitConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DaoWarmup implements Warmup {
    @Autowired
    @Lazy
    private DataSourceInitConfiguration configuration;

    @Override
    public List<WarmupResponse> execute() {
        long totalStart = System.currentTimeMillis();
        ImmutableMap<String, DataSource> dataSourceMap = configuration.dataSourceMap();
        log.info("begin execute warmup data source item {}", dataSourceMap.keySet());
        List<WarmupResponse> responses = Lists.newArrayListWithCapacity(dataSourceMap.size());
        for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            long start = System.currentTimeMillis();
            try {
                if (entry.getValue() instanceof DruidDataSource) {
                    ((DruidDataSource) entry.getValue()).init();
                } else if (entry.getValue() instanceof HikariDataSource) {
                    entry.getValue().getConnection().prepareStatement("select 1").execute();
                } else {
                    responses.add(WarmupResponse.builder()
                            .costMillis(System.currentTimeMillis() - start)
                            .message("failed db pool driver invalid")
                            .key(entry.getKey())
                            .success(true)
                            .build());
                    continue;
                }
                responses.add(WarmupResponse.builder()
                        .costMillis(System.currentTimeMillis() - start)
                        .message("ok")
                        .key(entry.getKey())
                        .success(true)
                        .build());
            } catch (SQLException e) {
                responses.add(WarmupResponse.builder()
                        .costMillis(System.currentTimeMillis() - start)
                        .message(e.toString())
                        .key(entry.getKey())
                        .success(false)
                        .build());
            }
        }
        log.info("warmup execute done,cost {}", System.currentTimeMillis() - totalStart);
        return responses;
    }
}
