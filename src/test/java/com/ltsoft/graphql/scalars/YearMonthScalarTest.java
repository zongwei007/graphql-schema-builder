package com.ltsoft.graphql.scalars;

import graphql.language.BooleanValue;
import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.junit.Test;

import java.time.Duration;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class YearMonthScalarTest {

    private YearMonthScalar scalar = new YearMonthScalar();
    private static final YearMonth SOURCE = YearMonth.parse("2011-10");

    @Test
    public void testSerialize() {
        assertThat(scalar.getCoercing().serialize(YearMonth.of(2011, 10))).isEqualTo("2011-10");
        assertThat(scalar.getCoercing().serialize(SOURCE.toString())).isEqualTo("2011-10");
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> scalar.getCoercing().serialize("2011-12-03"));
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> scalar.getCoercing().serialize(Duration.ofSeconds(1)));
    }

    @Test
    public void testParseValue() {
        assertThat(scalar.getCoercing().parseValue(YearMonth.of(2011, 10))).isEqualTo(SOURCE);
        assertThat(scalar.getCoercing().parseValue(SOURCE.toString())).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseValueException.class)
                .isThrownBy(() -> scalar.getCoercing().parseValue(Duration.ofSeconds(1)));
    }

    @Test
    public void testParseLiteral() {
        StringValue stringValue = new StringValue(SOURCE.toString());

        assertThat(scalar.getCoercing().parseLiteral(stringValue)).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseLiteralException.class)
                .isThrownBy(() -> scalar.getCoercing().parseLiteral(new BooleanValue(false)));
    }
}
