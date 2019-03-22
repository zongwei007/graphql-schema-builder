package com.ltsoft.graphql.scalars;

import graphql.schema.GraphQLScalarType;

public final class ScalarTypes {

    public static final GraphQLScalarType GraphQLUUID = GraphQLScalarType.newScalar()
            .name("UUID")
            .description("UUID GraphQL ScalarType")
            .coercing(new UuidCoercing())
            .build();

    public static final GraphQLScalarType GraphQLURI = GraphQLScalarType.newScalar()
            .name("URI")
            .description("URI GraphQL ScalarType")
            .coercing(new UriCoercing())
            .build();

    public static final GraphQLScalarType GraphQLInstant = GraphQLScalarType.newScalar()
            .name("Instant")
            .description("JDK8 Instant GraphQL ScalarType")
            .coercing(new InstantCoercing())
            .build();

    public static final GraphQLScalarType GraphQLLocalTime = GraphQLScalarType.newScalar()
            .name("LocalTime")
            .description("JDK8 LocalTime GraphQL ScalarType")
            .coercing(new LocalTimeCoercing())
            .build();

    public static final GraphQLScalarType GraphQLLocalDateTime = GraphQLScalarType.newScalar()
            .name("LocalDateTime")
            .description("JDK8 LocalDateTime GraphQLType")
            .coercing(new LocalDateTimeCoercing())
            .build();

    public static final GraphQLScalarType GraphQLZonedDateTime = GraphQLScalarType.newScalar()
            .name("ZonedDateTime")
            .description("JDK8 ZonedDateTime GraphQL ScalarType")
            .coercing(new ZonedDateTimeCoercing())
            .build();

    public static final GraphQLScalarType GraphQLDuration = GraphQLScalarType.newScalar()
            .name("Duration")
            .description("JDK8 Duration GraphQL ScalarType")
            .coercing(new DurationCoercing())
            .build();

    public static final GraphQLScalarType GraphQLPeriod = GraphQLScalarType.newScalar()
            .name("Period")
            .description("JDK8 Period GraphQL ScalarType")
            .coercing(new PeriodCoercing())
            .build();

    public static final GraphQLScalarType GraphQLYear = GraphQLScalarType.newScalar()
            .name("Year")
            .description("JDK8 Year GraphQL ScalarType")
            .coercing(new YearCoercing())
            .build();

    public static final GraphQLScalarType GraphQLYearMonth = GraphQLScalarType.newScalar()
            .name("YearMonth")
            .description("JDK8 YearMonth GraphQL ScalarType")
            .coercing(new YearMonthCoercing())
            .build();

}
