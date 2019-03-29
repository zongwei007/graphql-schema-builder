package com.ltsoft.graphql.impl;

import com.ltsoft.graphql.example.EnumObject;
import graphql.language.*;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class BasicDirectiveBuilderTest {

    @Test
    public void testBuild() {
        AnnotationBasicDirectiveBuilder resolver = new AnnotationBasicDirectiveBuilder(ele -> {
            ele.addArgument("bool", true);
            ele.addArgument("double", 1.0D);
            ele.addArgument("float", 2.0F);
            ele.addArgument("int", 1);
            ele.addArgument("long", 2L);
            ele.addArgument("char", 'a');
            ele.addArgument("string", "bar");
            ele.addArgument("array", 1, 2, 3);
            ele.addArgument("enum", EnumObject.second);
        });

        Directive directive = resolver.builder(null)
                .name("directive")
                .build();

        assertThat(directive.getName()).isEqualTo("directive");
        assertThat(directive.getArgument("bool")).isNotNull()
                .extracting(Argument::getValue)
                .first()
                .isInstanceOf(BooleanValue.class);
        assertThat(directive.getArgument("double")).isNotNull()
                .extracting(Argument::getValue)
                .first()
                .isInstanceOf(FloatValue.class);
        assertThat(directive.getArgument("float")).isNotNull()
                .extracting(Argument::getValue)
                .first()
                .isInstanceOf(FloatValue.class);
        assertThat(directive.getArgument("int")).isNotNull()
                .extracting(Argument::getValue)
                .first()
                .isInstanceOf(IntValue.class);
        assertThat(directive.getArgument("long")).isNotNull()
                .extracting(Argument::getValue)
                .first()
                .isInstanceOf(IntValue.class);
        assertThat(directive.getArgument("char")).isNotNull()
                .extracting(Argument::getValue)
                .first()
                .isInstanceOf(StringValue.class);
        assertThat(directive.getArgument("string")).isNotNull()
                .extracting(Argument::getValue)
                .first()
                .isInstanceOf(StringValue.class);
        assertThat(directive.getArgument("array")).isNotNull()
                .extracting(Argument::getValue)
                .first()
                .isInstanceOf(ArrayValue.class);
        assertThat(directive.getArgument("enum")).isNotNull()
                .extracting(Argument::getValue)
                .first()
                .isInstanceOf(EnumValue.class);
    }

    @Test
    public void testUnsupported() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new AnnotationBasicDirectiveBuilder((ele) -> ele.addArgument("short", Short.parseShort("3"))).builder(null))
                .withMessageStartingWith("Unsupported directive argument type");
    }

    private static class AnnotationBasicDirectiveBuilder extends BasicDirectiveBuilder<Annotation> {

        private Consumer<AnnotationBasicDirectiveBuilder> consumer;

        private AnnotationBasicDirectiveBuilder(Consumer<AnnotationBasicDirectiveBuilder> consumer) {
            this.consumer = consumer;
        }

        @Override
        public boolean isSupport(Class type) {
            return true;
        }

        @Override
        Directive.Builder apply(Annotation annotation, Directive.Builder builder) {
            consumer.accept(this);
            return builder;
        }
    }
}
