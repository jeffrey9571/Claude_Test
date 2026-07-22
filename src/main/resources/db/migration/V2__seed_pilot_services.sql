-- 부록 B. 서비스 등록 Template 및 9장 파일럿 서비스 상세명세 기준 초기 Catalog 데이터.

INSERT INTO business_service.bs_service
  (service_id, service_name, domain_code, service_type, source_system, service_description, owner_department, active_yn, created_by, updated_by)
VALUES
  ('IFRS17.CLOSING.STATUS', '결산 진행상태 조회', 'CLOSING', 'READ', 'IFRS17', '결산 단계별 상태, 시작/종료시간, 진행률, 오류건수를 조회한다.', '정보기술팀', 'Y', 'SYSTEM', 'SYSTEM'),
  ('IFRS17.JOURNAL.STATUS', '전표 생성·반영 상태 조회', 'JOURNAL', 'READ', 'IFRS17', '전표 유형별 생성건수, 반영건수, 오류건수, 최종처리시간을 조회한다.', '정보기술팀', 'Y', 'SYSTEM', 'SYSTEM'),
  ('IFRS17.EXPENSE.STATUS', '사업비 처리 상태 조회', 'EXPENSE', 'READ', 'IFRS17', '사업비 구분별 적재/검증/배부 상태와 오류를 조회한다.', '정보기술팀', 'Y', 'SYSTEM', 'SYSTEM'),
  ('IFRS17.STATEMENT.STATUS', '재무제표 산출 상태 조회', 'STATEMENT', 'READ', 'IFRS17', '보고서별 산출 상태, 버전, 최종생성시각을 조회한다.', '정보기술팀', 'Y', 'SYSTEM', 'SYSTEM'),
  ('IFRS17.CSM.STATUS', 'CSM 산출 상태 조회', 'CSM', 'READ', 'IFRS17', '포트폴리오별 CSM 산출 상태, 건수, 오류, 기준시각을 조회한다.', '정보기술팀', 'Y', 'SYSTEM', 'SYSTEM');

INSERT INTO business_service.bs_service_version
  (service_id, version, implementation_bean, timeout_ms, status_code)
VALUES
  ('IFRS17.CLOSING.STATUS', '1.0', 'closingStatusBusinessService', 30000, 'ACTIVE'),
  ('IFRS17.JOURNAL.STATUS', '1.0', 'journalStatusBusinessService', 30000, 'ACTIVE'),
  ('IFRS17.EXPENSE.STATUS', '1.0', 'expenseStatusBusinessService', 30000, 'ACTIVE'),
  ('IFRS17.STATEMENT.STATUS', '1.0', 'statementStatusBusinessService', 30000, 'ACTIVE'),
  ('IFRS17.CSM.STATUS', '1.0', 'csmStatusBusinessService', 30000, 'ACTIVE');

INSERT INTO business_service.bs_service_param (service_id, version, param_name, param_type, required_yn, param_description, display_order)
VALUES
  ('IFRS17.CLOSING.STATUS', '1.0', 'closingYearMonth', 'STRING', 'Y', '기준년월 (YYYY-MM)', 1),
  ('IFRS17.CLOSING.STATUS', '1.0', 'closingType', 'STRING', 'N', '결산 구분(선택)', 2),
  ('IFRS17.JOURNAL.STATUS', '1.0', 'closingYearMonth', 'STRING', 'Y', '기준년월 (YYYY-MM)', 1),
  ('IFRS17.JOURNAL.STATUS', '1.0', 'journalType', 'STRING', 'N', '전표 유형(선택)', 2),
  ('IFRS17.EXPENSE.STATUS', '1.0', 'closingYearMonth', 'STRING', 'Y', '기준년월 (YYYY-MM)', 1),
  ('IFRS17.EXPENSE.STATUS', '1.0', 'expenseCategory', 'STRING', 'N', '사업비 구분(선택)', 2),
  ('IFRS17.STATEMENT.STATUS', '1.0', 'closingYearMonth', 'STRING', 'Y', '기준년월 (YYYY-MM)', 1),
  ('IFRS17.STATEMENT.STATUS', '1.0', 'statementType', 'STRING', 'N', '보고서 유형(선택)', 2),
  ('IFRS17.CSM.STATUS', '1.0', 'closingYearMonth', 'STRING', 'Y', '기준년월 (YYYY-MM)', 1),
  ('IFRS17.CSM.STATUS', '1.0', 'portfolioCode', 'STRING', 'N', '포트폴리오 코드(선택)', 2);

INSERT INTO business_service.bs_service_role (service_id, role_code)
VALUES
  ('IFRS17.CLOSING.STATUS', 'IFRS17_CLOSING_VIEW'),
  ('IFRS17.JOURNAL.STATUS', 'IFRS17_JOURNAL_VIEW'),
  ('IFRS17.EXPENSE.STATUS', 'IFRS17_EXPENSE_VIEW'),
  ('IFRS17.STATEMENT.STATUS', 'IFRS17_STATEMENT_VIEW'),
  ('IFRS17.CSM.STATUS', 'IFRS17_CSM_VIEW');

-- 개발/테스트용 기본 Client 및 전체 파일럿 서비스 허용목록.
INSERT INTO business_service.bs_client (client_id, client_name, channel_type, active_yn, created_by)
VALUES ('MCP-IFRS17-01', 'IFRS17 MCP 연계 채널(개발용)', 'MCP', 'Y', 'SYSTEM');

INSERT INTO business_service.bs_client_service (client_id, service_id, active_yn)
SELECT 'MCP-IFRS17-01', service_id, 'Y' FROM business_service.bs_service;
