package com.ltsoft.graphql.types;

import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spf4j.log.Level;
import org.spf4j.test.log.LogAssert;
import org.spf4j.test.log.TestLoggers;
import org.spf4j.test.log.junit4.Spf4jTestLogJUnitRunner;
import org.spf4j.test.matchers.LogMatchers;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Spf4jTestLogJUnitRunner.class)
public class ScalarTypeRepositoryTest {

    private static final GraphQLScalarType HELLO = new GraphQLScalarType(
            "Hello", "hello type",
            new Coercing<String, String>() {
                @Override
                public String serialize(Object input) {
                    return "Hello World!";
                }

                @Override
                public String parseValue(Object input) {
                    return "Hello World!";
                }

                @Override
                public String parseLiteral(Object input) {
                    return "Hello World!";
                }
            });

    @Test
    public void register() {
        ScalarTypeRepository repo = new ScalarTypeRepository();
        //noinspection unchecked
        LogAssert expect = TestLoggers.sys().expect(
                "com.ltsoft.graphql",
                Level.WARN,
                LogMatchers.hasMatchingMessage(Matchers.containsString("registered"))
        );

        repo.register(HELLO);

        assertThat(repo.get("Hello").isPresent()).isTrue();

        repo.register(HELLO);

        expect.assertObservation();
    }

    @Test
    public void getScalarType() {
        ScalarTypeRepository repo = new ScalarTypeRepository();

        assertThat(repo.get("Instant"))
                .isPresent()
                .hasValue(ScalarTypes.GraphQLInstant);
        assertThat(repo.get("UUID")).isPresent();
    }
}
