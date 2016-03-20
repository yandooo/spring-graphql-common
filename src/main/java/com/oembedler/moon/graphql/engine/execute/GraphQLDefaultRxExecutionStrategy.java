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
import java.util.List;
import java.util.Map;

/**
 * Idea was borrowed from <a href="https://github.com/nfl/graphql-rxjava"></a>
 *
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
class GraphQLDefaultRxExecutionStrategy extends GraphQLAbstractRxExecutionStrategy {

    public GraphQLDefaultRxExecutionStrategy(GraphQLSchemaHolder graphQLSchemaHolder, int maxQueryDepth, int maxQueryComplexity) {
        super(graphQLSchemaHolder, maxQueryDepth, maxQueryComplexity);
    }

    public ExecutionResult doExecute(ExecutionContext executionContext, GraphQLObjectType parentType, Object source, Map<String, List<Field>> fields) {

        List<Observable<Pair<String, Object>>> observablesResult = new ArrayList<>();
        List<Observable<Double>> observablesComplexity = new ArrayList<>();
        for (String fieldName : fields.keySet()) {
            final List<Field> fieldList = fields.get(fieldName);

            ExecutionResult executionResult = resolveField(executionContext, parentType, source, fieldList);
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

    protected Observable<Pair<String, Object>> unwrapExecutionResult(String fieldName, ExecutionResult executionResult) {
        Observable<Pair<String, Object>> result;
        if (executionResult instanceof GraphQLRxExecutionResult) {
            GraphQLRxExecutionResult rxResult = (GraphQLRxExecutionResult) executionResult;
            Observable<?> unwrappedResult = rxResult.getDataObservable().flatMap(potentialResult -> {
                if (potentialResult instanceof GraphQLRxExecutionResult) {
                    return ((GraphQLRxExecutionResult) potentialResult).getDataObservable();
                }
                return Observable.just(potentialResult);
            });

            result = Observable.zip(Observable.just(fieldName), unwrappedResult, Pair::of);
        } else {
            result = Observable.just(Pair.of(fieldName, executionResult != null ? executionResult.getData() : null));
        }
        return result;
    }

}
