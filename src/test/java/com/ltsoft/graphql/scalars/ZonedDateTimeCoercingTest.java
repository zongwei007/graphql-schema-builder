package com.ltsoft.graphql.scalars;

import graphql.language.BooleanValue;
import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.junit.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ZonedDateTimeCoercingTest {

    private ZonedDateTimeCoercing coercing = new ZonedDateTimeCoercing();
    private static final ZonedDateTime SOURCE = ZonedDateTime.parse("2011-12-03T10:15:30+08:00");

    @Test
    public void testSerialize() {
        assertThat(coercing.serialize(SOURCE)).isEqualTo(SOURCE);
        assertThat(coercing.serialize(SOURCE.toString())).isEqualTo(SOURCE);
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

        assertThat(coercing.parseLiteral(stringValue)).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseLiteralException.class)
                .isThrownBy(() -> coercing.parseLiteral(new BooleanValue(false)));
    }

}
