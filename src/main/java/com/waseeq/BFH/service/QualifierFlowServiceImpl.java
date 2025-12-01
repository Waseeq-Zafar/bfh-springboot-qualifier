package com.waseeq.BFH.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waseeq.BFH.dto.GenerateWebhookRequest;
import com.waseeq.BFH.dto.GenerateWebhookResponse;
import com.waseeq.BFH.entity.Solution;
import com.waseeq.BFH.repository.SolutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QualifierFlowServiceImpl implements QualifierFlowService {

    private final RestTemplate restTemplate;
    private final SolutionRepository repository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${app.name}")
    private String name;

    @Value("${app.regNo}")
    private String regNo;

    @Value("${app.email}")
    private String email;

    @Value("${app.baseUrl}")
    private String baseUrl;

    @Value("${app.generatePath}")
    private String generatePath;

    @Value("${app.defaultAuthPrefix:Bearer }")
    private String defaultAuthPrefix;

    @Override
    public void executeOnce() {

        log.info("Starting BFH qualifier flow for regNo={}", regNo);

        String generateUrl = baseUrl + generatePath;
        GenerateWebhookRequest req = new GenerateWebhookRequest(name, regNo, email);

        ResponseEntity<GenerateWebhookResponse> genResp;
        try {
            genResp = restTemplate.postForEntity(generateUrl, req, GenerateWebhookResponse.class);
        } catch (Exception e) {
            log.error("Failed to call generateWebhook: {}", e.getMessage(), e);
            return;
        }

        if (!genResp.getStatusCode().is2xxSuccessful() || genResp.getBody() == null) {
            log.error("generateWebhook returned non-2xx or empty body: {}", genResp);
            return;
        }

        String webhookUrl = genResp.getBody().getWebhook();
        String accessToken = genResp.getBody().getAccessToken();
        log.info("Webhook URL: {}", webhookUrl);

        // Determine question
        int lastTwo = extractLastTwoDigits(regNo);
        boolean isOdd = (lastTwo % 2 == 1);
        String questionId = isOdd ? "Q1" : "Q2";

        String finalQuery = isOdd ? getQuestion1SQL() : getQuestion2SQL();

        // Save before submission
        Solution s = Solution.builder()
                .regNo(regNo)
                .questionId(questionId)
                .finalQuery(finalQuery)
                .webhookUrl(webhookUrl)
                .accessTokenUsed(mask(accessToken))
                .createdAt(Instant.now())
                .build();

        repository.save(s);

        // Prepare submission payload
        Map<String, String> payload = new HashMap<>();
        payload.put("finalQuery", finalQuery);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", defaultAuthPrefix + accessToken);

        try {
            ResponseEntity<String> submitResp = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(payload, headers),
                    String.class
            );

            s.setSubmissionStatus(submitResp.getStatusCode().toString());
            s.setSubmissionResponse(submitResp.getBody());
            repository.save(s);

            log.info("Submission success: {}", submitResp.getStatusCode());

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Unauthorized: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error submitting query: {}", e.getMessage(), e);
        }
    }

    private int extractLastTwoDigits(String value) {
        String digits = value.replaceAll("\\D+", "");
        if (digits.length() >= 2) {
            return Integer.parseInt(digits.substring(digits.length() - 2));
        }
        return 1;
    }

    private String getQuestion1SQL() {
        return "SELECT d.DEPARTMENT_NAME, "
                + "AVG(FLOOR(DATEDIFF(CURDATE(), e.DOB) / 365)) AS AVERAGE_AGE, "
                + "GROUP_CONCAT(CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) ORDER BY e.EMP_ID SEPARATOR ', ') AS EMPLOYEE_LIST "
                + "FROM PAYMENTS p "
                + "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID "
                + "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID "
                + "WHERE p.AMOUNT > 70000 "
                + "GROUP BY d.DEPARTMENT_ID, d.DEPARTMENT_NAME "
                + "ORDER BY d.DEPARTMENT_ID DESC "
                + "LIMIT 10;";
    }

    private String getQuestion2SQL() {
        return "SELECT d.DEPARTMENT_NAME, "
                + "MAX(p.AMOUNT) AS SALARY, "
                + "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS EMPLOYEE_NAME, "
                + "FLOOR(DATEDIFF(CURDATE(), e.DOB) / 365) AS AGE "
                + "FROM PAYMENTS p "
                + "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID "
                + "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID "
                + "WHERE DAY(p.PAYMENT_TIME) != 1 "
                + "GROUP BY d.DEPARTMENT_NAME, e.EMP_ID, e.DOB "
                + "ORDER BY SALARY DESC;";
    }

    private String mask(String token) {
        if (token == null) return null;
        if (token.length() <= 10) return "****";
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}
