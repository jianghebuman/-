package com.campus.service;

import com.campus.config.EnterpriseVerificationProperties;
import com.campus.entity.Enterprise;
import com.campus.service.EnterpriseVerificationService.VerificationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class EnterpriseVerificationServiceTest {

    @Test
    void verifiesMatchedEnterpriseFromAuthorityResponse() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/verify", exchange -> {
            byte[] body = "{\"data\":{\"companyName\":\"测试科技有限公司\",\"creditCode\":\"913100001234567897\",\"status\":\"存续\"}}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            EnterpriseVerificationProperties properties = new EnterpriseVerificationProperties();
            properties.setEnabled(true);
            properties.setQueryUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/verify?creditCode={creditCode}");
            EnterpriseVerificationService service = new EnterpriseVerificationService(properties, new ObjectMapper());

            Enterprise enterprise = new Enterprise();
            enterprise.setCompanyName("测试科技有限公司");

            VerificationResult result = service.verify(enterprise, "913100001234567897");

            assertTrue(result.getMatched());
            assertEquals(1, result.getVerifyResult());
            assertEquals("测试科技有限公司", result.getVerifyCompanyName());
            assertEquals("913100001234567897", result.getVerifyCreditCode());
            assertNotNull(result.getVerifySnapshotHash());
        } finally {
            server.stop(0);
        }
    }
}
