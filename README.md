# Mong Life 공통 모듈

## Summary
```
Mong Life 하위 서비스를 개발할 때 사용하는 공통 기능들을 자동 설정할 수 있는 공통 모듈입니다.
```

## Modules

### 1. Feign Module
- Feign Client 를 사용하기 위한 Configuration 을 모듈화 하였습니다.

### 2. Jpa Module
- 모든 서비스에서 사용되는 공통 엔티티를 정의했습니다.
- Hibernate Properties 를 생성합니다.
- 여러 TransactionManager 를 하나의 TransactionManager 로 만드는 Configuration 을 포함했습니다.

### 3. Kafka Module
- Kafka 를 사용하기 위한 Configuration 을 모듈화 하였습니다.
- 분산 트랜잭션 처리를 위해 사용됩니다.

### 4. Logging Module
- Log4j 기반 로깅 시스템을 모듈화 하였습니다. 
- 모든 서비스에서 로깅 공통화를 위해 도입되었습니다. 

### 5. Security Module
- Spring Security 기반 인가 시스템을 모듈화 하였습니다.
- Header 의 Passport 값을 추출하여 인가에 대한 정보를 확인하고 인가를 진행합니다.