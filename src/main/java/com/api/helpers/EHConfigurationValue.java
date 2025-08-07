package com.api.helpers;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class EHConfigurationValue {
    @Value("${ehapi.db.lif.uat.driver}")
private String lifUatDriver;

@Value("${ehapi.db.lif.uat.url}")
private String lifUatUrl;

@Value("${ehapi.db.lif.uat.username}")
private String lifUatUsername;

@Value("${ehapi.db.lif.uat.password}")
private String lifUatPassword;
}