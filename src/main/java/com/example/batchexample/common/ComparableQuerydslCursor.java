package com.example.batchexample.common;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.lang.reflect.Field;

public class ComparableQuerydslCursor<T, C extends Comparable<C>, P extends ComparableExpression<C> & Path<C>> implements QuerydslCursor<T> {
    private final P cursorPath;
    private final EntityPathBase<?> entityPath;
    private C cursor;

    public ComparableQuerydslCursor(P cursorPath, EntityPathBase<?> entityPath) {
        this.cursorPath = cursorPath;
        this.entityPath = entityPath;
    }

    @Override
    public void initFirstCursor(JPAQueryFactory queryFactory) {
        cursor = queryFactory.select(cursorPath)
                .from(entityPath)
                .orderBy(cursorPath.asc())
                .fetchFirst();
    }

    @Override
    public JPAQuery<?> createQuery(JPAQuery<?> query, int page) {
        if(cursor == null) {
            return query;
        }

        return query.where(page == 0 ? cursorPath.goe(cursor) : cursorPath.gt(cursor));
    }

    @Override
    public void nextCursor(T last) {
        if(last == null) {
            cursor = null;
            return;
        }

        try {
            Field field = last.getClass().getDeclaredField(cursorPath.getMetadata().getName());
            field.setAccessible(true);
            cursor = (C) field.get(last);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }
    }


    public static class Builder<T, C extends Comparable<C>, P extends ComparableExpression<C> & Path<C>> {
        private P cursorPath;
        private EntityPathBase<?> entityPath;

        public Builder<T, C, P> cursorPath(P cursorPath) {
            this.cursorPath = cursorPath;

            return this;
        }

        public Builder<T, C, P> entityPath(EntityPathBase<?> entityPath) {
            this.entityPath = entityPath;

            return this;
        }

        public ComparableQuerydslCursor<T, C, P> build() {
            return new ComparableQuerydslCursor<>(cursorPath, entityPath);
        }
    }
}
