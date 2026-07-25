-- =====================================================================
-- CON-01(서비스 명세 관리)을 설계서 8.3/그림 8-1에 맞춰 정렬.
-- bs_service_version의 request_schema/response_schema(요청·응답 JSON 명세)를
-- 파일럿 5종에 대해 채운다. (컬럼은 V1에서 이미 생성됨)
-- =====================================================================

UPDATE business_service.bs_service_version
   SET request_schema  = '{ closingYearMonth, closingType }',
       response_schema = '{ closingYearMonth, closingType, overallStatus, stages: [ { stageName, status, startTime, endTime, progressRate, errorCount } ], totalErrorCount }'
 WHERE service_id = 'IFRS17.CLOSING.STATUS' AND version = '1.0';

UPDATE business_service.bs_service_version
   SET request_schema  = '{ closingYearMonth, journalType }',
       response_schema = '{ closingYearMonth, journalType, overallStatus, journalTypes: [ { journalType, status, createdCount, postedCount, errorCount, lastProcessedAt } ], totalErrorCount }'
 WHERE service_id = 'IFRS17.JOURNAL.STATUS' AND version = '1.0';

UPDATE business_service.bs_service_version
   SET request_schema  = '{ closingYearMonth, expenseCategory }',
       response_schema = '{ closingYearMonth, expenseCategory, overallStatus, categories: [ { expenseCategory, loadStatus, validationStatus, allocationStatus, errorCount } ], totalErrorCount }'
 WHERE service_id = 'IFRS17.EXPENSE.STATUS' AND version = '1.0';

UPDATE business_service.bs_service_version
   SET request_schema  = '{ closingYearMonth, statementType }',
       response_schema = '{ closingYearMonth, statementType, overallStatus, statements: [ { statementType, status, version, lastGeneratedAt } ] }'
 WHERE service_id = 'IFRS17.STATEMENT.STATUS' AND version = '1.0';

UPDATE business_service.bs_service_version
   SET request_schema  = '{ closingYearMonth, portfolioCode }',
       response_schema = '{ closingYearMonth, portfolioCode, overallStatus, portfolios: [ { portfolioCode, status, calculatedCount, errorCount, asOfTime } ], totalErrorCount }'
 WHERE service_id = 'IFRS17.CSM.STATUS' AND version = '1.0';
