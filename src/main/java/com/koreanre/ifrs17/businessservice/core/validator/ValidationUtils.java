package com.koreanre.ifrs17.businessservice.core.validator;

import com.koreanre.ifrs17.businessservice.core.exception.ValidationException;
import org.springframework.util.StringUtils;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * 10.2 Reference Implementation에서 사용하는 공통 입력 검증 유틸리티.
 * 파일럿 5개 서비스가 공통으로 요구하는 "기준년월은 YYYY-MM 형식" 검증(9.1~9.5)을 담당한다.
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static void requireYearMonth(String value) {
        if (!StringUtils.hasText(value)) {
            throw new ValidationException("closingYearMonth는 필수입니다.");
        }
        try {
            YearMonth.parse(value);
        } catch (DateTimeParseException e) {
            throw new ValidationException("closingYearMonth는 YYYY-MM 형식이어야 합니다: " + value);
        }
    }

    public static void requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ValidationException(fieldName + "는 필수입니다.");
        }
    }
}
