package com.koreanre.ifrs17.businessservice.core.masking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 4.2 MaskingPolicy: 민감정보 마스킹. 6.4에 따라 주민번호·계좌번호·개인 연락처 등은
 * 응답과 감사로그 모두에서 마스킹한다.
 *
 * <p>필드명 키워드 기반의 범용 마스킹으로, 응답 DTO를 직렬화한 JSON 트리를
 * 재귀적으로 순회하며 민감 필드로 판단되는 문자열 값을 마스킹한다.</p>
 */
@Component
public class MaskingPolicy {

    public static final String DEFAULT_MASKING = "DEFAULT_MASKING";
    public static final String NO_MASKING = "NONE";

    private static final List<String> SENSITIVE_KEYWORDS = Arrays.asList(
            "rrn", "residentnumber", "ssn", "account", "accountnumber",
            "phone", "mobile", "telno", "contact", "email", "address");

    private final ObjectMapper objectMapper;

    public MaskingPolicy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public <T> T mask(T result, String policy) {
        if (result == null || NO_MASKING.equals(policy)) {
            return result;
        }
        JsonNode tree = objectMapper.valueToTree(result);
        if (!tree.isContainerNode()) {
            return result;
        }
        maskNode(tree, null);
        try {
            return (T) objectMapper.treeToValue(tree, result.getClass());
        } catch (Exception e) {
            // 마스킹 대상 필드 타입이 String이 아닌 등 원본 구조를 복원할 수 없는 경우
            // 마스킹되지 않은 원본을 그대로 반환하기보다 예외를 표면화한다.
            throw new IllegalStateException("마스킹 처리 후 응답 객체 복원에 실패했습니다.", e);
        }
    }

    private void maskNode(JsonNode node, String fieldName) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode value = entry.getValue();
                if (value.isTextual() && isSensitiveField(entry.getKey())) {
                    entry.setValue(objectNode.textNode(maskText(value.asText())));
                } else {
                    maskNode(value, entry.getKey());
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode element : arrayNode) {
                maskNode(element, fieldName);
            }
        }
    }

    private boolean isSensitiveField(String fieldName) {
        String normalized = fieldName.toLowerCase(Locale.ROOT).replace("_", "");
        return SENSITIVE_KEYWORDS.stream().anyMatch(normalized::contains);
    }

    private String maskText(String value) {
        if (value == null || value.length() <= 2) {
            return "***";
        }
        int visible = Math.max(1, value.length() / 4);
        return value.substring(0, visible) + repeat("*", value.length() - visible);
    }

    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
