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
import graphql.execution.ExecutionContext;
import graphql.language.Field;
import graphql.schema.GraphQLObjectType;
import org.apache.commons.lang3.tuple.Pair;
import rx.Observable;
import rx.observables.MathObservable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RecursiveTask;

/**
 * Idea was borrowed from <a href="https://github.com/nfl/graphql-rxjava"></a>
 *
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
class GraphQLExecutorServiceRxExecutionStrategy extends GraphQLDefaultRxExecutionStrategy {

    private final ExecutorService executorService;

    public GraphQLExecutorServiceRxExecutionStrategy(GraphQLSchemaHolder graphQLSchemaHolder, ExecutorService executorService, int maxQueryDepth, int maxQueryComplexity) {
        super(graphQLSchemaHolder, maxQueryDepth, maxQueryComplexity);
        this.executorService = executorService;
    }

    public ExecutionResult doExecute(ExecutionContext executionContext, GraphQLObjectType parentType, Object source, Map<String, List<Field>> fields) {

        Map<String, RecursiveTask<ExecutionResult>> recursiveTaskMap = new LinkedHashMap<String, RecursiveTask<ExecutionResult>>();
        for (String fieldName : fields.keySet()) {
            final List<Field> fieldList = fields.get(fieldName);
            RecursiveTask<ExecutionResult> resolveField = new RecursiveTask<ExecutionResult>() {
                @Override
                protected ExecutionResult compute() {
                    return resolveField(executionContext, parentType, source, fieldList);
                }
            };
            resolveField.fork();
            recursiveTaskMap.put(fieldName, resolveField);
        }

        List<Observable<Pair<String, Object>>> observablesResult = new ArrayList<>();
        List<Observable<Double>> observablesComplexity = new ArrayList<>();

        for (String fieldName : recursiveTaskMap.keySet()) {
            List<Field> fieldList = fields.get(fieldName);
            ExecutionResult executionResult = recursiveTaskMap.get(fieldName).join();

            observablesResult.add(unwrapExecutionResult(fieldName, executionResult));
            observablesComplexity.add(calculateFieldComplexity(executionContext, parentType, fieldList,
                    executionResult != null ? ((GraphQLRxExecutionResult) executionResult).getComplexityObservable() : Observable.just(0.0)));

        }

        Observable<Map<String, Object>> result =
                Observable.merge(observablesResult)
                        .toMap(Pair::getLeft, Pair::getRight);

        GraphQLRxExecutionResult graphQLRxExecutionResult = new GraphQLRxExecutionResult(result, Observable.just(executionContext.getErrors()), MathObservable.sumDouble(Observable.merge(observablesComplexity)));
        return graphQLRxExecutionResult;
    }
}
