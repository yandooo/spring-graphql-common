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

import com.oembedler.moon.graphql.GraphQLSchemaBeanFactory;
import com.oembedler.moon.graphql.engine.dfs.GraphQLSchemaDfsTraversal;
import graphql.schema.GraphQLSchema;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class GraphQLSchemaBuilder {

    private final GraphQLSchemaConfig graphQLSchemaConfig;
    private final GraphQLSchemaBeanFactory graphQLSchemaBeanFactory;

    public GraphQLSchemaBuilder(GraphQLSchemaConfig graphQLSchemaConfig, GraphQLSchemaBeanFactory graphQLSchemaBeanFactory) {
        Assert.notNull(graphQLSchemaConfig, "Schema configuration can not be null");
        Assert.notNull(graphQLSchemaBeanFactory, "Schema bean factory can not be null");

        this.graphQLSchemaConfig = graphQLSchemaConfig;
        this.graphQLSchemaBeanFactory = graphQLSchemaBeanFactory;
    }

    public GraphQLSchemaHolder buildSchema(final Class<?> schemaClass) {
        Assert.notNull(schemaClass, "Schema class can not be null");

        GraphQLSchemaDfsTraversal graphQLSchemaDfsTraversal = new GraphQLSchemaDfsTraversal(
                schemaClass,
                getGraphQLSchemaConfig(),
                getGraphQLSchemaBeanFactory());

        GraphQLSchema graphQLSchema = graphQLSchemaDfsTraversal.traverse();
        GraphQLSchemaHolder graphQLSchemaHolder =
                new GraphQLSchemaHolder(graphQLSchemaDfsTraversal.getSchemaName(),
                        graphQLSchema,
                        getGraphQLSchemaConfig(),
                        graphQLSchemaDfsTraversal.getObjectTypeResolverMap());

        graphQLSchemaHolder.setMutationInputTypeResolverMap(graphQLSchemaDfsTraversal.getMutationInputTypeResolverMap());
        graphQLSchemaHolder.setMutationReturnTypeResolverMap(graphQLSchemaDfsTraversal.getMutationReturnTypeResolverMap());
        graphQLSchemaHolder.setFieldDefinitionResolverMap(graphQLSchemaDfsTraversal.getFieldDefinitionResolverMap());

        return graphQLSchemaHolder;
    }

    public GraphQLSchemaConfig getGraphQLSchemaConfig() {
        return graphQLSchemaConfig;
    }

    public GraphQLSchemaBeanFactory getGraphQLSchemaBeanFactory() {
        return graphQLSchemaBeanFactory;
    }
}
