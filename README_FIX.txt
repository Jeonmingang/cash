# UltimateCashShop (fixed)
- 마크 1.16.5 / 자바 8
- `/캐시 보내기 <플레이어> <수량>` 기능 추가
- 기존 기능 유지: `/캐시`, `/캐시 지급/차감`, `/캐시상점`(열기/등록/취소), Citizens 우클릭 연동(옵션)

## 빌드
```bash
mvn -q -e -DskipTests package
```
생성물: `target/UltimateCashShop-1.4.1.jar`

## 주요 파일
- `balances.yml` : 유저 캐시
- `shop.yml` : 상점 슬롯/아이템/가격
- `links.yml` : Citizens NPC 연결 목록

## 주의
- `config.yml` 내 `messages`에 더치기(...) 같은 YAML 문법 오류가 있으면 서버 부팅시 파싱 에러가 납니다. 이번 버전은 유효한 YAML로 교체했습니다.
