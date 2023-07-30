# 주식 실시간 순위 정보 서비스

## 프로젝트 구성
```c
 1.Gradle 기반 멀티모듈 구성
    ㄴcommon(공통코드 모듈), domain(도메인 객체, 레파지토리 포함 모듈), 
    ㄴranking(주식 실시간 순위 정보 서비스 비지니스 로직 모듈)
 2.Spring Boot 기반 WebFlux 사용
 3.BlockHoud 라이브러리(블로킹 메소드 검출 도구)를 사용하여 논블로킹 검증
 4.계층 구조
    ㄴRouter
        ㄴHandler
            ㄴService
 5.계층별 단위테스트
    ㄴio.khw.ranking.rnaking.handler
    ㄴio.khw.ranking.rnaking.service
```

## 비지니스 로직 설계
```c
 1.어플리케이션 구동시 주식 기초정보(SampleData로 제공된) 데이터는 RDB(stock 테이블), redis에 적재
 2.주식 기초정보(SampleData로 제공된)는 RDB(stock 테이블)에 저장
    ㄴ현재는 주식 아이디, 코드값으로 주식정보만 조회하는 테이블 이지만
    ㄴ거래시장 마감시간마다 현재 주가 정보를 업데이트 하는 용도 목적도 추가가 필요함(전날의 변화된 주가 정보는 영구적 저장이 필요하기 때문)
 3.실시간 순위정보, 실시간 주식 가격은 Redis에 적재 수정이 매우 빈번한 데이터 저장소에 적합하다 생각
    ㄴzset을 이용하여 실시간 순위 또한 정렬에 용이
    ㄴhash 단일 조회에 최적화된 데이터 구조
    ㄴhash를 사용하여 실시간 주식정보 저장 (현재가격{실시간으로 변동되는 가격}, 주식 명칭, 주가 상승,하락폭)
        ㄴex) 42:377300   {"currentPrice":"60000","name":"삼성전자","percent":"12.28"}
    ㄴ다만 영구적인 저장소가 아닌점, 거래시장 마감시간마다 새로운 key 구조(날짜 포함등)으로 변경하고, 당일 장의 데이터는 RDB로 이동 적재
     하는 방식을 생각해 볼 수 있다
 4.주식거래 API 
    ㄴ구매 수량, 상승,하락폭 업데이트
    ㄴhash에 저장된 주식정보 업데이트
 5.주식정보 상세조회 API
    ㄴ상세 조회시 주식 인기도 증가
 6.주식 인기도,거래량,상승,하락 API 
    ㄴRedis의 zset 데이터 구조에 각각 저장
 7.테스트 API(주식 거래량 증가, 주식 인기도 증가) 제공
    ㄴ해당 API만 호출하여 실시간 순위 변화 테스트 가능
 
```

## 사용 외부 라이브러리
embedded-redis : 인메모리 h2디비와 같은 인메모리 내장 redis 서버 \
lombok : Java의 라이브러리로 반복되는 메소드를 Annotation을 사용해서 자동으로 작성해주는 라이브러리 \
mapstruct : Java bean 유형 간의 매핑 구현을 단순화하는 코드 생성기 
BlockHound : 비동기 프로그램을 구현하기 위해 블로킹 메소드를 검출을 도와주는 라이브러리

## jar 파일 위치
/ranking-module.jar

## 서버 구동 방법
java -XX:+AllowRedefinitionToAddDeleteMethods -jar /ranking-module --spring.profiles.active=local

## 빌드 방법
WORK_DIR :  stock-flow

jar 파일 빌드 \
./gradlew build


## 빌드 후 서버 구동 방법
빌드된 jar 파일 실행 \
java -XX:+AllowRedefinitionToAddDeleteMethods -jar ./ranking-module/build/libs/ranking-module-0.0.1-SNAPSHOT.jar --spring.profiles.active=local

## API 명세서
### Swagger Ui : http://localhost:8080/swagger-ui/webjars/swagger-ui/index.html


### 주식 거래(구매 수량, 상승,하락폭 업데이트)
POST : localhost:8080/stocks/rankings/{stockId}/{stockCode}
```c
request : {
    "tradeVolume" : 14,
    "buyPrice" : 60000
}

response : {
    "message": "주식 거래 완료",
    "code": "STOCK_TRADE_OK"
}
```

### 실시간 주식 정보 상세조회(상세 조회시 주식 인기도 증가)
GET : localhost:8080/stocks/{stockId}/{stockCode}
```c
response : {
    "message": "종목 거래량 순위 조회 성공",
    "result": {
        "id": 42,
        "code": "377300",
        "name": "카카오페이",
        "price": "60,000원",
        "priceDeltaPercentage": -12.28
    },
    "code": "FIND_ALL_VOLUME_RANK_OK"
}
```

### 주식 거래량 순위
GET : localhost:8080/stocks/rankings/volume?page=1&size=10
```c
response : {
    "message": "종목 거래량 순위 조회 성공",
    "result": [
        {
            "id": 9,
            "code": "5380",
            "name": "현대차",
            "price": "186,000원",
            "priceDeltaPercentage": 0.0
        },
        {
            "id": 99,
            "code": "16360",
            "name": "삼성증권",
            "price": "35,100원",
            "priceDeltaPercentage": 0.0
        }
    ],
    "code": "FIND_ALL_VOLUME_RANK_OK"
}
```

### 주식 인기 순위
GET : localhost:8080/stocks/rankings/volume?page=1&size=10
```c
response : {
    "message": "종목 인기 순위 조회 성공",
    "result": [
        {
            "id": 9,
            "code": "5380",
            "name": "현대차",
            "price": "186,000원",
            "priceDeltaPercentage": 0.0
        },
        {
            "id": 99,
            "code": "16360",
            "name": "삼성증권",
            "price": "35,100원",
            "priceDeltaPercentage": 0.0
        }
    ],
    "code": "FIND_ALL_POPULARITY_RANK_OK"
}
```

### 주식 가격 상승 순위
GET : localhost:8080/stocks/rankings/price-delta?page=1&size=10&orderType=INC
```c
response : {
    "message": "종목 상승,하락가 순위 조회 성공",
    "result": [
        {
            "id": 5,
            "code": "5935",
            "name": "삼성전자우",
            "price": "60,000원",
            "priceDeltaPercentage": 6.38
        },
        {
            "id": 9,
            "code": "5380",
            "name": "현대차",
            "price": "186,000원",
            "priceDeltaPercentage": 0.0
        }
    ],
    "code": "FIND_ALL_PRICE_DELTA_RANK_OK"
}
```

### 주식 가격 상승 순위
GET : localhost:8080/stocks/rankings/price-delta?page=1&size=10&orderType=DEC
```c
response : {
    "message": "종목 상승,하락가 순위 조회 성공",
    "result": [
        {
            "id": 42,
            "code": "377300",
            "name": "카카오페이",
            "price": "60,000원",
            "priceDeltaPercentage": -12.28
        },
        {
            "id": 11,
            "code": "270",
            "name": "기아",
            "price": "75,700원",
            "priceDeltaPercentage": -1.3
        }
    ],
    "code": "FIND_ALL_PRICE_DELTA_RANK_OK"
}
```

### 주식 거래량 증가
GET : localhost:8080/stocks/rankings/volume/increase
```c
request : {
    "stockId" : 42,
    "stockCode" : "377300",
    "tradeVolume" : 555
}

response : {
    "message": "종목 거래량 증가 성공",
    "result": 569.0,
    "code": "INCREASE_VOLUME_RANK_OK"
}
```

### 주식 인기 증가
GET : localhost:8080/stocks/rankings/popularity/increase
```c
request : {
    "stockId" : 42,
    "stockCode" : "377300",
    "tradeVolume" : 555
}

response : {
    "message": "종목 인기 증가 성공",
    "result": 4.0,
    "code": "INCREASE_POPULARITY_RANK_OK"
}
```

