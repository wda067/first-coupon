# 🏃선착순 쿠폰 발급 시스템

## 📌프로젝트 소개

본 프로젝트는 선착순으로 쿠폰을 발급받고 사용할 수 있는 시스템입니다.

## 📌주요 기능

- **쿠폰 생성 및 관리**
    - 관리자는 쿠폰을 생성하고, 발급 수량을 설정할 수 있습니다.
    - 쿠폰에는 유효기간과 발급 가능 시간이 설정될 수 있습니다.
- **쿠폰 발급**
    - 특정 시간에 사용자가 쿠폰을 신청할 수 있습니다.
    - 선착순으로 정해진 개수만큼의 쿠폰이 발급됩니다.
    - 중복으로 쿠폰을 발급받을 수 없습니다.
- **쿠폰 사용**
    - 사용자는 유효기간 내에 발급받은 쿠폰을 사용할 수 있습니다.
    - 한 번 사용된 쿠폰은 다시 사용할 수 없습니다.
- **알림**
  - 쿠폰 유효기간 7일 전 쿠폰 만료 임박 알림이 전송됩니다.
  - 쿠폰 사용 후 사용 완료 알림이 전송됩니다.

## 💻기술 스택

<p>
  <!-- Java -->
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <!-- Spring -->
  <img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
  <!-- MySQL -->
  <img src="https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white" />
  <!-- Redis -->
  <img src="https://img.shields.io/badge/redis-%23DD0031.svg?&style=for-the-badge&logo=redis&logoColor=white" />
  <!-- Apache Kafka -->
  <img src="https://img.shields.io/badge/kafka-232F3E?style=for-the-badge&logo=apache-kafka&logoColor=white" />
  <!-- Docker -->
  <img src="https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white" />
</p>

## 🏗️시스템 아키텍처
![Image](https://github.com/user-attachments/assets/42416148-0def-4d55-9f21-b501eeeba2ff)

## 📃API 문서
🔗 [REST API Documentation](https://wda067.github.io/first-coupon/)

## 📅ERD
![Image](https://github.com/user-attachments/assets/cf557a0a-d7fb-42a0-9bd9-adc1b8272004)

- **coupon**: 쿠폰의 기본 정보와 생성 시 정의되는 속성을 저장합니다.
- **issued_coupon**: 사용자에게 발급된 개별 쿠폰의 상태와 정보를 저장합니다.

## 〰️쿠폰 발급 및 관리 Flow
![Image](https://github.com/user-attachments/assets/e5c3cad0-2672-498c-921e-628eca8bdf48)

## 〰️쿠폰 사용 Flow
![Image](https://github.com/user-attachments/assets/d4926111-1eff-43d9-b509-61f151caec14)

## 🔫트러블슈팅

### 1. 쿠폰 초과 발급
- 문제: 다수의 사용자가 동시에 쿠폰을 신청할 때, 초과 발급이 발생.
- 해결: 
  - Redis Lua 스크립트를 사용해 중복 체크와 재고 감소를 원자적으로 처리.
  - 발급 가능 여부 확인 후 Kafka로 이벤트를 발행해 DB 저장을 비동기 처리.
  - Kafka 컨슈머 그룹을 활용해 대규모 이벤트(발급 내역 저장)를 병렬로 처리하며 DB 부하 분산.
  - 초당 1,000건 요청에서도 초과 발급 없음.

### 2. 독립적인 테스트 환경
- 문제: 개발 환경에서 Redis와 Kafka의 외부 의존성으로 인해 독립적인 단위 테스트가 어려움.
- 해결:
  - Testcontainers를 활용해 Redis와 Kafka를 독립적으로 동작시키는 통합 테스트 환경 구축.
  - Redis 컨테이너로 재고 관리 및 중복 체크 로직 검증.
  - Kafka 컨테이너로 이벤트 발행 및 소비 프로세스 테스트.
