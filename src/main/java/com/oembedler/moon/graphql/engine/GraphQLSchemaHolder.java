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

package com.oembedler.moon.graphql.engine;

import com.oembedler.moon.graphql.engine.dfs.GraphQLFieldDefinitionWrapper;
import graphql.schema.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class GraphQLSchemaHolder {

    private final String schemaName;
    private final GraphQLSchema graphQLSchema;
    private final GraphQLSchemaConfig graphQLSchemaConfig;

    private final ConcurrentHashMap<Class<?>, GraphQLType> objectTypeResolverMap;
    private Map<String, Map<Class<?>, GraphQLOutputType>> mutationReturnTypeResolverMap;
    private Map<String, Map<Class<?>, GraphQLInputObjectField>> mutationInputTypeResolverMap;
    private Map<GraphQLFieldDefinition, GraphQLFieldDefinitionWrapper> fieldDefinitionResolverMap;

    public GraphQLSchemaHolder(String schemaName,
                               GraphQLSchema graphQLSchema,
                               GraphQLSchemaConfig graphQLSchemaConfig,
                               ConcurrentHashMap<Class<?>, GraphQLType> objectTypeResolverMap) {
        this.schemaName = schemaName;
        this.objectTypeResolverMap = objectTypeResolverMap;
        this.graphQLSchema = graphQLSchema;
        this.graphQLSchemaConfig = graphQLSchemaConfig;
    }

    public ConcurrentHashMap<Class<?>, GraphQLType> getObjectTypeResolverMap() {
        return objectTypeResolverMap;
    }

    public GraphQLSchema getGraphQLSchema() {
        return graphQLSchema;
    }

    public Map<String, Map<Class<?>, GraphQLOutputType>> getMutationReturnTypeResolverMap() {
        return mutationReturnTypeResolverMap;
    }

    public void setMutationReturnTypeResolverMap(Map<String, Map<Class<?>, GraphQLOutputType>> mutationReturnTypeResolverMap) {
        this.mutationReturnTypeResolverMap = mutationReturnTypeResolverMap;
    }

    public Map<String, Map<Class<?>, GraphQLInputObjectField>> getMutationInputTypeResolverMap() {
        return mutationInputTypeResolverMap;
    }

    public void setMutationInputTypeResolverMap(Map<String, Map<Class<?>, GraphQLInputObjectField>> mutationInputTypeResolverMap) {
        this.mutationInputTypeResolverMap = mutationInputTypeResolverMap;
    }

    public Map<GraphQLFieldDefinition, GraphQLFieldDefinitionWrapper> getFieldDefinitionResolverMap() {
        return fieldDefinitionResolverMap;
    }

    public void setFieldDefinitionResolverMap(Map<GraphQLFieldDefinition, GraphQLFieldDefinitionWrapper> fieldDefinitionResolverMap) {
        this.fieldDefinitionResolverMap = fieldDefinitionResolverMap;
    }

    public GraphQLSchemaConfig getGraphQLSchemaConfig() {
        return graphQLSchemaConfig;
    }

    public String getSchemaName() {
        return schemaName;
    }
}
