package com.koreanre.ifrs17.businessservice.core.validator;

import com.koreanre.ifrs17.businessservice.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationUtilsTest {

    @Test
    void requireYearMonth_acceptsValidFormat() {
        ValidationUtils.requireYearMonth("2026-06");
    }

    @Test
    void requireYearMonth_rejectsNull() {
        assertThatThrownBy(() -> ValidationUtils.requireYearMonth(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void requireYearMonth_rejectsBlank() {
        assertThatThrownBy(() -> ValidationUtils.requireYearMonth("  "))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void requireYearMonth_rejectsWrongFormat() {
        assertThatThrownBy(() -> ValidationUtils.requireYearMonth("2026/06"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ValidationUtils.requireYearMonth("202606"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ValidationUtils.requireYearMonth("2026-13"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void requireText_rejectsBlank() {
        assertThatThrownBy(() -> ValidationUtils.requireText(" ", "field"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("field");
        assertThat(ValidationUtils.class).isNotNull();
    }
}
