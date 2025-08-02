package com.api.helpers;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class EHConfigurationValue {
    @Value("${ehapi.lif.uat.driver}")
    private String lifUatDriver;

    @Value("${ehapi.lif.uat.url}")
    private String lifUatUrl;

    @Value("${ehapi.lif.uat.username}")
    private String lifUatUsername;

    @Value("${ehapi.lif.uat.password}")
    private String lifUatPassword;
}