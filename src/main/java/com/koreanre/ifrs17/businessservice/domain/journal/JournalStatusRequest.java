package com.koreanre.ifrs17.businessservice.domain.journal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 9.2 IFRS17.JOURNAL.STATUS 입력: closingYearMonth, journalType(optional). */
@Getter
@Setter
@NoArgsConstructor
public class JournalStatusRequest {
    private String closingYearMonth;
    private String journalType;
}
