/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 oEmbedler Inc. and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 *  persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.oembedler.moon.graphql.engine.type;

import graphql.GraphQLException;
import graphql.schema.Coercing;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class GraphQLEnumTypeExt extends GraphQLEnumType {

    private final Coercing coercingExt = new Coercing() {

        @Override
        public Object serialize(Object input) {
            return getNameByValue(input);
        }

        @Override
        public Object parseValue(Object input) {
            return getCoercingSuper().parseValue(input);
        }

        @Override
        public Object parseLiteral(Object input) {
            return getCoercingSuper().parseLiteral(input);
        }
    };

    public GraphQLEnumTypeExt(String name, String description, List<GraphQLEnumValueDefinition> values) {
        super(name, description, values);
    }

    private Object getNameByValue(Object value) {
        Object normalizedValue = enumToStringIfAny(value);
        for (GraphQLEnumValueDefinition valueDefinition : getValues()) {
            if (normalizedValue.equals(valueDefinition.getValue())) return valueDefinition.getName();
        }
        throw new GraphQLException("");
    }

    private Object enumToStringIfAny(Object value) {
        if (value instanceof Enum)
            return value.toString();
        return value;
    }

    @Override
    public Coercing getCoercing() {
        return this.coercingExt;
    }

    public Coercing getCoercingSuper() {
        return super.getCoercing();
    }

    public static class Builder {

        private String name;
        private String description;
        private final List<GraphQLEnumValueDefinition> values = new ArrayList<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder value(String name, Object value, String description) {
            values.add(new GraphQLEnumValueDefinition(name, description, value));
            return this;
        }

        public Builder value(String name, Object value) {
            values.add(new GraphQLEnumValueDefinition(name, null, value));
            return this;
        }

        public Builder value(String name) {
            values.add(new GraphQLEnumValueDefinition(name, null, name));
            return this;
        }

        public GraphQLEnumType build() {
            return new GraphQLEnumTypeExt(name, description, values);
        }

    }
}