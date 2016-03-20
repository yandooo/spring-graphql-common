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

package com.oembedler.moon.graphql.engine.execute;

import com.oembedler.moon.graphql.engine.GraphQLSchemaHolder;
import graphql.ExecutionResult;
import graphql.GraphQLException;
import graphql.execution.*;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
class RxExecution {

    private FieldCollector fieldCollector = new FieldCollector();
    private final ExecutionStrategy strategy;
    private final GraphQLSchemaHolder graphQLSchemaHolder;
    private final int maxQueryDepth;
    private final int maxQueryComplexity;

    public RxExecution(GraphQLSchemaHolder graphQLSchemaHolder, int maxQueryDepth, int maxQueryComplexity, ExecutionStrategy strategy) {
        this.strategy = strategy;
        this.graphQLSchemaHolder = graphQLSchemaHolder;
        this.maxQueryDepth = maxQueryDepth;
        this.maxQueryComplexity = maxQueryComplexity;
    }

    public ExecutionResult execute(GraphQLSchema graphQLSchema, Object root, Document document, String operationName, Map<String, Object> args) {
        ExecutionContextBuilder executionContextBuilder = new ExecutionContextBuilder(new ValuesResolver());
        ExecutionContext executionContext = executionContextBuilder.build(graphQLSchema, strategy, root, document, operationName, args);
        return executeOperation(executionContext, root, executionContext.getOperationDefinition());
    }

    private GraphQLObjectType getOperationRootType(GraphQLSchema graphQLSchema, OperationDefinition operationDefinition) {
        if (operationDefinition.getOperation() == OperationDefinition.Operation.MUTATION) {
            return graphQLSchema.getMutationType();

        } else if (operationDefinition.getOperation() == OperationDefinition.Operation.QUERY) {
            return graphQLSchema.getQueryType();

        } else {
            throw new GraphQLException();
        }
    }

    private ExecutionResult executeOperation(
            ExecutionContext executionContext,
            Object root,
            OperationDefinition operationDefinition) {
        GraphQLObjectType operationRootType = getOperationRootType(executionContext.getGraphQLSchema(), executionContext.getOperationDefinition());

        Map<String, List<Field>> fields = new LinkedHashMap<String, List<Field>>();
        fieldCollector.collectFields(executionContext, operationRootType, operationDefinition.getSelectionSet(), new ArrayList<String>(), fields);

        if (operationDefinition.getOperation() == OperationDefinition.Operation.MUTATION) {
            return new GraphQLDefaultRxExecutionStrategy(graphQLSchemaHolder, maxQueryDepth, maxQueryComplexity)
                                                      .execute(executionContext, operationRootType, root, fields);
        } else {
            return strategy.execute(executionContext, operationRootType, root, fields);
        }
    }
}
