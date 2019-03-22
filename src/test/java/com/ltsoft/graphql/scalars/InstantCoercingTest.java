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

public class InstantCoercingTest {

    private InstantCoercing coercing = new InstantCoercing();
    private static final Instant SOURCE = Instant.parse("2011-12-03T10:15:30Z");

    @Test
    public void testSerialize() {
        assertThat(coercing.serialize(Instant.ofEpochMilli(SOURCE.toEpochMilli()))).isEqualTo("2011-12-03T10:15:30Z");
        assertThat(coercing.serialize(SOURCE.toString())).isEqualTo("2011-12-03T10:15:30Z");
        assertThat(coercing.serialize(Math.floorDiv(SOURCE.toEpochMilli(), 1000L))).isEqualTo("2011-12-03T10:15:30Z");
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> coercing.serialize("2011-12-03"));
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> coercing.serialize(Duration.ofSeconds(1)));
    }

    @Test
    public void testParseValue() {
        assertThat(coercing.parseValue(SOURCE)).isEqualTo(SOURCE);
        assertThat(coercing.parseValue(SOURCE.toString())).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseValueException.class)
                .isThrownBy(() -> coercing.parseValue(Duration.ofSeconds(1)));
    }

    @Test
    public void testParseLiteral() {
        StringValue stringValue = new StringValue(SOURCE.toString());
        IntValue intValue = new IntValue(new BigInteger(String.valueOf(Math.floorDiv(SOURCE.toEpochMilli(), 1000L))));

        assertThat(coercing.parseLiteral(stringValue)).isEqualTo(SOURCE);
        assertThat(coercing.parseLiteral(intValue)).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseLiteralException.class)
                .isThrownBy(() -> coercing.parseLiteral(new BooleanValue(false)));
    }

}
