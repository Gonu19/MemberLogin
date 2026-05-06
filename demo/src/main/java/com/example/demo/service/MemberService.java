package com.example.demo.service;

import com.example.demo.domain.Member;
import com.example.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.mindrot.jbcrypt.BCrypt;

@Service
@RequiredArgsConstructor

public class MemberService {
    private final MemberRepository memberRepository;
    /*회원가입*/
    public Long signUp(Member member){
        String hashedPassword = BCrypt.hashpw(member.getPassword(), BCrypt.gensalt());
        /*BCcypt로 해싱해서 password를 저장*/
        member.setPassword(hashedPassword);
        memberRepository.save(member);
        /*Repository 계층을 호출해서 저장처리
        *Repository 인터페이스를 바라봄 */
        return member.getId();
    }
}