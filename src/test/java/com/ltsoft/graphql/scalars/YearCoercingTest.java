package com.ltsoft.graphql.scalars;

import graphql.language.BooleanValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.junit.Test;

import java.math.BigInteger;
import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class YearCoercingTest {

    private YearCoercing coercing = new YearCoercing();
    private static final Year SOURCE = Year.of(2011);

    @Test
    public void testSerialize() {
        assertThat(coercing.serialize(Year.of(2011))).isEqualTo(2011);
        assertThat(coercing.serialize(SOURCE.toString())).isEqualTo(2011);
        assertThat(coercing.serialize(2011)).isEqualTo(2011);
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> coercing.serialize("15M"));
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> coercing.serialize("a"));
    }

    @Test
    public void testParseValue() {
        assertThat(coercing.parseValue(SOURCE)).isEqualTo(SOURCE);
        assertThat(coercing.parseValue(SOURCE.toString())).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseValueException.class)
                .isThrownBy(() -> coercing.parseValue("b"));
    }

    @Test
    public void testParseLiteral() {
        StringValue stringValue = new StringValue(SOURCE.toString());
        IntValue intValue = new IntValue(new BigInteger("2011"));

        assertThat(coercing.parseLiteral(stringValue)).isEqualTo(SOURCE);
        assertThat(coercing.parseLiteral(intValue)).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseLiteralException.class)
                .isThrownBy(() -> coercing.parseLiteral(new BooleanValue(false)));
    }

}
