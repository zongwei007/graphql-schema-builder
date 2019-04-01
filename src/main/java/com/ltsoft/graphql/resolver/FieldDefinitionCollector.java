package com.ltsoft.graphql.resolver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * 按 keyMapper 获取的 key 值进行去重，合并为 List。重复项将被忽略。
 */
class FieldDefinitionCollector<T> implements Collector<T, List<T>, List<T>> {

    private static final Set<Characteristics> CHARACTERISTICS = Collections.unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH));

    private final Map<String, T> fields = new ConcurrentHashMap<>();

    private final Function<T, String> keyMapper;

    FieldDefinitionCollector(Function<T, String> keyMapper) {
        this.keyMapper = keyMapper;
    }

    @Override
    public Supplier<List<T>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return (fieldDefinitions, fieldDefinition) -> {
            String key = keyMapper.apply(fieldDefinition);

            if (!fields.containsKey(key)) {
                fields.put(key, fieldDefinition);
                fieldDefinitions.add(fieldDefinition);
            }
        };
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        return (left, right) -> {
            left.addAll(right);
            return left;
        };
    }

    @Override
    public Function<List<T>, List<T>> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return CHARACTERISTICS;
    }
}
