package com.example.demo.controller;

import com.example.demo.DTO.MemberDTO;
import com.example.demo.domain.Member;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*외부  요청이 앱 내부로 들어오는 진입접 역할*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/members")
    public MemberDTO.Result<Long> saveMember(@RequestBody MemberDTO.Request.Create request){
        //  MemberDTO.Request.Create 객체로 Data Binding
        // 화면상에 보여지는 데이터와 메모리에 있는 데이터를 묶어서 서로 간의 데이터를 동기화
        Member member = new Member();
        member.setUserId(request.getUserId());
        member.setPassword(request.getPassword());
        member.setUsername(request.getUsername());

        Long id = memberService.signUp(member);
        /*Service 계층으로 실제 처리를 위임*/
        return new MemberDTO.Result<>(id);
        /*결과물을 Wrapper class로 감싸서 변환
        * 항상 일관된 데이터를 반환해줌*/
    }
}
