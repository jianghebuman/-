package com.campus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 企业权威数据核验配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "campus.enterprise-verification")
public class EnterpriseVerificationProperties {

    private Boolean enabled = false;

    /**
     * 查询接口地址，支持 {creditCode} 和 {companyName} 占位符。
     */
    private String queryUrl;

    private String sourceName = "国家企业信用信息公示系统";

    private String sourceUrl = "https://www.gsxt.gov.cn/";

    private String companyNameField = "data.companyName";

    private String creditCodeField = "data.creditCode";

    private String statusField = "data.status";

    private List<String> normalStatuses = Arrays.asList("存续", "在业", "正常", "开业");

    private Integer connectTimeoutMs = 3000;

    private Integer readTimeoutMs = 5000;
}
