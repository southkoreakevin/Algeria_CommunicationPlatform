Framework: 4.0.5
JAVA: 21
Dependencies: Spring Web, Lombok,
Spring Data JPA, H2 Database


@PathVariable의 기본 동작 원리는 URL 경로에 있는 변수명({...})과 자바 파라미터 변수명이 일치하는 것을 찾는 것입니다.

* @ModelAttribute name 생략 가능
* model.addAttribute(item); 자동 추가, 생략 가능
* 생략시 model에 저장되는 name은 클래스명 첫글자만 소문자로 등록 Item -> item

api형식에는 @ResponseBody쓴다.

DTO 에는
Lombok 없이 순수 자바로만 만든다면, 보통 다음 5가지가 세트로 들어갑니다.

Getter: 필드 값을 읽기 위해 필요합니다.

Setter: 필드 값을 수정하기 위해 필요합니다. (단, DTO에서는 데이터 불변성을 위해 생략하기도 합니다.)

기본 생성자 (No-Args Constructor): J
SON 라이브러리(Jackson)나 JPA가 객체를 생성할 때 꼭 필요합니다.

전체 생성자 (All-Args Constructor): 객체를 한 번에 초기화할 때 편합니다.

toString(), equals(), hashCode(): 디버깅할 때 객체 내용을 확인하거나, 객체끼리 비교할 때 필수입니다.