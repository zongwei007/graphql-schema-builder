package com.ltsoft.graphql.scalars;

import graphql.language.BooleanValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.junit.Test;

import java.math.BigInteger;
import java.time.Instant;
import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class PeriodScalarTest {

    private PeriodScalar scalar = new PeriodScalar();
    private static final Period DAYS_15 = Period.ofDays(15);

    @Test
    public void testSerialize() {
        assertThat(scalar.getCoercing().serialize(Period.parse(DAYS_15.toString()))).isEqualTo("P15D");
        assertThat(scalar.getCoercing().serialize(DAYS_15.toString())).isEqualTo("P15D");
        assertThat(scalar.getCoercing().serialize(DAYS_15.getDays())).isEqualTo("P15D");
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> scalar.getCoercing().serialize("15M"));
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> scalar.getCoercing().serialize(Instant.now()));
    }

    @Test
    public void testParseValue() {
        assertThat(scalar.getCoercing().parseValue(DAYS_15)).isEqualTo(DAYS_15);
        assertThat(scalar.getCoercing().parseValue(DAYS_15.toString())).isEqualTo(DAYS_15);
        assertThatExceptionOfType(CoercingParseValueException.class)
                .isThrownBy(() -> scalar.getCoercing().parseValue(Instant.now()));
    }

    @Test
    public void testParseLiteral() {
        StringValue stringValue = new StringValue(DAYS_15.toString());
        IntValue intValue = new IntValue(new BigInteger(String.valueOf(DAYS_15.getDays())));

        assertThat(scalar.getCoercing().parseLiteral(stringValue)).isEqualTo(DAYS_15);
        assertThat(scalar.getCoercing().parseLiteral(intValue)).isEqualTo(DAYS_15);
        assertThatExceptionOfType(CoercingParseLiteralException.class)
                .isThrownBy(() -> scalar.getCoercing().parseLiteral(new BooleanValue(false)));
    }

}
