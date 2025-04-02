# 🚀 Monglife Common

MongLife 조직의 모듈에서 의존하는 ```Common Library``` 프로젝트입니다. 본 라이브러리는 ```설정 클래스 중복 감소 및 설정 자동화```, ```로그 형식 공통화```, ```인가 비즈니스 로직 공통화```에 활용됩니다. 본 라이브러리를 통해 MongLife 조직의 모든 모듈이 일관된 구조로 개발될 수 있도록 지원합니다.

## 🏗 Project Overview

### - Feign
- Feign Client 를 사용하기 위한 설정을 ```@AutoConfiguration```을 통해 자동으로 설정합니다.

### - Jpa
- ```Domain Layer```모듈 중에서 ```Jpa```를 통해 데이터베이스에 접근하는 모듈에 사용하기 위해 만들어 졌습니다.
- JPA를 위해 공통적으로 사용되는 ```Hibernate Properties```와 같은 설정 ```Bean```을 자동으로 생성합니다.
- ````Entity````에서 사용되는 ```생성 일자```,```수정 일자```와 같은 공통 필드를 정의한 엔티티와 ```공통 코드 엔티티```를 정의했습니다.
- 다수의 ```TransactionManager``` 를 하나의 ```TransactionManager``` 로 만드는 설정을 ```@AutoConfiguration```를 통해 자동으로 설정합니다.

### - Kafka
- Kafka 를 사용하기 위한 설정을 ```@AutoConfiguration```을 통해 자동으로 설정합니다.
- 본 모듈은 마이크로 서비스 간 ```분산 트랜잭션``` 처리와 ```Firebase Notification``` 처리를 위해 사용됩니다.

### - Logging
- Log4j 기반 로깅 시스템을 모듈화 하였습니다. 
- Monglife의 모든 모듈에서의 로그 형식 공통화를 위해 도입되었습니다. 

### - Security
- ```Spring Security```기반 인가 시스템을 모듈화 하였습니다.
- Header 의 Passport 값을 통해 인가에 대한 정보를 확인하고 ```UserDetails```를 생성하여, 이를 통해 인가를 진행합니다.