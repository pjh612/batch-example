package com.example.batchexample.payment;

import com.example.batchexample.common.ComparableQuerydslCursor;
import com.example.batchexample.common.QuerydslCursorItemReader;
import com.example.batchexample.common.alarm.JobExecuteAlarmListener;
import com.example.batchexample.common.alarm.MessageSender;
import com.example.batchexample.payment.dto.PaymentRequestResponse;
import com.example.batchexample.payment.entity.QPaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.Function;

@Configuration
public class DeleteGarbagePaymentJobConfig {
    private static final String JOB_NAME = "deleteGarbagePaymentJob";

    private final JPAQueryFactory queryFactory;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final MessageSender messageSender;
    private final ObjectMapper objectMapper;

    public DeleteGarbagePaymentJobConfig(JPAQueryFactory queryFactory, PlatformTransactionManager transactionManager,
                                         @Qualifier("joyEntityManagerFactory") EntityManagerFactory entityManagerFactory, MessageSender messageSender, ObjectMapper objectMapper) {
        this.queryFactory = queryFactory;
        this.transactionManager = transactionManager;
        this.entityManagerFactory = entityManagerFactory;
        this.messageSender = messageSender;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Job deleteGarbagePaymentRequestJob(JobRepository jobRepository) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(deleteGarbagePaymentRequestStep(jobRepository, null))
                .listener(new JobExecuteAlarmListener(messageSender, objectMapper))
                .build();
    }

    @Bean
    @JobScope
    public Step deleteGarbagePaymentRequestStep(JobRepository jobRepository, @Value("#{jobParameters[today]}") String today) {

        return new StepBuilder("deleteGarbagePaymentStep", jobRepository)
                .<PaymentRequestResponse, PaymentRequestResponse>chunk(100, transactionManager)
                .reader(reader(today))
                .writer(itemWriter())
                .build();
    }

    public ItemReader<PaymentRequestResponse> reader(String today) {
        ComparableQuerydslCursor<PaymentRequestResponse, UUID, ComparablePath<UUID>> cursor = new ComparableQuerydslCursor.Builder<PaymentRequestResponse, UUID, ComparablePath<UUID>>()
                .cursorPath(QPaymentRequest.paymentRequest.requestId)
                .entityPath(QPaymentRequest.paymentRequest)
                .build();

        return new QuerydslCursorItemReader<>(
                entityManagerFactory,
                selectGarbagePaymentRequestFunction(today),
                cursor,
                100
        );
    }

    private static Function<JPAQueryFactory, JPAQuery<PaymentRequestResponse>> selectGarbagePaymentRequestFunction(String today) {
        LocalDate parse = LocalDate.parse(today);
        LocalDateTime localDateTime = parse.minusYears(1).atStartOfDay();
        Instant oneYearsAgoInstant = localDateTime.toInstant(ZoneOffset.UTC);

        return jpaQueryFactory ->
                jpaQueryFactory.select(Projections.constructor(PaymentRequestResponse.class, QPaymentRequest.paymentRequest.requestId))
                        .from(QPaymentRequest.paymentRequest)
                        .where(QPaymentRequest.paymentRequest.createdAt.before(oneYearsAgoInstant)
                                .and(QPaymentRequest.paymentRequest.paymentStatus.eq(-1)));
    }

    @Bean
    public ItemWriter<PaymentRequestResponse> itemWriter() {
        return new DeleteGarbagePaymentItemWriter(queryFactory);
    }
}
