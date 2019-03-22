package com.ltsoft.graphql.scalars;

import graphql.language.BooleanValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.junit.Test;

import java.math.BigInteger;
import java.time.Instant;
import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class PeriodCoercingTest {

    private PeriodCoercing coercing = new PeriodCoercing();
    private static final Period DAYS_15 = Period.ofDays(15);

    @Test
    public void testSerialize() {
        assertThat(coercing.serialize(Period.parse(DAYS_15.toString()))).isEqualTo("P15D");
        assertThat(coercing.serialize(DAYS_15.toString())).isEqualTo("P15D");
        assertThat(coercing.serialize(DAYS_15.getDays())).isEqualTo("P15D");
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> coercing.serialize("15M"));
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> coercing.serialize(Instant.now()));
    }

    @Test
    public void testParseValue() {
        assertThat(coercing.parseValue(DAYS_15)).isEqualTo(DAYS_15);
        assertThat(coercing.parseValue(DAYS_15.toString())).isEqualTo(DAYS_15);
        assertThatExceptionOfType(CoercingParseValueException.class)
                .isThrownBy(() -> coercing.parseValue(Instant.now()));
    }

    @Test
    public void testParseLiteral() {
        StringValue stringValue = new StringValue(DAYS_15.toString());
        IntValue intValue = new IntValue(new BigInteger(String.valueOf(DAYS_15.getDays())));

        assertThat(coercing.parseLiteral(stringValue)).isEqualTo(DAYS_15);
        assertThat(coercing.parseLiteral(intValue)).isEqualTo(DAYS_15);
        assertThatExceptionOfType(CoercingParseLiteralException.class)
                .isThrownBy(() -> coercing.parseLiteral(new BooleanValue(false)));
    }

}
