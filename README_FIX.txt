[UltimateCashShop] 버킷 오류(=Bukkit 에러) 수정 내역

1) 원인
 - config.yml 의 messages 블록에 'sent', 'incoming' 키가 최상단에 노출되어 들여쓰기가 깨져 있었습니다.
 - 그 결과 YAML 파싱 에러(InvalidConfigurationException)가 발생했고, 캐시 GUI/명령어 처리 시 버킷 오류로 이어졌습니다.
   예) while parsing a block mapping ... expected <block end> ...

2) 수정
 - config.yml 구조를 올바른 YAML 로 정리하고 messages 하위에 송금 관련 메시지 키들을 배치했습니다.
 - /캐시 보내기 로직을 보완하여 잘못된 금액, 자기 자신 송금, 대상 미접속(미가입) 등 예외를 안전하게 처리합니다.

3) 추가 개선
 - plugin.yml 의 명령어 사용법에 /캐시 보내기 를 명시.
 - CashCommand.java: 보내기/지급/차감 처리부에 숫자 파싱/권한/잔액 검증을 강화.

4) 배포 방법
 - /plugins/UltimateCashShop/config.yml 를 본 소스의 것으로 교체 후 서버 재시작 또는 /reload.
 - 플러그인 중복 주의: plugins 폴더에 UltimateCashShop-*.jar 가 1개만 존재하도록 정리하세요.

Java 8 / Minecraft 1.16.5 (api-version 1.16) 기준.
