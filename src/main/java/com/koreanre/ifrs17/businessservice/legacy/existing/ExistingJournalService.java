package com.koreanre.ifrs17.businessservice.legacy.existing;

/**
 * 현행 "전표 현황 화면/전표 Service"를 대표하는 계약.
 * 착수 분석 전까지는 Stub 구현으로 동작하며, DB-INC가 실제 기존 Service 호출로 교체한다.
 */
public interface ExistingJournalService {

    /** @return 데이터가 없으면 null. */
    LegacyJournalStatus findJournalStatus(String closingYearMonth, String journalType);
}
