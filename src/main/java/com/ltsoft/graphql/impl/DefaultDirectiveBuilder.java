package com.ltsoft.graphql.impl;

import com.ltsoft.graphql.annotations.GraphQLDirective;
import com.ltsoft.graphql.annotations.GraphQLIgnore;
import graphql.language.Directive;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.ltsoft.graphql.resolver.ResolveUtil.resolveFieldName;
import static com.ltsoft.graphql.resolver.ResolveUtil.resolveTypeName;

public class DefaultDirectiveBuilder extends BasicDirectiveBuilder<Annotation> {

    @Override
    public boolean isSupport(Class<? extends Annotation> type) {
        return type.isAnnotationPresent(GraphQLDirective.class);
    }

    @Override
    Directive.Builder apply(Annotation annotation, Directive.Builder result) {
        Class<? extends Annotation> type = annotation.annotationType();
        // Annotation 实际是通过 Proxy 代理的实例，需要通过 InvocationHandler 进行调用
        InvocationHandler proxy = Proxy.getInvocationHandler(annotation);

        try {
            for (Method method : type.getMethods()) {
                Class<?> owner = method.getDeclaringClass();

                if (owner.equals(type) && !method.isAnnotationPresent(GraphQLIgnore.class)) {
                    Object value = proxy.invoke(annotation, method, new Object[0]);
                    Object[] param = value.getClass().isArray() ? (Object[]) value : new Object[]{value};

                    addArgument(resolveFieldName(method, null), param);
                }
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException("Read annotation as GraphQL directive fail", e);
        }

        return result.name(resolveTypeName(type));
    }


}
