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

package com.oembedler.moon.graphql.engine.dfs;

import graphql.GraphQLException;
import graphql.Scalars;
import graphql.language.FloatValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class MappingConstants {

    private static final Map<Type, GraphQLScalarType> GRAPHQL_SCALARS_TYPE_MAP = new HashMap<>();

    // ---

    public static GraphQLScalarType graphQLTimestamp = new GraphQLScalarType("Timestamp", "Timestamp since 1970 Jan 1st", new Coercing() {
        @Override
        public Object serialize(Object input) {
            if (input instanceof String) {
                return new Date(Long.parseLong((String) input));
            } else if (input instanceof Date) {
                return (Date) input;
            } else if (input instanceof Integer) {
                return new Date(((Integer) input).longValue());
            } else if (input instanceof Long) {
                return new Date((Long) input);
            } else {
                throw new GraphQLException("Wrong timestamp value");
            }
        }

        @Override
        public Object parseValue(Object input) {
            return serialize(input);
        }

        @Override
        public Object parseLiteral(Object input) {
            if (!(input instanceof StringValue)) return null;
            return new Date(Long.parseLong(((StringValue) input).getValue()));
        }
    });

    // ---

    public static GraphQLScalarType GraphQLDouble = new GraphQLScalarType("Double", "Built-in Double", new Coercing() {
        @Override
        public Object serialize(Object input) {
            if (input instanceof String) {
                return Double.parseDouble((String) input);
            } else if (input instanceof Double) {
                return input;
            } else {
                throw new GraphQLException();
            }
        }

        @Override
        public Object parseValue(Object input) {
            return serialize(input);
        }

        @Override
        public Object parseLiteral(Object input) {
            return ((FloatValue) input).getValue().doubleValue();
        }
    });

    // ---

    static {
        GRAPHQL_SCALARS_TYPE_MAP.put(boolean.class, Scalars.GraphQLBoolean);
        GRAPHQL_SCALARS_TYPE_MAP.put(byte.class, Scalars.GraphQLInt);
        GRAPHQL_SCALARS_TYPE_MAP.put(char.class, Scalars.GraphQLString);
        GRAPHQL_SCALARS_TYPE_MAP.put(double.class, GraphQLDouble);
        GRAPHQL_SCALARS_TYPE_MAP.put(float.class, Scalars.GraphQLFloat);
        GRAPHQL_SCALARS_TYPE_MAP.put(int.class, Scalars.GraphQLInt);
        GRAPHQL_SCALARS_TYPE_MAP.put(long.class, Scalars.GraphQLLong);

        GRAPHQL_SCALARS_TYPE_MAP.put(Boolean.class, Scalars.GraphQLBoolean);
        GRAPHQL_SCALARS_TYPE_MAP.put(Byte.class, Scalars.GraphQLInt);
        GRAPHQL_SCALARS_TYPE_MAP.put(Short.class, Scalars.GraphQLInt);
        GRAPHQL_SCALARS_TYPE_MAP.put(Integer.class, Scalars.GraphQLInt);
        GRAPHQL_SCALARS_TYPE_MAP.put(Long.class, Scalars.GraphQLLong);
        GRAPHQL_SCALARS_TYPE_MAP.put(Float.class, Scalars.GraphQLFloat);
        GRAPHQL_SCALARS_TYPE_MAP.put(Character.class, Scalars.GraphQLString);
        GRAPHQL_SCALARS_TYPE_MAP.put(String.class, Scalars.GraphQLString);
        GRAPHQL_SCALARS_TYPE_MAP.put(Double.class, GraphQLDouble);
    }

    public static GraphQLScalarType getScalarGraphQLType(final Type cls) {
        return GRAPHQL_SCALARS_TYPE_MAP.get(cls);
    }

}
