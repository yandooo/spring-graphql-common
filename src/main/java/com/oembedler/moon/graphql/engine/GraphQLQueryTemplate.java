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

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class GraphQLQueryTemplate {

    private static final String MUTATION_TEMPLATE = "mutation %sQuery($%s: %s!) {   %s(%s: $%s) %s }";

    private final SchemaUtil schemaUtil = new SchemaUtil();
    private final GraphQLSchema graphQLSchema;
    private final GraphQLSchemaHolder graphQLSchemaHolder;
    private final ObjectMapper objectMapper;

    public static final class MutationQuery {
        private final String query;
        private final Map<String, Object> variables;

        public MutationQuery(String query, Map<String, Object> variables) {
            this.query = query;
            this.variables = variables;
        }

        public String getQuery() {
            return query;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }
    }

    public GraphQLQueryTemplate(final GraphQLSchemaHolder graphQLSchemaHolder) {
        Assert.notNull(graphQLSchemaHolder, "GraphQL Schema holder can not be null");
        this.graphQLSchemaHolder = graphQLSchemaHolder;
        this.graphQLSchema = this.graphQLSchemaHolder.getGraphQLSchema();
        this.objectMapper = new ObjectMapper();
        if (!graphQLSchemaHolder.getGraphQLSchemaConfig().isDateAsTimestamp())
            this.objectMapper.setDateFormat(new SimpleDateFormat(graphQLSchemaHolder.getGraphQLSchemaConfig().getDateFormat()));
    }

    public MutationQuery forMutation(final String mutationName, final Object... inputs) {
        return new MutationQuery(buildQuery(mutationName), buildInput(mutationName, inputs));
    }

    // ---

    public String buildQuery(final String mutationName) {
        Assert.hasText(mutationName, "Mutation value must not be null");

        GraphQLObjectType objectType = graphQLSchema.getMutationType();
        GraphQLFieldDefinition graphQLFieldDefinition = objectType.getFieldDefinition(mutationName);
        Assert.notNull(graphQLFieldDefinition, "Mutation does not exist");

        GraphQLObjectType graphQLOutputType = (GraphQLObjectType) graphQLFieldDefinition.getType();

        return String.format(
                MUTATION_TEMPLATE,
                mutationName,
                getMutationArgumentName(graphQLFieldDefinition),
                getMutationArgumentType(graphQLFieldDefinition),
                mutationName,
                getMutationInputArgumentName(),
                getMutationInputArgumentName(),
                expandNestedObjectTree("", graphQLOutputType));
    }

    private String getMutationInputArgumentName() {
        return graphQLSchemaHolder.getGraphQLSchemaConfig().getMutationInputArgumentName();
    }

    protected GraphQLArgument getMutationArgument(GraphQLFieldDefinition mutationGraphQLFieldDefinition) {
        List<GraphQLArgument> graphQLArguments = mutationGraphQLFieldDefinition.getArguments();
        return graphQLArguments.size() > 0 ? graphQLArguments.get(0) : null;
    }

    protected String getMutationArgumentName(GraphQLFieldDefinition mutationGraphQLFieldDefinition) {
        GraphQLArgument graphQLArgument = getMutationArgument(mutationGraphQLFieldDefinition);
        return graphQLArgument != null ? graphQLArgument.getName() : "";
    }

    protected String getMutationArgumentType(GraphQLFieldDefinition mutationGraphQLFieldDefinition) {
        GraphQLArgument graphQLArgument = getMutationArgument(mutationGraphQLFieldDefinition);
        GraphQLInputType graphQLInputType = graphQLArgument.getType();
        return graphQLInputType != null ? schemaUtil.getUnmodifiedType(graphQLInputType).getName() : "";
    }

    protected String expandNestedObjectTree(String nodeName, GraphQLObjectType graphQLOutputType) {
        StringBuilder stringBuilder = new StringBuilder();
        for (GraphQLFieldDefinition graphQLFieldDefinition : graphQLOutputType.getFieldDefinitions()) {
            GraphQLUnmodifiedType graphQLUnmodifiedType = schemaUtil.getUnmodifiedType(graphQLFieldDefinition.getType());
            if (graphQLUnmodifiedType instanceof GraphQLScalarType)
                stringBuilder.append(graphQLFieldDefinition.getName() + ", ");
            else {
                GraphQLObjectType castedGraphQLObjectType = (GraphQLObjectType) graphQLUnmodifiedType;
                stringBuilder.append(expandNestedObjectTree(graphQLFieldDefinition.getName(), castedGraphQLObjectType));
            }
        }
        String result = StringUtils.removeEnd(stringBuilder.toString(), ", ");
        return StringUtils.isNoneBlank(result) ? String.format("%s { %s }", nodeName, result) : "";
    }

    // ---

    protected Map<String, Object> buildInput(final String mutationName, final Object... inputs) {
        Map<String, Object> variables = new HashMap<>();

        GraphQLObjectType objectType = graphQLSchema.getMutationType();
        GraphQLFieldDefinition graphQLFieldDefinition = objectType.getFieldDefinition(mutationName);
        List<GraphQLArgument> graphQLArguments = graphQLFieldDefinition.getArguments();

        Map<String, Object> inputArgs = new HashMap<>();
        if (inputs != null && inputs.length > 0) {
            Map<Class<?>, GraphQLInputObjectField> argsDefs = graphQLSchemaHolder.getMutationInputTypeResolverMap().get(mutationName);
            for (Object object : inputs) {
                Class<?> cls = object.getClass();
                GraphQLInputObjectField graphQLInputObjectField = argsDefs.get(cls);
                if (graphQLInputObjectField != null) {
                    inputArgs.put(graphQLInputObjectField.getName(), convertToMap(object));
                }
            }
        }

        if (isClientMutationIdInjected())
            inputArgs.put(getClientMutationIdName(), generateClientMutationId());

        variables.put(getMutationInputArgumentName(), inputArgs);

        return variables;
    }

    private Object convertToMap(Object argObject) {
        return objectMapper.convertValue(argObject, Object.class);
    }

    private boolean isClientMutationIdInjected() {
        return graphQLSchemaHolder.getGraphQLSchemaConfig().isInjectClientMutationId();
    }

    private String getClientMutationIdName() {
        return graphQLSchemaHolder.getGraphQLSchemaConfig().getClientMutationIdName();
    }

    private String generateClientMutationId() {
        return UUID.randomUUID().toString();
    }
}