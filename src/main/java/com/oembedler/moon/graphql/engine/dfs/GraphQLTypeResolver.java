package com.oembedler.moon.graphql.engine.dfs;

import com.oembedler.moon.graphql.engine.GraphQLSchemaConfig;
import graphql.schema.GraphQLScalarType;

/**
 * @author Sergey Kuptsov
 * @since 23/03/2017
 */
public interface GraphQLTypeResolver {
    Class getType();

    GraphQLScalarType resolve(GraphQLSchemaConfig graphQLSchemaConfig);
}
