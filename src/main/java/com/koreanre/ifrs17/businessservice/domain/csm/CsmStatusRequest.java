package com.koreanre.ifrs17.businessservice.domain.csm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 9.5 IFRS17.CSM.STATUS 입력: closingYearMonth, portfolioCode(optional). */
@Getter
@Setter
@NoArgsConstructor
public class CsmStatusRequest {
    private String closingYearMonth;
    private String portfolioCode;
}
