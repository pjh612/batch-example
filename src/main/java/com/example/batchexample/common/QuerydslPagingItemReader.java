package com.example.batchexample.common;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class QuerydslPagingItemReader<T> extends AbstractPagingItemReader<T> {
    protected final Map<String, Object> jpaPropertyMap = new HashMap<>();
    protected EntityManagerFactory entityManagerFactory;
    protected EntityManager entityManager;
    protected JPAQueryFactory queryFactory;
    protected final Function<JPAQueryFactory, JPAQuery<T>> queryFunction;
    protected boolean transacted = true;// default value

    public QuerydslPagingItemReader(EntityManagerFactory entityManagerFactory,
                                    int pageSize,
                                    Function<JPAQueryFactory, JPAQuery<T>> queryFunction) {
        this(entityManagerFactory, pageSize, true, queryFunction);
    }

    public QuerydslPagingItemReader(EntityManagerFactory entityManagerFactory,
                                    int pageSize,
                                    boolean transacted,
                                    Function<JPAQueryFactory, JPAQuery<T>> queryFunction) {
        setName(ClassUtils.getShortName(JpaPagingItemReader.class));
        this.entityManagerFactory = entityManagerFactory;
        this.queryFunction = queryFunction;
        setPageSize(pageSize);
        setTransacted(transacted);
    }


    /**
     * Create a query using an appropriate query provider (entityManager OR
     * queryProvider).
     */
    protected Query createQuery() {
        JPAQuery<?> apply = queryFunction.apply(queryFactory);

        return apply.createQuery();
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * By default (true) the EntityTransaction will be started and committed around the
     * read. Can be overridden (false) in cases where the JPA implementation doesn't
     * support a particular transaction. (e.g. Hibernate with a JTA transaction). NOTE:
     * may cause problems in guaranteeing the object consistency in the
     * EntityManagerFactory.
     *
     * @param transacted indicator
     */
    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }

    @Override
    protected void doOpen() throws Exception {
        super.doOpen();

        entityManager = entityManagerFactory.createEntityManager(jpaPropertyMap);
        queryFactory = new JPAQueryFactory(entityManager);
        if (entityManager == null) {
            throw new DataAccessResourceFailureException("Unable to obtain an EntityManager");
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        if (queryFunction == null) {
            Assert.state(entityManagerFactory != null, "EntityManager is required when queryProvider is null");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doReadPage() {
        EntityTransaction tx = beginTransactionIfTransacted();
        Query query = createQuery().setFirstResult(getPage() * getPageSize()).setMaxResults(getPageSize());
        initResults();
        fetch(query, tx);
    }

    protected void initResults() {
        if (results == null) {
            results = new CopyOnWriteArrayList<>();
        } else {
            results.clear();
        }
    }

    protected void fetch(Query query, EntityTransaction tx) {
        if (!transacted) {
            List<T> queryResult = query.getResultList();
            for (T entity : queryResult) {
                entityManager.detach(entity);
                results.add(entity);
            } // end if
        } else {
            results.addAll(query.getResultList());
            tx.commit();
        } // end if
    }

    protected EntityTransaction beginTransactionIfTransacted() {
        EntityTransaction tx = null;

        if (transacted) {
            tx = entityManager.getTransaction();
            tx.begin();

            entityManager.flush();
            entityManager.clear();
        } // end if
        return tx;
    }

    @Override
    protected void doClose() throws Exception {
        entityManager.close();
        super.doClose();
    }

}
