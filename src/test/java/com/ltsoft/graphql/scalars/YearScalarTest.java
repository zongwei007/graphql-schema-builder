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

public class YearScalarTest {

    private YearScalar scalar = new YearScalar();
    private static final Year SOURCE = Year.of(2011);

    @Test
    public void testSerialize() {
        assertThat(scalar.getCoercing().serialize(Year.of(2011))).isEqualTo(SOURCE);
        assertThat(scalar.getCoercing().serialize(SOURCE.toString())).isEqualTo(SOURCE);
        assertThat(scalar.getCoercing().serialize(2011)).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> scalar.getCoercing().serialize("15M"));
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> scalar.getCoercing().serialize("a"));
    }

    @Test
    public void testParseValue() {
        assertThat(scalar.getCoercing().parseValue(SOURCE)).isEqualTo(SOURCE);
        assertThat(scalar.getCoercing().parseValue(SOURCE.toString())).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseValueException.class)
                .isThrownBy(() -> scalar.getCoercing().parseValue("b"));
    }

    @Test
    public void testParseLiteral() {
        StringValue stringValue = new StringValue(SOURCE.toString());
        IntValue intValue = new IntValue(new BigInteger("2011"));

        assertThat(scalar.getCoercing().parseLiteral(stringValue)).isEqualTo(SOURCE);
        assertThat(scalar.getCoercing().parseLiteral(intValue)).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseLiteralException.class)
                .isThrownBy(() -> scalar.getCoercing().parseLiteral(new BooleanValue(false)));
    }

}
