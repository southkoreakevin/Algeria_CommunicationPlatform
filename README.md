Framework: 4.0.5
JAVA: 21
Dependencies: Spring Web, Lombok,
Spring Data JPA, H2 Database


@PathVariable의 기본 동작 원리는 URL 경로에 있는 변수명({...})과 자바 파라미터 변수명이 일치하는 것을 찾는 것입니다.

* @ModelAttribute name 생략 가능
* model.addAttribute(item); 자동 추가, 생략 가능
* 생략시 model에 저장되는 name은 클래스명 첫글자만 소문자로 등록 Item -> item

api형식에는 @ResponseBody쓴다.