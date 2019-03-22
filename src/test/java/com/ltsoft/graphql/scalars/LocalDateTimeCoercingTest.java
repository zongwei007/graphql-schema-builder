package com.ltsoft.graphql.scalars;

import graphql.language.BooleanValue;
import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class LocalDateTimeCoercingTest {

    private LocalDateTimeCoercing coercing = new LocalDateTimeCoercing();
    private static final LocalDateTime SOURCE = LocalDateTime.parse("2011-12-03T10:15:00");

    @Test
    public void testSerialize() {
        assertThat(coercing.serialize(LocalDateTime.of(2011, 12, 3, 10, 15))).isEqualTo("2011-12-03T10:15:00");
        assertThat(coercing.serialize(SOURCE.toString())).isEqualTo("2011-12-03T10:15:00");
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> coercing.serialize("2011-12-03"));
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> coercing.serialize(Duration.ofSeconds(1)));
    }

    @Test
    public void testParseValue() {
        assertThat(coercing.parseValue(LocalDateTime.of(2011, 12, 3, 10, 15))).isEqualTo(SOURCE);
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
