package com.campus.service;

import com.campus.config.EnterpriseVerificationProperties;
import com.campus.entity.Enterprise;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 联网核验企业主体信息，并只保存必要的留痕摘要。
 */
@Service
public class EnterpriseVerificationService {

    private final EnterpriseVerificationProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public EnterpriseVerificationService(EnterpriseVerificationProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMs());
        factory.setReadTimeout(properties.getReadTimeoutMs());
        this.restTemplate = new RestTemplate(factory);
    }

    public VerificationResult verify(Enterprise enterprise, String licenseNo) {
        VerificationResult result = baseResult();
        if (!Boolean.TRUE.equals(properties.getEnabled()) || !StringUtils.hasText(properties.getQueryUrl())) {
            result.setVerifyResult(3);
            result.setVerifyRemark("未配置权威数据接口，已保留人工审核");
            return result;
        }

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(buildUrl(licenseNo, enterprise.getCompanyName()), String.class);
            String body = response.getBody() == null ? "" : response.getBody();
            result.setVerifySnapshotHash(sha256(body));

            Object root = objectMapper.readValue(body, Object.class);
            String sourceCompanyName = firstText(root, properties.getCompanyNameField(), "companyName", "name", "entName");
            String sourceCreditCode = firstText(root, properties.getCreditCodeField(), "creditCode", "unifiedSocialCreditCode", "uscc");
            String sourceStatus = firstText(root, properties.getStatusField(), "status", "regStatus", "enterpriseStatus");

            result.setVerifyCompanyName(sourceCompanyName);
            result.setVerifyCreditCode(sourceCreditCode);
            result.setVerifyStatus(sourceStatus);

            boolean codeMatched = normalizeCode(licenseNo).equals(normalizeCode(sourceCreditCode));
            boolean nameMatched = normalizeName(enterprise.getCompanyName()).equals(normalizeName(sourceCompanyName));
            boolean statusNormal = !StringUtils.hasText(sourceStatus) || containsAny(sourceStatus, properties.getNormalStatuses());
            boolean matched = codeMatched && nameMatched && statusNormal;

            result.setMatched(matched);
            result.setVerifyResult(matched ? 1 : 2);
            result.setVerifyRemark(matched
                    ? "权威来源返回的企业名称、统一社会信用代码与提交信息一致"
                    : "权威来源比对未完全一致，已转人工审核");
            return result;
        } catch (Exception ex) {
            result.setVerifyResult(3);
            result.setVerifyRemark("权威数据接口查询失败，已转人工审核：" + ex.getClass().getSimpleName());
            return result;
        }
    }

    private VerificationResult baseResult() {
        VerificationResult result = new VerificationResult();
        result.setVerifySource(properties.getSourceName());
        result.setVerifySourceUrl(properties.getSourceUrl());
        result.setVerifyTime(new Date());
        result.setVerifyResult(0);
        result.setMatched(false);
        return result;
    }

    private String buildUrl(String creditCode, String companyName) {
        String url = properties.getQueryUrl();
        String code = UriUtils.encodeQueryParam(creditCode == null ? "" : creditCode, StandardCharsets.UTF_8);
        String name = UriUtils.encodeQueryParam(companyName == null ? "" : companyName, StandardCharsets.UTF_8);
        if (url.contains("{creditCode}") || url.contains("{companyName}")) {
            return url.replace("{creditCode}", code).replace("{companyName}", name);
        }
        return url + (url.contains("?") ? "&" : "?") + "creditCode=" + code;
    }

    private String firstText(Object root, String configuredPath, String... fallbackPaths) {
        String value = readText(root, configuredPath);
        if (StringUtils.hasText(value)) {
            return value;
        }
        for (String path : fallbackPaths) {
            value = readText(root, path);
            if (StringUtils.hasText(value)) {
                return value;
            }
            value = readText(root, "data." + path);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String readText(Object current, String path) {
        if (current == null || !StringUtils.hasText(path)) {
            return null;
        }
        for (String part : path.split("\\.")) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else if (current instanceof List && !((List<?>) current).isEmpty()) {
                current = ((List<?>) current).get(0);
                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(part);
                }
            } else {
                return null;
            }
        }
        return current == null ? null : String.valueOf(current).trim();
    }

    private boolean containsAny(String text, List<String> allowed) {
        if (!StringUtils.hasText(text) || allowed == null || allowed.isEmpty()) {
            return true;
        }
        for (String item : allowed) {
            if (StringUtils.hasText(item) && text.contains(item)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeCode(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.replaceAll("[\\s（）()【】\\[\\]]", "").toUpperCase();
    }

    private String sha256(String text) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    @Data
    public static class VerificationResult {
        private String verifySource;
        private String verifySourceUrl;
        private Date verifyTime;
        private String verifyCompanyName;
        private String verifyCreditCode;
        private String verifyStatus;
        /** 0未核验1一致2不一致3未接入或异常 */
        private Integer verifyResult;
        private String verifyRemark;
        private String verifySnapshotHash;
        private Boolean matched;
    }
}
