Spring Batch 5.2.1 + JPA & QueryDsl 사용 및 슬랙 알람 예제입니다.

QuerydslCursorItemReader는 querydsl로 커서 페이징을 쉽게할 수 있도록 지원해주는 class입니다.

Example
1년이 지난 결제 실패 요청을 delete하기 위한 커서 페이징 방식의 ItemReader 생성 예제입니다.

```java
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
```

QuerydslCursor를 구현한 NumberPathQuerydslCursor 또는 ComparableQuerydslCursor를 사용해 커서에 대한 정보를 생성해줍니다.
커서에 대한 조정을 담당합니다. 현재는 오름차순 페이징만 지원합니다.

cursorPath: 커서로 지정할 QClass의 필드(컬럼)을 지정해줍니다.
entityPath: QClass를 지정합니다.

JPAQueryFactory, JPAQuery<?>를 제네릭 파라미터로 갖는 Function 인터페이스를 구현합니다. 실제 ItemReader에서 데이터를 읽어 오는 쿼리입니다.
QuerydslCursor가 이 쿼리에 cursor 조건절을 추가해주게 됩니다.



Slack 알람

<img width="709" alt="image" src="https://github.com/user-attachments/assets/ab658541-04e7-403e-9d82-1c8831fed262" />


<img width="1259" alt="image" src="https://github.com/user-attachments/assets/a718ee40-6f3e-4b27-b4e2-cfcc3fae4b23" />

