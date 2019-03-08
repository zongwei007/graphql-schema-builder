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

public class InstantScalarTest {

    private InstantScalar scalar = new InstantScalar();
    private static final Instant SOURCE = Instant.parse("2011-12-03T10:15:30Z");

    @Test
    public void testSerialize() {
        assertThat(scalar.getCoercing().serialize(Instant.ofEpochMilli(SOURCE.toEpochMilli()))).isEqualTo(SOURCE);
        assertThat(scalar.getCoercing().serialize(SOURCE.toString())).isEqualTo(SOURCE);
        assertThat(scalar.getCoercing().serialize(Math.floorDiv(SOURCE.toEpochMilli(), 1000L))).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> scalar.getCoercing().serialize("2011-12-03"));
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> scalar.getCoercing().serialize(Duration.ofSeconds(1)));
    }

    @Test
    public void testParseValue() {
        assertThat(scalar.getCoercing().parseValue(SOURCE)).isEqualTo(SOURCE);
        assertThat(scalar.getCoercing().parseValue(SOURCE.toString())).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseValueException.class)
                .isThrownBy(() -> scalar.getCoercing().parseValue(Duration.ofSeconds(1)));
    }

    @Test
    public void testParseLiteral() {
        StringValue stringValue = new StringValue(SOURCE.toString());
        IntValue intValue = new IntValue(new BigInteger(String.valueOf(Math.floorDiv(SOURCE.toEpochMilli(), 1000L))));

        assertThat(scalar.getCoercing().parseLiteral(stringValue)).isEqualTo(SOURCE);
        assertThat(scalar.getCoercing().parseLiteral(intValue)).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseLiteralException.class)
                .isThrownBy(() -> scalar.getCoercing().parseLiteral(new BooleanValue(false)));
    }

}
