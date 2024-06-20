package com.example.communityservice.repository;

import com.example.communityservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // 이 인터페이스가 데이터 액세스 레이어(DAL)의 리포지토리임을 나타냅니다.
public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일로 사용자를 찾는 메서드입니다. 반환 타입은 Optional<User>로, 해당 이메일을 가진 사용자가 없을 수도 있음을 나타냅니다.
    Optional<User> findByEmail(String email);

    // 닉네임으로 사용자를 찾는 메서드입니다. 반환 타입은 Optional<User>로, 해당 닉네임을 가진 사용자가 없을 수도 있음을 나타냅니다.
    Optional<User> findByNickname(String nickname);
}
