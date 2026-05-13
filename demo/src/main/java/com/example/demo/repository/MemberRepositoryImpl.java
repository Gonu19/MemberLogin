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
    * ConcurrentHashMap은 Locking Mechanism(잠금 메커니즘)을 사용하여 데이터의 일관성 보장
    * Long, 즉 id가 key*/

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
            saveDataToFile(); /*전체 회원 데이터를 파일에 저장*/
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Member findById(Long id) {
        return store.get(id);
    }
    /*id를 통해 Member 객체 전체를 반환*/

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }
    /*메모리에 있는 모든 Member 객체를 리스트형태로 불러옴*/

    @Override
    public void remove(Long id) {
        store.remove(id);
        try {
            saveDataToFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /*remove 실행 후 상태를 저장*/

    @Override
    public Member findByUserId(String userId) {
        return store.values().stream()
                .filter(member -> member.getUserId().equals(userId))
                .findAny()
                .orElse(null);
    }
}
/* userId를 매개변수로 받아온 뒤 user이름이 맞다면 반환
* store.values()를 통해 key값을 제외한 Member객체들만 모음
* 이 데이터들을 stream 위에 올려둠
* 입력받은 매개변수와 객체의 userId가 일치하는지 확인 - Lambda expression 조건 검사 수행
* .findAny() : 필터링을 받은 객체를 아무거나 하나 반환. 없을수도 있기에 Optional(findAny() 메소드 호출시 자동으로 Optional<T> 객체로 Wrapping 됨)
* Optional 안에 Member 객체가 있다면 반환, 없으면 null 반환
*
* findById는 store의 구조상 id가 키임으로 바로 value에 접근 가능
* 하지만 Member 객체 안에 뭐가 들었는지 모르기 때문에 findByUserId()는 모두 대조해서 찾음
* findById처럼 찾을려면 Map<String, Member>로 map을 하나 더 만들어야함(메모리 2배)
* */


/* Stream이란 ?
* 명령형 프로그래밍(Imperative)(ex. for,if 등)에서 선언적 데이터 처리(Declarative)로 전환
* 지연 연산 - 최종연산(Terminal operation)이 호출되는 순간에 연산을 한번에 묶어서 실행 -> 연산 효율성 극대화
* 불변성 - 원본 데이터 훼손 x
* 일회용성 - 소비된 Stream을 재사용하려고 하면 IllegalStateException 에러가 발생
* */
