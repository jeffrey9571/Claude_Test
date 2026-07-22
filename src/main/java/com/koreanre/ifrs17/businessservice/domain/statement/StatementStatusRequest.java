package com.koreanre.ifrs17.businessservice.domain.statement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 9.4 IFRS17.STATEMENT.STATUS 입력: closingYearMonth, statementType(optional). */
@Getter
@Setter
@NoArgsConstructor
public class StatementStatusRequest {
    private String closingYearMonth;
    private String statementType;
}
