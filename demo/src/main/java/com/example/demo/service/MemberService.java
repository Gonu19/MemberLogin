package com.example.demo.service;

import com.example.demo.domain.Member;
import com.example.demo.repository.MemberRepository;
import com.example.demo.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

@Service
@RequiredArgsConstructor

public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil; //추가
    public Member tokenToMember(String token){
        return memberRepository.findByUserId(jwtUtil.getClaimsFromJwt(token).getSubject());
    }
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

    public List<Member> findAll(){
        return memberRepository.findAll();
    }
    /*Repository의 findAll() 메소드 호출*/

    public Member findById(Long id){ return memberRepository.findById(id); }
    /*Repository의 findById() 메소드 호출, 매개변수는 id*/

    public void update(Long id, String newName, String newPassword){
        Member member = memberRepository.findById(id);
        /*id를 통해 Member 객체를 호출*/

        member.setUsername(newName);
        /*userName 변경*/
        if(newPassword != null && !newPassword.isEmpty()){
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            member.setPassword(hashedPassword);
        }
        /*이건 새 password가 null이 아니고 비지 않았을 때 다시 해싱 후 password를 설정*/

        memberRepository.save(member);
    }
    /*update된 member 객체를 저장*/

    public void delete(Long id){memberRepository.remove(id);}
    /*memberRepository의 remove()메소드 호출*/

    public String login(String userId, String password){
        Member member = memberRepository.findByUserId(userId);
        if(member != null && BCrypt.checkpw(password, member.getPassword())){
            String token = jwtUtil.generateJwt(member.getUserId(), member.getUsername());
            return token;
        }
        return "아이디와 비밀번호를 확인하세요";
    }
    /*id를 불러와서 password가 맞는지 비교
    * 맞다면 토큰 부여*/

    public Member findByUserId(String userId){return memberRepository.findByUserId(userId); }
    /*memberRepository의 findByUserId(userId) 메서드 호출*/
}