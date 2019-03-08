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

public class UuidScalarTest {

    private UuidScalar scalar = new UuidScalar();
    private static final UUID SOURCE = UUID.randomUUID();

    @Test
    public void testSerialize() {
        assertThat(scalar.getCoercing().serialize(SOURCE)).isEqualTo(SOURCE);
        assertThat(scalar.getCoercing().serialize(SOURCE.toString())).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> scalar.getCoercing().serialize("1234567890"));
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> scalar.getCoercing().serialize(1));
    }

    @Test
    public void testParseValue() {
        assertThat(scalar.getCoercing().parseValue(SOURCE)).isEqualTo(SOURCE);
        assertThat(scalar.getCoercing().parseValue(SOURCE.toString())).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseValueException.class)
                .isThrownBy(() -> scalar.getCoercing().parseValue("1234567890"));
    }

    @Test
    public void testParseLiteral() {
        StringValue stringValue = new StringValue(SOURCE.toString());

        assertThat(scalar.getCoercing().parseLiteral(stringValue)).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseLiteralException.class)
                .isThrownBy(() -> scalar.getCoercing().parseLiteral(new BooleanValue(false)));
    }

}
