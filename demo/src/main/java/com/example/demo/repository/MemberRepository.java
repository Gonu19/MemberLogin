package com.example.demo.repository;

import com.example.demo.domain.Member;

import java.util.List;

public interface MemberRepository {
    void save(Member member);
    Member findById(Long id);
    List<Member> findAll();
    void remove(Long id);
    Member findByUserId(String userId);
}

/*Service는 추상적인 인터페이스 MemberRepository를 바라봄
* 데이터 베이스는 External Dependency(외부 의존성)이기 떄문에 상황에 따라 교체될 수 있음
* 때문에 비즈니스 로직(Service)이 외부 기술의 변화에 흔들리지 않도록 인터페이스를 두어서 보호
* */

/*모든 멤버 조회 기능
* 삭제 기능
* userId를 통해 검색하는 기능
* 3가지를 추가 명세함*/