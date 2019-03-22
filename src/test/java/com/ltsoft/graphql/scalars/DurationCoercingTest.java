package com.ltsoft.graphql.scalars;

import graphql.language.BooleanValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.junit.Test;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class DurationCoercingTest {

    private DurationCoercing coercing = new DurationCoercing();
    private static final Duration MINUTES_15 = Duration.ofMinutes(15);
    private static final Duration SECONDS_15 = Duration.ofSeconds(15);

    @Test
    public void testSerialize() {
        assertThat(coercing.serialize(Duration.ofMinutes(15))).isEqualTo("PT15M");
        assertThat(coercing.serialize("PT15M")).isEqualTo("PT15M");
        assertThat(coercing.serialize(15L)).isEqualTo("PT15S");
        assertThat(coercing.serialize(15)).isEqualTo("PT15S");
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> coercing.serialize("15M"));
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> coercing.serialize(Instant.now()));
    }

    @Test
    public void testParseValue() {
        assertThat(coercing.parseValue(MINUTES_15)).isEqualTo(MINUTES_15);
        assertThat(coercing.parseValue("PT15M")).isEqualTo(MINUTES_15);
        assertThatExceptionOfType(CoercingParseValueException.class)
                .isThrownBy(() -> coercing.parseValue(Instant.now()));
    }

    @Test
    public void testParseLiteral() {
        StringValue stringValue = new StringValue("PT15M");
        IntValue intValue = new IntValue(new BigInteger("15"));

        assertThat(coercing.parseLiteral(stringValue)).isEqualTo(MINUTES_15);
        assertThat(coercing.parseLiteral(intValue)).isEqualTo(SECONDS_15);
        assertThatExceptionOfType(CoercingParseLiteralException.class)
                .isThrownBy(() -> coercing.parseLiteral(new BooleanValue(false)));
    }
}
