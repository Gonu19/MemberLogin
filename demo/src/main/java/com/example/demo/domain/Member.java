package com.example.demo.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Member {
    private Long id;
    private String userId; //로그인 id
    private String username;
    private String password;

    public Member(String userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    } //db 내부키인 id를 제외한 나머지 속성만 초기화
}

