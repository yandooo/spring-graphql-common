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

import graphql.ExecutionResult;
import graphql.GraphQLError;
import rx.Observable;

import java.util.List;

/**
 * Idea was borrowed from <a href="https://github.com/nfl/graphql-rxjava"></a>
 *
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class GraphQLRxExecutionResult implements ExecutionResult {

    private Observable<?> dataObservable;
    private Observable<List<? extends GraphQLError>> errorsObservable;
    private Observable<Double> queryComplexity;

    public GraphQLRxExecutionResult(Observable<?> data, Observable<List<? extends GraphQLError>> errors, Observable<Double> complexity) {
        dataObservable = data;
        errorsObservable = errors;
        queryComplexity = complexity;
    }

    public GraphQLRxExecutionResult(Observable<?> data, Observable<List<? extends GraphQLError>> errors) {
        dataObservable = data;
        errorsObservable = errors;
        queryComplexity = Observable.just(0.0);

    }

    public GraphQLRxExecutionResult(Observable<List<? extends GraphQLError>> errors) {
        this.errorsObservable = errors;
    }

    public Observable<?> getDataObservable() {
        return dataObservable;
    }

    public Observable<List<? extends GraphQLError>> getErrorsObservable() {
        return errorsObservable;
    }

    public Observable<Double> getComplexityObservable() {
        return queryComplexity;
    }

    public Double getComplexity() {
        return queryComplexity.toBlocking().first();
    }

    @Override
    public Object getData() {
        return dataObservable.toBlocking().first();
    }

    @Override
    public List<GraphQLError> getErrors() {
        return (List<GraphQLError>) errorsObservable.toBlocking().first();
    }
}
