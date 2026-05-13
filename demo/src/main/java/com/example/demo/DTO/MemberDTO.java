package com.example.demo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
상황별로 DTO를 나누어서 정의*/
public class MemberDTO {
    private MemberDTO(){

    }

    public static class Request{
        @Data
        @NoArgsConstructor
        public static class Create{
            private String userId;
            private String password;
            private String username;
        }
        // POST 회원가입

        @Data
        @NoArgsConstructor
        public static class Update{
            private String username;
            private String password;
        }
        //PUT 정보수정

        @Data
        @NoArgsConstructor
        public static class Login {
            private String userId;
            private String password;
        }
        //POST 로그인 인증
    }

    public static  class Response {
        @Data
        @AllArgsConstructor
        public static class Member {
            private Long id;
            private String userId;
            private String username;
        }
     /*GET 정보조회
     응답 전용 DTO*/
    }


    @Data
    @AllArgsConstructor
    public static class Result<T>{
        private T data;
    }
    //공통 응답 규격, 모든 API 응답이 일관된 형식을 가지게 해줌

}
