package com.unionpay.quartz.task.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "resttemplate")
@Data
public class RestTemplateProperties {
    private Integer retryTime = 1;
    private Integer maxTotal = 100;
    private Integer defaultMaxPerRoute = 20;
    private Integer timeToLive = 60000;
    private Integer connectTimeout = 20000;
    private Integer readTimeout = 50000;
    private Integer connectionRequestTimeout = 2000;
    private Integer socketTimeout = 50000;
    private Boolean bufferRequestBody = true;
}
