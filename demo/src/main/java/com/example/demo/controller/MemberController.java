package com.example.demo.controller;

import com.example.demo.DTO.MemberDTO;
import com.example.demo.domain.Member;
import com.example.demo.service.MemberService;
import com.example.demo.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/*외부  요청이 앱 내부로 들어오는 진입접 역할*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {
    private final MemberService memberService;
    private final JwtUtil jwtUtil;

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
    @PostMapping("/login")
    public MemberDTO.Result<String> login(@RequestBody MemberDTO.Request.Login request){
        String token = memberService.login(request.getUserId(), request.getPassword());
        return new MemberDTO.Result<>(token);
    }

    @GetMapping("/members")
    public MemberDTO.Result<List<MemberDTO.Response.Member>> findAllMembers(){
        /*도메인 리스트 객체 가져오기*/
        List<Member> findMembers = memberService.findAll();
        List<MemberDTO.Response.Member> collect = findMembers.stream()
                .map(m -> new MemberDTO.Response.Member(m.getId(), m.getUserId(), m.getUsername()))
                .collect(Collectors.toList());
        /*Entity를 각각 DTO로 변환
        * list에 DTO들을 저장*/
        return new MemberDTO.Result<>(collect);
    }
    /*List에 담긴 DTO 객체들을 리턴*/

    @PutMapping("/members")
    public MemberDTO.Result<?> updateMember(
            @RequestBody MemberDTO.Request.Update request,
            /*RequestBody에 담긴 데이터를 객체로 변환*/
            @RequestHeader("Authorization") String token){
            /*Header에 담긴 데이터를 추출*/
        if(!jwtUtil.validateJwt(token)){
            return new MemberDTO.Result<>("유효한 토큰이 아닙니다");
        }
        /*토큰이 맞는지 확인*/

        Long id = memberService.tokenToMember(token).getId();
        /*payload에서 id를 추출*/
        memberService.update(id, request.getUsername(), request.getPassword());
        /*memberService.update() 호출*/
        Member findmember = memberService.findById(id);
        /*service의 findById()를 호출해서 key값인 id를 통해 객체 반환(service는 repository를 호출)*/
        return new MemberDTO.Result<>(
                new MemberDTO.Response.Member(findmember.getId(), findmember.getUserId(), findmember.getUsername())
        );
        /*responseDTO에 명시된 대로 DTO를 변환하여 최종 반환*/
    }


    @DeleteMapping("/members")
    public MemberDTO.Result<?> deleteMember(@RequestHeader("Authorization") String token){
        if(!jwtUtil.validateJwt(token)){
            return new MemberDTO.Result<>("유효한 토큰이 아닙니다.");
        }
        /*토큰값이 맞는지 확인*/

        Long id = memberService.tokenToMember(token).getId();
        memberService.delete(id);
        /*id 삭제*/

        return new MemberDTO.Result<>("회원삭제 완료");
        /*평문으로 작성시 에러
        * Result 객체로 감싸서 반환함으로 JSON 객체를 클라이언트가 받음*/
    }

    @GetMapping("/members/{id}")
    public MemberDTO.Result<List<MemberDTO.Response.Member>> findMember(@PathVariable Long id){
        List<Member> findMember = Collections.singletonList(memberService.findById(id));
        /*단일 Member 객체를 담은 리스트를 가져옴*/
        List<MemberDTO.Response.Member> collect = Collections.singletonList(findMember.stream()
                .map(m -> new MemberDTO.Response.Member(m.getId(), m.getUserId(), m.getUsername()))
                .findAny()
                .orElse(null));
        /*찾은 객체를 map으로 데이터 변환하고 findAny와 orElse로 단일 DTO로 뽑아냄*/
        /*list로 감싸서 반환*/
        return new MemberDTO.Result<>(collect);
    }

   /*
   Gemini에게 피드백 받고 수정한 코드
   단일 객체를 다루기 때문에 굳이 Stream을 사용하는건 비효율적이다.
   불필요한 list 생성과 Stream 변환 과정이 들어가기 때문.
   단일 객체를 Direct Mapping하는게 직관적이고 성능 면에서 유리.

   @GetMapping("/members/{id}")
    public MemberDTO.Result<MemberDTO.Response.Member> findMember(@PathVariable Long id){
        *//*도메인 리스트 객체 가져오기/*
        Member findMember = memberService.findById(id);
        MemberDTO.Response.Member findMemberDTO = new MemberDTO.Response.Member(findMember.getId(), findMember.getUserId(), findMember.getUsername());
        //직접 매핑
        return new MemberDTO.Result<>(findMemberDTO);
    }*/
}
