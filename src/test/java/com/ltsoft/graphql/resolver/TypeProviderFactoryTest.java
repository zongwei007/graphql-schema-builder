package com.ltsoft.graphql.resolver;

import com.ltsoft.graphql.TypeProvider;
import com.ltsoft.graphql.example.RootSchemaService;
import com.ltsoft.graphql.example.enumeration.EnumObject;
import com.ltsoft.graphql.example.iface.NormalInterface;
import com.ltsoft.graphql.example.scalar.HelloObject;
import com.ltsoft.graphql.impl.DefaultInstanceFactory;
import graphql.language.Document;
import graphql.schema.idl.RuntimeWiring;
import org.junit.Test;

import static com.ltsoft.graphql.annotations.GraphQLName.ROOT_MUTATION;
import static com.ltsoft.graphql.annotations.GraphQLName.ROOT_QUERY;
import static com.ltsoft.graphql.example.scalar.HelloObject.HelloObjectScalar;
import static org.assertj.core.api.Assertions.assertThat;

public class TypeProviderFactoryTest extends BasicTypeResolverTest {


    @Test
    public void testWiring() {
        TypeProviderFactory factory = new TypeProviderFactory();
        factory.setInstanceFactory(new DefaultInstanceFactory());
        factory.addScalar(HelloObjectScalar, HelloObject.class);
        factory.addClass(RootSchemaService.class);
        factory.addClass(EnumObject.class);
        factory.addClass(NormalInterface.class);

        RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();

        factory.getProviders()
                .map(TypeProvider::getWiringOperator)
                .forEach(operator -> operator.apply(builder));

        RuntimeWiring runtimeWiring = builder.build();

        assertThat(runtimeWiring.getEnumValuesProviders()).containsOnlyKeys("EnumObject");
        assertThat(runtimeWiring.getScalars()).containsValue(HelloObjectScalar);
        assertThat(runtimeWiring.getDataFetcherForType(ROOT_QUERY)).containsKeys("hello", "fieldDefinitionName", "fieldTypeName", "unknownArgument");
        assertThat(runtimeWiring.getDataFetcherForType(ROOT_MUTATION)).containsKeys("parse");
        assertThat(runtimeWiring.getTypeResolvers()).containsKeys("NormalInterface");
    }

    @Test
    public void testDocument() {
        TypeProviderFactory factory = new TypeProviderFactory();
        factory.setInstanceFactory(new DefaultInstanceFactory());
        factory.addScalar(HelloObjectScalar, HelloObject.class);
        factory.addClass(RootSchemaService.class);

        Document.Builder builder = Document.newDocument();

        factory.getProviders()
                .map(TypeProvider::getDefinitionOperator)
                .forEach(operator -> operator.apply(builder));

        Document document = builder.build();

        assertThat(document).isNotNull();
        assertThat(document.getDefinitions()).isNotEmpty();

//      由于 UnExecutableSchemaGenerator 的影响，无法输出 HelloObjectScalar
//        assertThat(printDocument(document))
//                .isEqualToIgnoringWhitespace(readSchemaExample("/example/EnumExtensionObject.graphql"));
    }

}
