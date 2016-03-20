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
import graphql.InvalidSyntaxError;
import graphql.execution.ExecutionStrategy;
import graphql.language.Document;
import graphql.language.SourceLocation;
import graphql.parser.Parser;
import graphql.validation.ValidationError;
import graphql.validation.Validator;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import rx.Observable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static graphql.Assert.assertNotNull;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class GraphQLQueryExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLQueryExecutor.class);

    private GraphQLSchemaHolder graphQLSchemaHolder;
    private ExecutionStrategy executionStrategy;
    private String requestQuery;
    private String operationName;
    private Object context = Collections.emptyMap();
    private Map<String, Object> arguments = Collections.emptyMap();
    private ExecutorService executorService;
    private int maxQueryComplexity = -1;
    private int maxQueryDepth = -1;


    private GraphQLQueryExecutor(final GraphQLSchemaHolder graphQLSchemaHolder) {
        this(graphQLSchemaHolder, null);
    }

    private GraphQLQueryExecutor(final GraphQLSchemaHolder graphQLSchemaHolder, final ExecutionStrategy executionStrategy) {
        Assert.notNull(graphQLSchemaHolder, "GraphQL Schema holder can not be null");
        this.graphQLSchemaHolder = graphQLSchemaHolder;
        this.executionStrategy = executionStrategy;
    }

    public static GraphQLQueryExecutor create(final GraphQLSchemaHolder graphQLSchemaHolder) {
        return new GraphQLQueryExecutor(graphQLSchemaHolder);
    }

    public static GraphQLQueryExecutor create(final GraphQLSchemaHolder graphQLSchemaHolder, final ExecutionStrategy executionStrategy) {
        return new GraphQLQueryExecutor(graphQLSchemaHolder, executionStrategy);
    }

    public GraphQLQueryExecutor query(final String requestQuery) {
        this.requestQuery = requestQuery;
        return this;
    }

    public GraphQLQueryExecutor operation(final String operationName) {
        this.operationName = operationName;
        return this;
    }

    public GraphQLQueryExecutor context(final Object context) {
        this.context = context;
        return this;
    }

    public GraphQLQueryExecutor arguments(final Map<String, Object> arguments) {
        this.arguments = arguments;
        return this;
    }

    public GraphQLQueryExecutor maxQueryComplexity(int maxQueryComplexity) {
        this.maxQueryComplexity = maxQueryComplexity;
        return this;
    }

    public GraphQLQueryExecutor maxQueryDepth(final int maxQueryDepth) {
        this.maxQueryDepth = maxQueryDepth;
        return this;
    }

    public GraphQLQueryExecutor executorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    public GraphQLQueryExecutor forkJoinExecutorService(int parallelism) {
        this.executorService = Executors.newWorkStealingPool(parallelism);
        return this;
    }

    public GraphQLQueryExecutor forkJoinExecutorService() {
        this.executorService = Executors.newWorkStealingPool();
        return this;
    }

    public <T extends ExecutionResult> T execute() {

        assertNotNull(arguments, "Arguments can't be null");
        LOGGER.info("Executing request. Operation name: {}. Request: {} ", operationName, requestQuery);

        Parser parser = new Parser();
        Document document;
        try {
            document = parser.parseDocument(requestQuery);
        } catch (ParseCancellationException e) {
            RecognitionException recognitionException = (RecognitionException) e.getCause();
            SourceLocation sourceLocation = new SourceLocation(recognitionException.getOffendingToken().getLine(), recognitionException.getOffendingToken().getCharPositionInLine());
            InvalidSyntaxError invalidSyntaxError = new InvalidSyntaxError(sourceLocation);
            return (T) new GraphQLRxExecutionResult(Observable.just(null), Observable.just(Arrays.asList(invalidSyntaxError)));
        }

        Validator validator = new Validator();
        List<ValidationError> validationErrors = validator.validateDocument(graphQLSchemaHolder.getGraphQLSchema(), document);
        if (validationErrors.size() > 0) {
            return (T) new GraphQLRxExecutionResult(Observable.just(null), Observable.just(validationErrors));
        }

        if (executionStrategy == null) {
            if (executorService == null) {
                executionStrategy = new GraphQLDefaultRxExecutionStrategy(graphQLSchemaHolder, maxQueryDepth, maxQueryComplexity);
            } else {
                executionStrategy = new GraphQLExecutorServiceRxExecutionStrategy(graphQLSchemaHolder, executorService, maxQueryDepth, maxQueryComplexity);
            }
        }

        RxExecution execution = new RxExecution(graphQLSchemaHolder, maxQueryDepth, maxQueryComplexity, executionStrategy);
        ExecutionResult executionResult = execution.execute(graphQLSchemaHolder.getGraphQLSchema(), context, document, operationName, arguments);

        return (T) (executionResult instanceof GraphQLRxExecutionResult ?
                executionResult : new GraphQLRxExecutionResult(Observable.just(executionResult.getData()), Observable.just(executionResult.getErrors())));
    }

}
