package com.koreanre.ifrs17.businessservice.domain.closing;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 9.1 IFRS17.CLOSING.STATUS 입력: closingYearMonth, closingType(optional). */
@Getter
@Setter
@NoArgsConstructor
public class ClosingStatusRequest {
    private String closingYearMonth;
    private String closingType;
}
