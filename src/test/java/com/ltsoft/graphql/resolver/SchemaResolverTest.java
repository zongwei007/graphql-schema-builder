package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.example.MutationService;
import com.ltsoft.graphql.example.NormalObject;
import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLTypeUtil;
import org.junit.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaResolverTest {

    @Test
    public void testNormalObject() {
        SchemaResolver schemaResolver = new SchemaResolver();

        GraphQLObjectType objectType = schemaResolver.object(NormalObject.class);

        assertThat(objectType.getName()).isEqualTo("Normal");
        assertThat(objectType.getDescription()).isEqualTo("Normal GraphQL Object");
        assertThat(objectType.getFieldDefinition("foo"))
                .isNotNull()
                .satisfies(definition -> {
                    assertThat(definition.getDescription()).isEqualTo("GraphQL foo field");
                    assertThat(definition.getArguments()).hasSize(0);
                    assertThat(definition.getType()).isEqualTo(Scalars.GraphQLString);
                });
        assertThat(objectType.getFieldDefinition("barList"))
                .isNotNull()
                .satisfies(definition -> assertThat(definition.getType()).satisfies(type -> {
                    assertThat(GraphQLTypeUtil.isList(type)).isTrue();
                    assertThat(GraphQLTypeUtil.unwrapAll(type)).isEqualTo(Scalars.GraphQLString);
                }));
        assertThat(objectType.getFieldDefinition("fooList"))
                .isNotNull()
                .satisfies(definition -> assertThat(definition.getType()).satisfies(type -> {
                    assertThat(GraphQLTypeUtil.isNonNull(type)).isTrue();
                    assertThat(GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapOne(type))).isTrue();
                    assertThat(GraphQLTypeUtil.unwrapAll(type)).isEqualTo(Scalars.GraphQLString);
                }));
        assertThat(objectType.getFieldDefinition("count"))
                .isNotNull()
                .satisfies(definition -> {
                    assertThat(definition.getDescription()).isEqualTo("GraphQL count field");
                    assertThat(definition.getType()).matches(GraphQLTypeUtil::isNonNull);
                    assertThat(GraphQLTypeUtil.unwrapOne(definition.getType())).isEqualTo(Scalars.GraphQLInt);
                    assertThat(definition.getArgument("cnd")).isNotNull()
                            .satisfies(argument -> {
                                assertThat(argument.getDefaultValue()).isEqualTo("1");
                                assertThat(argument.getDescription()).isEqualTo("A cnd argument");
                                assertThat(argument.getType()).matches(GraphQLTypeUtil::isNonNull);
                                assertThat(GraphQLTypeUtil.unwrapOne(argument.getType()))
                                        .isEqualTo(Scalars.GraphQLString);
                            });
                });
        assertThat(objectType.getFieldDefinition("filterDateTimes"))
                .isNotNull()
                .satisfies(definition -> {
                    assertThat(definition.getType()).matches(GraphQLTypeUtil::isNonNull);
                    assertThat(GraphQLTypeUtil.unwrapOne(definition.getType())).matches(GraphQLTypeUtil::isList);
                    assertThat(GraphQLTypeUtil.unwrapAll(definition.getType())).isEqualTo(ExtendedScalars.DateTime);
                    assertThat(definition.getArgument("args")).isNotNull()
                            .satisfies(argument -> {
                                assertThat(argument.getDefaultValue()).isInstanceOf(OffsetDateTime[].class);
                                assertThat(argument.getType()).matches(GraphQLTypeUtil::isNonNull);
                                assertThat(GraphQLTypeUtil.unwrapOne(argument.getType())).matches(GraphQLTypeUtil::isList);
                                assertThat(GraphQLTypeUtil.unwrapAll(argument.getType())).isEqualTo(ExtendedScalars.DateTime);
                            });
                });
    }

    @Test
    public void testGraphQLView() {
        SchemaResolver schemaResolver = new SchemaResolver();

        GraphQLObjectType objectType = schemaResolver.object(MutationService.class);

        assertThat(objectType.getName()).isEqualTo("MutationService");

        assertThat(objectType.getFieldDefinition("create"))
                .isNotNull()
                .satisfies(definition -> {
                    assertThat(definition.getArguments()).hasSize(2);
                    assertThat(definition.getArgument("name"))
                            .isNotNull()
                            .satisfies(argument -> {
                                assertThat(argument.getType()).matches(GraphQLTypeUtil::isNonNull);
                                assertThat(GraphQLTypeUtil.unwrapOne(argument.getType())).isEqualTo(Scalars.GraphQLString);
                            });
                    assertThat(definition.getArgument("parent"))
                            .isNotNull()
                            .satisfies(argument -> {
                                assertThat(argument.getType()).isInstanceOf(GraphQLTypeReference.class);
                                assertThat(argument.getType().getName()).isEqualTo("MutationInputObject");
                            });
                });

        assertThat(objectType.getFieldDefinition("update"))
                .isNotNull()
                .satisfies(definition -> {
                    assertThat(definition.getArguments()).hasSize(3);
                    assertThat(definition.getArgument("id"))
                            .isNotNull()
                            .satisfies(argument -> {
                                assertThat(argument.getType()).matches(GraphQLTypeUtil::isNonNull);
                                assertThat(GraphQLTypeUtil.unwrapOne(argument.getType())).isEqualTo(Scalars.GraphQLLong);
                            });
                    assertThat(definition.getArgument("name"))
                            .isNotNull()
                            .satisfies(argument -> assertThat(argument.getType()).isEqualTo(Scalars.GraphQLString));
                    assertThat(definition.getArgument("parent"))
                            .isNotNull()
                            .satisfies(argument -> {
                                assertThat(argument.getType()).isInstanceOf(GraphQLTypeReference.class);
                                assertThat(argument.getType().getName()).isEqualTo("MutationInputObject");
                            });
                });

    }
}
