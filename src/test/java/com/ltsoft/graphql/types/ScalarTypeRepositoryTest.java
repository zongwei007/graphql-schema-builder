package com.ltsoft.graphql.types;

import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
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

import java.util.UUID;

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
    public void testRegister() {
        ScalarTypeRepository repo = new ScalarTypeRepository();
        //noinspection unchecked
        LogAssert expect = TestLoggers.sys().expect(
                "com.ltsoft.graphql",
                Level.WARN,
                LogMatchers.hasMatchingMessage(Matchers.containsString("registered"))
        );

        repo.register(HELLO);

        assertThat(repo.getScalarType("Hello").isPresent()).isTrue();

        repo.register(HELLO);

        expect.assertObservation();
    }

    @Test
    public void testGetScalarType() {
        ScalarTypeRepository repo = new ScalarTypeRepository();

        assertThat(repo.getScalarType("Instant"))
                .isPresent()
                .hasValue(ScalarTypes.GraphQLInstant);
        assertThat(repo.getScalarType("UUID")).isPresent();
    }

    @Test
    public void testAllExtensionTypes() {
        ScalarTypeRepository repo = new ScalarTypeRepository();

        assertThat(repo.allExtensionTypes()).isNotEmpty();
    }

    @Test
    public void testFindMappingScalarType() {
        ScalarTypeRepository repo = new ScalarTypeRepository();

        assertThat(repo.findMappingScalarType(String.class)).contains(Scalars.GraphQLString);
        assertThat(repo.findMappingScalarType(UUID.class)).contains(ScalarTypes.GraphQLUUID);
        assertThat(repo.findMappingScalarType(Object.class)).isNotPresent();
    }
}
