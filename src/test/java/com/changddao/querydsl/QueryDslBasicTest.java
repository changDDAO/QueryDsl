package com.changddao.querydsl;

import com.changddao.querydsl.entity.Member;
import com.changddao.querydsl.entity.QMember;
import com.changddao.querydsl.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.changddao.querydsl.entity.QMember.*;
import static com.changddao.querydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
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
    }

    @Test
    public void startJPQL() {
        //member1을 찾아라.
        Member findMember = em.createQuery("select m from Member m where m.username = : username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQueryDsl() {


        Member findMember = queryFactory.select(member)
                .from(member)
                .where(member.username.eq("member1"))//파라미터 바인딩 처리
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    //나이가 10살이고 이름이 member1인 member 가지고오기
    @Test
    public void search() {
        Member findMember = queryFactory.selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    public void variousFetch() {
        QueryResults<Member> results = queryFactory.selectFrom(member)
                .fetchResults();
        results.getTotal();
        List<Member> members = results.getResults();
        for (Member member : members) {
            System.out.println("member = " + member);
        }
        System.out.println("results.getTotal() = " + results.getTotal());
    }
    /*
    * 회원 정렬 순서
    * 1. 회원 나이 내림차순(desc)
    * 2. 회원 이름 오름차순(asc)
    * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
    * */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5",100));
        em.persist(new Member("member6",100));

        List<Member> fetch = queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc())
                .orderBy(member.username.asc().nullsLast())
                .fetch();
        for (Member member : fetch) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void aggregation() {
        List<Tuple> result = queryFactory.select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();
        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.avg())).isEqualTo(30);
        assertThat(tuple.get(member.age.max())).isEqualTo(50);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }
/*
* 팀의 이름과 각 팀의 평균 연령을 구해라.
* */
    @Test
    public void group() throws Exception {
        //given
        List<Tuple> result = queryFactory.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        //when
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        //then
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(45);

    }

    //teamA에 소속된 모든회원

    @Test
    public void join() throws Exception {
        //given
        List<Member> result = queryFactory.selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();


        //then
        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");

    }

    @Test
    public void thetaJoin() throws Exception {
        //given
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));
        em.persist(new Member("teamD"));

        //when
        List<Member> result = queryFactory.select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();
        //then
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA","teamB");

    }


}
