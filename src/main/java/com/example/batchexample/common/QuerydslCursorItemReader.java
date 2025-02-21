package com.example.batchexample.common;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public class QuerydslCursorItemReader<T> extends QuerydslPagingItemReader<T> {
    private final QuerydslCursor<T> cursor;

    public QuerydslCursorItemReader(EntityManagerFactory entityManagerFactory,
                                    Function<JPAQueryFactory, JPAQuery<T>> jpaQueryFunction, QuerydslCursor<T> cursor, int pageSize) {
        super(entityManagerFactory, pageSize, jpaQueryFunction);
        this.cursor = cursor;
    }


    @Override
    protected Query createQuery() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        JPAQuery<?> query = cursor.createQuery(queryFunction.apply(queryFactory), getPage());

        return query.createQuery();
    }

    @Override
    protected void doReadPage() {
        EntityTransaction tx = beginTransactionIfTransacted();

        if (getPage() == 0) {
            cursor.initFirstCursor(queryFactory);
        }

        Query query = createQuery();
        query.setMaxResults(getPageSize());

        initResults();

        fetch(query, tx);

        cursor.nextCursor(getLast());
    }

    private T getLast() {
        if(results.isEmpty()) {
            return null;
        }
        return results.get(results.size() - 1);
    }

}
