package com.example.batchexample.payment;

import com.example.batchexample.payment.dto.PaymentRequestResponse;
import com.example.batchexample.payment.entity.QPaymentRequest;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public class DeleteGarbagePaymentItemWriter implements ItemWriter<PaymentRequestResponse> {
    private final JPAQueryFactory jpaQueryFactory;

    public DeleteGarbagePaymentItemWriter(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }


    @Override
    @Transactional
    public void write(Chunk<? extends PaymentRequestResponse> chunk) throws Exception {
        List<UUID> ids = chunk.getItems()
                .stream()
                .map(PaymentRequestResponse::requestId)
                .toList();

        jpaQueryFactory.delete(QPaymentRequest.paymentRequest)
                .where(QPaymentRequest.paymentRequest.requestId.in(ids))
                .execute();
    }
}
