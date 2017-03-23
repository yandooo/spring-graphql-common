package com.oembedler.moon.graphql.engine.type.resolver;

import com.oembedler.moon.graphql.engine.GraphQLSchemaConfig;
import com.oembedler.moon.graphql.engine.dfs.GraphQLTypeResolver;
import com.oembedler.moon.graphql.engine.dfs.MappingConstants;
import com.oembedler.moon.graphql.engine.type.GraphQLDateType;
import graphql.schema.GraphQLScalarType;

import java.util.Date;

/**
 * @author Sergey Kuptsov
 * @since 23/03/2017
 */
public class DateTypeResolver implements GraphQLTypeResolver {

    @Override
    public Class getType() {
        return Date.class;
    }

    @Override
    public GraphQLScalarType resolve(GraphQLSchemaConfig graphQLSchemaConfig) {
        if (graphQLSchemaConfig.isDateAsTimestamp())
            return MappingConstants.graphQLTimestamp;
        else
            return new GraphQLDateType("Date", "Date formatted according to defined format string",
                    graphQLSchemaConfig.getDateFormat());

    }
}
