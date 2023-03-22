package com.changddao.querydsl.repository;

import com.changddao.querydsl.dto.MemberSearchCondition;
import com.changddao.querydsl.dto.MemberTeamDto;
import com.changddao.querydsl.entity.Member;
import com.changddao.querydsl.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {
    @Autowired
    private EntityManager em;
    @Autowired
    private MemberJpaRepository memberJpaRepository;



    @Test
    public void searchTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 40, teamB);
        Member member4 = new Member("member4", 50, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
        memberSearchCondition.setAgeLoe(20);
        memberSearchCondition.setTeamName("teamA");

        List<MemberTeamDto> result = memberJpaRepository.search(memberSearchCondition);
        assertThat(result).extracting("username")
                .containsExactly("member1","member2");

    }

}