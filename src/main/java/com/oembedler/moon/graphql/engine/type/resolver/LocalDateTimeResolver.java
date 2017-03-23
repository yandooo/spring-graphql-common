package com.oembedler.moon.graphql.engine.type.resolver;

import com.oembedler.moon.graphql.engine.GraphQLSchemaConfig;
import com.oembedler.moon.graphql.engine.dfs.GraphQLTypeResolver;
import com.oembedler.moon.graphql.engine.dfs.MappingConstants;
import com.oembedler.moon.graphql.engine.type.GraphQLLocalDateTimeType;
import graphql.schema.GraphQLScalarType;

import java.time.LocalDateTime;

/**
 * @author Sergey Kuptsov
 * @since 23/03/2017
 */
public class LocalDateTimeResolver implements GraphQLTypeResolver {
    @Override
    public Class getType() {
        return LocalDateTime.class;
    }

    @Override
    public GraphQLScalarType resolve(GraphQLSchemaConfig graphQLSchemaConfig) {
        if (graphQLSchemaConfig.isDateAsTimestamp())
            return MappingConstants.graphQLTimestamp;
        else {
            return new GraphQLLocalDateTimeType("LocalDateTime", "LocalDateTime formatted according to defined format string",
                    graphQLSchemaConfig.getDateFormat());
        }
    }
}
