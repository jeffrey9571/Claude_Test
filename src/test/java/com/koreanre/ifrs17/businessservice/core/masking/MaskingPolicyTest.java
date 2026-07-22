package com.koreanre.ifrs17.businessservice.core.masking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskingPolicyTest {

    private final MaskingPolicy maskingPolicy = new MaskingPolicy(new ObjectMapper());

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class SamplePayload {
        private String accountNumber;
        private String stageName;
    }

    @Test
    void mask_masksSensitiveFieldsButKeepsOtherFieldsIntact() {
        SamplePayload payload = new SamplePayload("110-123-456789", "데이터적재");

        SamplePayload masked = maskingPolicy.mask(payload, MaskingPolicy.DEFAULT_MASKING);

        assertThat(masked.getStageName()).isEqualTo("데이터적재");
        assertThat(masked.getAccountNumber()).isNotEqualTo("110-123-456789");
        assertThat(masked.getAccountNumber()).contains("*");
    }

    @Test
    void mask_returnsUnchanged_whenPolicyIsNone() {
        SamplePayload payload = new SamplePayload("110-123-456789", "데이터적재");

        SamplePayload result = maskingPolicy.mask(payload, MaskingPolicy.NO_MASKING);

        assertThat(result.getAccountNumber()).isEqualTo("110-123-456789");
    }

    @Test
    void mask_returnsNull_whenResultIsNull() {
        SamplePayload result = maskingPolicy.mask((SamplePayload) null, MaskingPolicy.DEFAULT_MASKING);
        assertThat(result).isNull();
    }
}
