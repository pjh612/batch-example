package com.example.batchexample.common;

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.lang.reflect.Field;

public class NumberPathQuerydslCursor<T, C extends Number & Comparable<C>> implements QuerydslCursor<T> {
    private final NumberPath<C> cursorPath;
    private final EntityPathBase<?> entityPath;
    private C cursor;

    public NumberPathQuerydslCursor(NumberPath<C> cursorPath, EntityPathBase<?> entityPath) {
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
        return query.where(page == 0 ? cursorPath.goe(cursor) : cursorPath.gt(cursor));
    }

    @Override
    public void nextCursor(T last) {
        try {
            Field field = last.getClass().getDeclaredField(cursorPath.getMetadata().getName());
            field.setAccessible(true);
            cursor = (C) field.get(last);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }
    }


    public static class Builder<T, C extends Number & Comparable<C>> {
        private NumberPath<C> cursorPath;
        private EntityPathBase<?> entityPath;

        public Builder<T, C> cursorPath(NumberPath<C> cursorPath) {
            this.cursorPath = cursorPath;

            return this;
        }

        public Builder<T, C> entityPath(EntityPathBase<?> entityPath) {
            this.entityPath = entityPath;

            return this;
        }

        public NumberPathQuerydslCursor<T, C> build() {
            return new NumberPathQuerydslCursor<>(cursorPath, entityPath);
        }
    }
}
