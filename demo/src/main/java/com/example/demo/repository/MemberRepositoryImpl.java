package com.example.demo.repository;

import com.example.demo.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Repository /*Repository 계층으로 등록, 스프링 빈임*/

public class MemberRepositoryImpl implements MemberRepository {
    private final ObjectMapper objectMapper = new ObjectMapper();
    /* JSON 파일 읽기/쓰기용 Jackson 객체*/

    private final String DATA_FILE_PATH = "data/members.json";
    /*members.json(회원 정보)를 저장할 파일 위치*/

    private final Map<Long,Member> store = new ConcurrentHashMap<>();
    /* 메모리 저장소
    * store은 메모리 저장소, 서버가 켜져있는 동안 데이터를 임시로 저장
    * HashMap은 여러 스레드가 동시에 접근하면 데이터가 꼬이거나 유실,
    * ConcurrentHashMap은 Locking Mechanism(잠금 메커니즘)을 사용하여 데이터의 일관성 보장*/

    private final AtomicLong sequence = new AtomicLong(0L);
    /*내부 키인 ID 자동 증가*/

    public MemberRepositoryImpl() {
        loadDataFromFile();
    }

    /*파일 목록을 메모리로 불러오기*/
    private void loadDataFromFile() {
        File file = new File(DATA_FILE_PATH);
        if (!file.exists()) {
            List<Member> members = objectMapper.readValue(file, new TypeReference<List<Member>>() {
            });
            for (Member member : members) {
                store.put(member.getId(), member);

                if(member.getId() > sequence.get()) {
                    sequence.set(member.getId() + 1);
                }
            }
            log.info("회원 데이터 로드 완료: {}명", members.size());
        }
        else{
            File directory = new File("data");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            log.info("기존 데이터 파일이 없어 새로 시작합니다");
        }
    }

    /*메모리 내용을 JSON 파일에 저장*/
    private  void saveDataToFile() throws IOException {
        List<Member> members = new ArrayList<>(store.values());
        objectMapper.writeValue(new File(DATA_FILE_PATH), members);
    }
    @Override
    public void save(Member member){
        if(member.getId() == null){
            /*처음 저장하는 저장하는 회원이라면*/

            long newId = sequence.incrementAndGet();
            /*1씩 증가하는 번호 생서*/

            member.setId(newId);
            store.put(newId, member);
            /*store(메모리 저장소)에 "id: member"로 저장*/
        }else{
            store.put(member.getId(),member);
        }
        /*이미 id가 있다면 그 id로 store에 덮어쓰기*/
        try {
            saveDataToFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /*전체 회원 데이터를 파일에 저장*/
    }
}
