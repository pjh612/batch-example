package com.example.batchexample.common;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

public interface QuerydslCursor<T> {
    void initFirstCursor(JPAQueryFactory queryFactory);

    JPAQuery<?> createQuery(JPAQuery<?> query, int page);

    void nextCursor(T last);
}
