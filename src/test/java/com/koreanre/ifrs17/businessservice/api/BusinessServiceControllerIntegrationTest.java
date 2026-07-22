package com.koreanre.ifrs17.businessservice.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 11.2 필수 인수 시나리오를 커버하는 /execute, /catalog, /calls 통합 테스트.
 * V2__seed_pilot_services.sql로 등록된 IFRS17.CLOSING.STATUS + MCP-IFRS17-01 Client를 사용한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class BusinessServiceControllerIntegrationTest {

    private static final String SERVICE_ID = "IFRS17.CLOSING.STATUS";
    private static final String EXECUTE_URL = "/api/business-services/v1/" + SERVICE_ID + ":execute";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void execute_returnsSuccess_withValidRequestAndAuthorizedRole() throws Exception {
        String body = "{\"serviceVersion\":\"1.0\",\"parameters\":{\"closingYearMonth\":\"2024-01\"}}";

        mockMvc.perform(post(EXECUTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .header("X-Client-ID", "MCP-IFRS17-01")
                        .header("X-User-ID", "E12345")
                        .header("X-User-Roles", "IFRS17_CLOSING_VIEW")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.serviceId").value(SERVICE_ID))
                .andExpect(jsonPath("$.result.overallStatus").value("COMPLETED"));
    }

    @Test
    void execute_returnsValidationError_whenYearMonthFormatIsInvalid() throws Exception {
        String body = "{\"serviceVersion\":\"1.0\",\"parameters\":{\"closingYearMonth\":\"2024/01\"}}";

        mockMvc.perform(post(EXECUTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .header("X-Client-ID", "MCP-IFRS17-01")
                        .header("X-User-ID", "E12345")
                        .header("X-User-Roles", "IFRS17_CLOSING_VIEW")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("BS-VAL-001"));
    }

    @Test
    void execute_returnsAuthenticationError_whenClientIdHeaderMissing() throws Exception {
        String body = "{\"serviceVersion\":\"1.0\",\"parameters\":{\"closingYearMonth\":\"2024-01\"}}";

        mockMvc.perform(post(EXECUTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .header("X-User-ID", "E12345")
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("BS-AUTH-001"));
    }

    @Test
    void execute_returnsForbidden_whenUserRoleNotAuthorized() throws Exception {
        String body = "{\"serviceVersion\":\"1.0\",\"parameters\":{\"closingYearMonth\":\"2024-01\"}}";

        mockMvc.perform(post(EXECUTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .header("X-Client-ID", "MCP-IFRS17-01")
                        .header("X-User-ID", "E12345")
                        .header("X-User-Roles", "SOME_OTHER_ROLE")
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("BS-AUTH-003"));
    }

    @Test
    void execute_returnsNotFound_whenServiceVersionDoesNotExist() throws Exception {
        String body = "{\"serviceVersion\":\"9.9\",\"parameters\":{\"closingYearMonth\":\"2024-01\"}}";

        mockMvc.perform(post(EXECUTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .header("X-Client-ID", "MCP-IFRS17-01")
                        .header("X-User-ID", "E12345")
                        .header("X-User-Roles", "IFRS17_CLOSING_VIEW")
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("BS-SVC-404"));
    }

    @Test
    void execute_returnsSuccessWithEmptyResult_whenNoDataForFutureMonth() throws Exception {
        String body = "{\"serviceVersion\":\"1.0\",\"parameters\":{\"closingYearMonth\":\"2099-01\"}}";

        mockMvc.perform(post(EXECUTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .header("X-Client-ID", "MCP-IFRS17-01")
                        .header("X-User-ID", "E12345")
                        .header("X-User-Roles", "IFRS17_CLOSING_VIEW")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.result.overallStatus").value("NOT_STARTED"))
                .andExpect(jsonPath("$.result.stages").isEmpty());
    }

    @Test
    void catalog_listsSeededPilotServices() throws Exception {
        mockMvc.perform(get("/api/business-services/v1/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void callLog_isTraceableAfterSuccessfulExecution() throws Exception {
        String body = "{\"serviceVersion\":\"1.0\",\"parameters\":{\"closingYearMonth\":\"2024-01\"}}";

        MvcResult result = mockMvc.perform(post(EXECUTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .header("X-Client-ID", "MCP-IFRS17-01")
                        .header("X-User-ID", "E12345")
                        .header("X-User-Roles", "IFRS17_CLOSING_VIEW")
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String requestId = json.get("requestId").asText();
        assertThat(requestId).isNotBlank();

        mockMvc.perform(get("/api/business-services/v1/calls/" + requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId))
                .andExpect(jsonPath("$.statusCode").value("SUCCESS"))
                .andExpect(jsonPath("$.userId").value("E12345"));
    }
}
