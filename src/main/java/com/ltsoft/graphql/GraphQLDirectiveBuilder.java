package com.ltsoft.graphql;

import graphql.language.Directive;

import java.lang.annotation.Annotation;

public interface GraphQLDirectiveBuilder<T extends Annotation> {

    boolean isSupport(Class<? extends Annotation> type);

    Directive.Builder builder(T annotation);
}
