package com.example.batchexample.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerydslConfig {
    @PersistenceContext(unitName = "joyEntityManager")
    private final EntityManager joyEntityManager;

    public QuerydslConfig(EntityManager joyEntityManager) {
        this.joyEntityManager = joyEntityManager;
    }

    @Bean
    JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(joyEntityManager);
    }
}
