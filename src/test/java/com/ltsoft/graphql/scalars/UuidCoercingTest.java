package com.ltsoft.graphql.scalars;

import graphql.language.BooleanValue;
import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class UuidCoercingTest {

    private UuidCoercing coercing = new UuidCoercing();
    private static final UUID SOURCE = UUID.randomUUID();

    @Test
    public void testSerialize() {
        assertThat(coercing.serialize(SOURCE)).isEqualTo(SOURCE);
        assertThat(coercing.serialize(SOURCE.toString())).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> coercing.serialize("1234567890"));
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> coercing.serialize(1));
    }

    @Test
    public void testParseValue() {
        assertThat(coercing.parseValue(SOURCE)).isEqualTo(SOURCE);
        assertThat(coercing.parseValue(SOURCE.toString())).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseValueException.class)
                .isThrownBy(() -> coercing.parseValue("1234567890"));
    }

    @Test
    public void testParseLiteral() {
        StringValue stringValue = new StringValue(SOURCE.toString());

        assertThat(coercing.parseLiteral(stringValue)).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseLiteralException.class)
                .isThrownBy(() -> coercing.parseLiteral(new BooleanValue(false)));
    }

}
