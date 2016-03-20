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

import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLUnionType;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
class SchemaHelper {

    public static void replaceTypeReferencesForUnionType(final GraphQLSchema schema, final Set<GraphQLUnionType> graphQLUnionTypeMap) {
        Field graphQLTypesField = ReflectionUtils.findField(GraphQLUnionType.class, "types");
        graphQLTypesField.setAccessible(true);
        for (GraphQLUnionType graphQLUnionType : graphQLUnionTypeMap) {
            List<GraphQLType> graphQLTypes = new ArrayList<>();
            for (GraphQLType graphQLType : graphQLUnionType.getTypes()) {
                if (graphQLType instanceof GraphQLTypeReference) {
                    graphQLTypes.add(schema.getType(graphQLType.getName()));
                } else {
                    graphQLTypes.add(graphQLType);
                }
            }
            ReflectionUtils.setField(graphQLTypesField, graphQLUnionType, graphQLTypes);
        }
    }
}
