package com.koreanre.ifrs17.businessservice.core.context;

import com.koreanre.ifrs17.businessservice.core.exception.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 4.2 RequestContextResolver: SSO/Client/Request ID/Trace 정보 생성.
 *
 * <p>미결사항 #2(현행 SSO Token/Session에서 취득 가능한 사용자 속성)가 확정되기 전까지는
 * 신뢰할 수 있는 Gateway/SSO Filter가 주입한 Header(X-User-ID, X-User-Roles,
 * X-Department-Code)를 사용자 Context 소스로 사용한다. 운영 반영 전 실제 SSO
 * Token 파싱 모듈로 교체해야 한다.</p>
 */
@Component
public class RequestContextResolver {

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_REQUEST_ID = "X-Request-ID";
    public static final String HEADER_CLIENT_ID = "X-Client-ID";
    public static final String HEADER_USER_ID = "X-User-ID";
    public static final String HEADER_TRACE_ID = "X-Trace-ID";
    public static final String HEADER_DEPARTMENT_CODE = "X-Department-Code";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_PARENT_REQUEST_ID = "X-Parent-Request-ID";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicLong sequence = new AtomicLong(0);

    public ServiceContext resolve(HttpServletRequest httpRequest, String serviceId) {
        String authorization = httpRequest.getHeader(HEADER_AUTHORIZATION);
        if (!StringUtils.hasText(authorization)) {
            throw new AuthenticationException("Authorization Header가 없습니다.");
        }

        String clientId = httpRequest.getHeader(HEADER_CLIENT_ID);
        if (!StringUtils.hasText(clientId)) {
            throw new AuthenticationException("X-Client-ID Header가 없습니다.");
        }

        String userId = httpRequest.getHeader(HEADER_USER_ID);
        if (!StringUtils.hasText(userId)) {
            throw new AuthenticationException("SSO 사용자 정보를 확인할 수 없습니다.");
        }

        String requestId = httpRequest.getHeader(HEADER_REQUEST_ID);
        if (!StringUtils.hasText(requestId)) {
            requestId = generateRequestId();
        }

        String traceId = httpRequest.getHeader(HEADER_TRACE_ID);
        if (!StringUtils.hasText(traceId)) {
            traceId = "TRACE-" + java.util.UUID.randomUUID();
        }

        return ServiceContext.builder()
                .requestId(requestId)
                .traceId(traceId)
                .parentRequestId(httpRequest.getHeader(HEADER_PARENT_REQUEST_ID))
                .clientId(clientId)
                .userId(userId)
                .departmentCode(httpRequest.getHeader(HEADER_DEPARTMENT_CODE))
                .roles(parseRoles(httpRequest.getHeader(HEADER_USER_ROLES)))
                .requestedAt(LocalDateTime.now())
                .remoteIp(resolveRemoteIp(httpRequest))
                .serviceId(serviceId)
                .serverInstance(resolveServerInstance())
                .build();
    }

    private Set<String> parseRoles(String rolesHeader) {
        if (!StringUtils.hasText(rolesHeader)) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String resolveRemoteIp(HttpServletRequest httpRequest) {
        String forwardedFor = httpRequest.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
    }

    private String resolveServerInstance() {
        String host = System.getenv("HOSTNAME");
        return StringUtils.hasText(host) ? host : "local";
    }

    private synchronized String generateRequestId() {
        String datePart = LocalDateTime.now().format(DATE_FMT);
        long seq = sequence.incrementAndGet();
        return String.format("REQ-%s-%04d", datePart, seq % 10000);
    }
}
