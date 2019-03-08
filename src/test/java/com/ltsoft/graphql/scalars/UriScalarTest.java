package com.ltsoft.graphql.scalars;

import graphql.language.BooleanValue;
import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


public class UriScalarTest {

    private UriScalar scalar = new UriScalar();
    private static final URI SOURCE = URI.create("../foo/bar");

    @Test
    public void testSerialize() throws MalformedURLException {
        assertThat(scalar.getCoercing().serialize(SOURCE)).isEqualTo(SOURCE);
        assertThat(scalar.getCoercing().serialize(new URL("http://localhost"))).isEqualTo(URI.create("http://localhost"));
        assertThat(scalar.getCoercing().serialize(SOURCE.toString())).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> scalar.getCoercing().serialize(1));
    }

    @Test
    public void testParseValue() {
        assertThat(scalar.getCoercing().parseValue(SOURCE)).isEqualTo(SOURCE);
        assertThat(scalar.getCoercing().parseValue(SOURCE.toString())).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseValueException.class)
                .isThrownBy(() -> scalar.getCoercing().parseValue(1));
    }

    @Test
    public void testParseLiteral() {
        StringValue stringValue = new StringValue(SOURCE.toString());

        assertThat(scalar.getCoercing().parseLiteral(stringValue)).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseLiteralException.class)
                .isThrownBy(() -> scalar.getCoercing().parseLiteral(new BooleanValue(false)));
    }

}
