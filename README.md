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
  <!-- Nginx -->
  <img src="https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white" />
</p>

## 🏗️시스템 아키텍처

<img src="https://github.com/user-attachments/assets/b49155d1-4504-4686-bb10-2cbb2ff966a8" width="60%" height="60%" />

## 📃API 문서
🔗 [REST API Documentation](https://wda067.github.io/first-coupon/)

## 📅ERD

<img src="https://github.com/user-attachments/assets/cf557a0a-d7fb-42a0-9bd9-adc1b8272004" width="60%" height="60%" />

- **coupon**: 쿠폰의 기본 정보와 생성 시 정의되는 속성을 저장합니다.
- **issued_coupon**: 사용자에게 발급된 개별 쿠폰의 상태와 정보를 저장합니다.

## 💡기술적 고민

### 1. 쿠폰 발급 동시성 문제
- 문제: 다수의 사용자가 동시에 쿠폰을 신청할 때, 동시성 문제가 발생함.
- 해결: 🔗 [동시성 제어 및 성능 개선 (feat. Redis, Kafka)](https://velog.io/@wda067/%EC%84%A0%EC%B0%A9%EC%88%9C-%EC%BF%A0%ED%8F%B0-%EB%B0%9C%EA%B8%89-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8)

### 2. 독립적인 테스트 환경
- 문제: 개발 환경에서 Redis와 Kafka의 외부 의존성으로 인해 독립적인 단위 테스트가 어려움.
- 해결: 🔗 [Testcontainers로 테스트 환경 구축](https://velog.io/@wda067/Docker-Testcontainers%EB%A1%9C-%ED%85%8C%EC%8A%A4%ED%8A%B8-%ED%99%98%EA%B2%BD-%EA%B5%AC%EC%B6%95%ED%95%98%EA%B8%B0)
