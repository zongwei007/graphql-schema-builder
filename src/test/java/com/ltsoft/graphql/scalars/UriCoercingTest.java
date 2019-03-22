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


public class UriCoercingTest {

    private UriCoercing coercing = new UriCoercing();
    private static final URI SOURCE = URI.create("../foo/bar");

    @Test
    public void testSerialize() throws MalformedURLException {
        assertThat(coercing.serialize(SOURCE)).isEqualTo(SOURCE);
        assertThat(coercing.serialize(new URL("http://localhost"))).isEqualTo(URI.create("http://localhost"));
        assertThat(coercing.serialize(SOURCE.toString())).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingSerializeException.class)
                .isThrownBy(() -> coercing.serialize(1));
    }

    @Test
    public void testParseValue() {
        assertThat(coercing.parseValue(SOURCE)).isEqualTo(SOURCE);
        assertThat(coercing.parseValue(SOURCE.toString())).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseValueException.class)
                .isThrownBy(() -> coercing.parseValue(1));
    }

    @Test
    public void testParseLiteral() {
        StringValue stringValue = new StringValue(SOURCE.toString());

        assertThat(coercing.parseLiteral(stringValue)).isEqualTo(SOURCE);
        assertThatExceptionOfType(CoercingParseLiteralException.class)
                .isThrownBy(() -> coercing.parseLiteral(new BooleanValue(false)));
    }

}
