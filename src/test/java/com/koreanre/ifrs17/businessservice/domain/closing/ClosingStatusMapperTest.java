package com.koreanre.ifrs17.businessservice.domain.closing;

import com.koreanre.ifrs17.businessservice.domain.common.StandardStatusCode;
import com.koreanre.ifrs17.businessservice.legacy.existing.LegacyClosingStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ClosingStatusMapperTest {

    @Test
    void toResponse_returnsEmptySuccess_whenLegacyIsNull() {
        ClosingStatusResponse response = ClosingStatusMapper.toResponse("2026-06", null, null);

        assertThat(response.getOverallStatus()).isEqualTo(StandardStatusCode.NOT_STARTED);
        assertThat(response.getStages()).isEmpty();
        assertThat(response.getTotalErrorCount()).isZero();
    }

    @Test
    void toResponse_resolvesCompleted_whenAllStagesCompleted() {
        LegacyClosingStatus legacy = LegacyClosingStatus.builder()
                .closingYearMonth("2026-05")
                .closingType(null)
                .stages(Collections.singletonList(
                        LegacyClosingStatus.LegacyStage.builder()
                                .stageName("데이터적재")
                                .statusCode("COMPLETED")
                                .startTime(LocalDateTime.now())
                                .endTime(LocalDateTime.now())
                                .progressRate(100)
                                .errorCount(0)
                                .build()))
                .build();

        ClosingStatusResponse response = ClosingStatusMapper.toResponse("2026-05", null, legacy);

        assertThat(response.getOverallStatus()).isEqualTo(StandardStatusCode.COMPLETED);
        assertThat(response.getStages()).hasSize(1);
        assertThat(response.getTotalErrorCount()).isZero();
    }

    @Test
    void toResponse_resolvesFailed_whenAnyStageFailedAndNotAllCompleted() {
        LegacyClosingStatus legacy = LegacyClosingStatus.builder()
                .closingYearMonth("2026-05")
                .stages(java.util.Arrays.asList(
                        LegacyClosingStatus.LegacyStage.builder()
                                .stageName("데이터적재").statusCode("COMPLETED").progressRate(100).errorCount(0).build(),
                        LegacyClosingStatus.LegacyStage.builder()
                                .stageName("보험부채평가").statusCode("FAILED").progressRate(30).errorCount(2).build()))
                .build();

        ClosingStatusResponse response = ClosingStatusMapper.toResponse("2026-05", null, legacy);

        assertThat(response.getOverallStatus()).isEqualTo(StandardStatusCode.FAILED);
        assertThat(response.getTotalErrorCount()).isEqualTo(2);
    }

    @Test
    void toResponse_mapsUnknownStatusCode_toUnknown() {
        LegacyClosingStatus legacy = LegacyClosingStatus.builder()
                .closingYearMonth("2026-05")
                .stages(Collections.singletonList(
                        LegacyClosingStatus.LegacyStage.builder()
                                .stageName("데이터적재").statusCode("WEIRD_CODE").progressRate(0).errorCount(0).build()))
                .build();

        ClosingStatusResponse response = ClosingStatusMapper.toResponse("2026-05", null, legacy);

        assertThat(response.getStages().get(0).getStatus()).isEqualTo(StandardStatusCode.UNKNOWN);
    }
}
