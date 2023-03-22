package com.changddao.querydsl.repository;

import com.changddao.querydsl.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> ,MemberRepositoryCustom {
    List<Member> findByUsername(String username);
}
