package com.api.config;

import com.api.helpers.EHConfigurationValue;
import com.api.helpers.DynamicDataSourceContextHolder;
import com.api.enums.DSEnum;
import com.api.utils.RSAUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Component("dataSourceRouting")
public class DynamicDataSourceRouting extends AbstractRoutingDataSource {
    @Autowired
    private EHConfigurationValue configurationValue;

    public DynamicDataSourceRouting(EHConfigurationValue configurationValue) {
        this.configurationValue = configurationValue;
        this.setTargetDataSources(initDataSourceMap());
        this.setDefaultTargetDataSource(dataSourceLifUat());
    }

    private Map<Object, Object> initDataSourceMap() {
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DSEnum.LIF_UAT, dataSourceLifUat());
        return dataSourceMap;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.getDataSourceContext();
    }

    private DataSource dataSourceLifUat() {
        return getDriverManagerDataSource(
                configurationValue.getLifUatDriver(),
                configurationValue.getLifUatUrl(),
                configurationValue.getLifUatUsername(),
                configurationValue.getLifUatPassword());
    }

    private DriverManagerDataSource getDriverManagerDataSource(String url, String username, String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password.isEmpty() ? "" : RSAUtil.decrypt(password));
        return dataSource;
    }

    private DriverManagerDataSource getDriverManagerDataSource(String driver, String url, String username, String password) {
        DriverManagerDataSource dataSource = getDriverManagerDataSource(url, username, password);
        dataSource.setDriverClassName(driver);
        return dataSource;
    }

}