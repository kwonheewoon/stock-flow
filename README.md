# 블로그(카카오, 네이버) 검색 서비스 : 권희운

### WebFlux 기반 카카오, 네이버 블로그 검색 API
```c
 1. BlockHound를 사용하여 동기적 호출이 발생되지 않게 개발
 2. application-common.yml api.blog.main 의 값에 따른 KakaoBlogSearchService, 
    NaverBlogSearchService 구현체 의존성 전략 주입
 3. main API의 서버 오류등 호출이 불가 할 겨우 지정된 횟수만큼 재시도
 4. 재시도 실패시 대체 API(네이버 블로그 검색 API) 재호출
 5. 호출 성공시 query=검색어 의 검색횟수 증가 메소드 호출
```

## 사용 외부 라이브러리
lombok : Java의 라이브러리로 반복되는 메소드를 Annotation을 사용해서 자동으로 작성해주는 라이브러리 \
mapstruct : Java bean 유형 간의 매핑 구현을 단순화하는 코드 생성기 
BlockHound : 비동기 프로그램을 구현하기 위해 블로킹 메소드를 검출을 도와주는 라이브러리

## jar 파일 위치
/search-module.jar

## 서버 구동 방법
java -jar /search-module.jar --spring.profiles.active=dev

## 빌드 방법
WORK_DIR :  search-module

jar 파일 빌드 \
./gradlew build


## 빌드 후 서버 구동 방법
빌드된 jar 파일 실행 \
java -jar ./build/libs/search-module-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

## 모듈 구조(Gradle 기반 멀티모듈 환경)
```c 
ㄴsearch-module : 비지니스 로직 포함 모듈
ㄴㄴdomain-module : Entity, Dto, Vo, Reposotiry 포함 모듈
ㄴㄴㄴcommon-module : 공통으로 사용 가능한 Config, Enum, Exception, ControllerAdvice 혹은 Util성 객체 포함 모듈
```

## API 명세서
### Swagger Ui : http://localhost:8080/swagger-ui/


GET : localhost:8080/search/blog?query=검색어&sort=recency&page=1&size=10
```c
response : {
    "documents": [
        {
            "title": "충격!ChatGPT가 밝히는 구글 SEO전략의 실체!",
            "thumbnail": "",
            "blogName": "알쓸잡",
            "contents": "도와주는 방법입니다. 이를 위해서는 다음과 같은 전략을 사용할 수 있습니다. 1.키워드 연구: 사용자가 검색할 때 입력하는 단어나 구를 찾고, 그에 대한 <b>검색어</b>를 선정하여 적극적으로 활용합니다. 2.내부 링크 구축: 웹사이트 내의 페이지들 간에 링크를 구성하여 검색 엔진이 쉽게 페이지를 찾을 수 있도록 돕습니다...",
            "url": "http://wjdals10.tistory.com/2",
            "datetime": "2023-03-22T19:02:23"
        }
    ],
    "totalCount": 1219700
}
```

### 인기검색어 상위 10개 조회 API
GET : localhost:8080/search/blog/top-keywords
```c
response : {
    "documents": [
        {
            "keyword": "인천",
            "searchVolume": 11
        },
        {
            "keyword": "경기도",
            "searchVolume": 7
        },
        {
            "keyword": "판교",
            "searchVolume": 5
        }
    ],
    "totalCount": 3
} 
```

