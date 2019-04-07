package com.ltsoft.graphql.scalars;

import com.ltsoft.graphql.example.scalar.HelloObject;
import graphql.Scalars;
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

    @Test
    public void testRegister() {
        ScalarTypeRepository repo = ScalarTypeRepository.getInstance();
        //noinspection unchecked
        LogAssert expect = TestLoggers.sys().expect(
                "com.ltsoft.graphql",
                Level.WARN,
                LogMatchers.hasMatchingMessage(Matchers.containsString("registered"))
        );

        repo.register(HelloObject.HelloObjectScalar);

        assertThat(repo.getScalarType("Hello").isPresent()).isTrue();

        repo.register(HelloObject.HelloObjectScalar);

        expect.assertObservation();
    }

    @Test
    public void testGetScalarType() {
        ScalarTypeRepository repo = ScalarTypeRepository.getInstance();

        assertThat(repo.getScalarType("Instant"))
                .isPresent()
                .hasValue(ScalarTypes.GraphQLInstant);
        assertThat(repo.getScalarType("UUID")).isPresent();
    }

    @Test
    public void testFindMappingScalarType() {
        ScalarTypeRepository repo = ScalarTypeRepository.getInstance();

        assertThat(repo.findMappingScalarType(String.class)).contains(Scalars.GraphQLString);
        assertThat(repo.findMappingScalarType(UUID.class)).contains(ScalarTypes.GraphQLUUID);
        assertThat(repo.findMappingScalarType(Object.class)).isNotPresent();
    }
}
