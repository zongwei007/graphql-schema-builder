package com.ltsoft.graphql.types;

import com.ltsoft.graphql.scalars.*;
import graphql.schema.GraphQLScalarType;

public final class ScalarTypes {

    public static final GraphQLScalarType GraphQLUUID = new UuidScalar();

    public static final GraphQLScalarType GraphQLURI = new UriScalar();

    public static final GraphQLScalarType GraphQLInstant = new InstantScalar();

    public static final GraphQLScalarType GraphQLLocalTime = new LocalTimeScalar();

    public static final GraphQLScalarType GraphQLLocalDateTime = new LocalDateTimeScalar();

    public static final GraphQLScalarType GraphQLZonedDateTime = new ZonedDateTimeScalar();

    public static final GraphQLScalarType GraphQLDuration = new DurationScalar();

    public static final GraphQLScalarType GraphQLPeriod = new PeriodScalar();

    public static final GraphQLScalarType GraphQLYear = new YearScalar();

    public static final GraphQLScalarType GraphQLYearMonth = new YearMonthScalar();

}
