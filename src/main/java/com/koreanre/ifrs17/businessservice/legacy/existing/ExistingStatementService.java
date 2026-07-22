package com.koreanre.ifrs17.businessservice.legacy.existing;

/**
 * 현행 "재무제표/리포트 Service"를 대표하는 계약.
 * 착수 분석 전까지는 Stub 구현으로 동작하며, DB-INC가 실제 기존 Service 호출로 교체한다.
 */
public interface ExistingStatementService {

    /** @return 데이터가 없으면 null. */
    LegacyStatementStatus findStatementStatus(String closingYearMonth, String statementType);
}
