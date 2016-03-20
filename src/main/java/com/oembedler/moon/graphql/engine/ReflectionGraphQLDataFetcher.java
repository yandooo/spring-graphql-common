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

import com.oembedler.moon.graphql.engine.dfs.GraphQLMethodParameters;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class ReflectionGraphQLDataFetcher implements DataFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionGraphQLDataMutator.class);

    private final Object targetObject;
    private final Method targetMethod;
    private final GraphQLSchemaConfig graphQLSchemaConfig;
    private final GraphQLMethodParameters graphQLMethodParameters;
    private final MethodParametersBinder methodParametersBinder;

    // ---

    public static final class DataFetcherRuntimeException extends NestedRuntimeException {
        public DataFetcherRuntimeException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    // ---

    public ReflectionGraphQLDataFetcher(GraphQLSchemaConfig graphQLSchemaConfig, Object targetObject, Method targetMethod) {
        this.targetObject = targetObject;
        this.targetMethod = targetMethod;
        this.graphQLSchemaConfig = graphQLSchemaConfig;
        this.graphQLMethodParameters = new GraphQLMethodParameters(targetMethod, this.targetObject.getClass());
        this.methodParametersBinder = new MethodParametersBinder(this.graphQLMethodParameters);
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        Object targetMethodResult = null;
        try {

            beforeInvocation(environment);

            Object[] bindByClassValues = collectBindByClassValues(environment);
            Object[] inputArguments = getMethodParametersBinder().bindParameters(environment.getArguments(), bindByClassValues);
            if (isAllNulls(inputArguments) && canApplySourceObject(environment)) {
                inputArguments = new Object[]{environment.getSource()};
            }
            targetMethodResult = getTargetMethod().invoke(getTargetObject(), inputArguments);

            targetMethodResult = afterInvocation(environment, targetMethodResult);

        } catch (Exception e) {
            String msg = "Exception while calling data fetcher [" + getTargetMethod().getName() + "]";
            if (LOGGER.isErrorEnabled())
                LOGGER.error(msg, e);
            throw new DataFetcherRuntimeException(msg, e);
        }

        return targetMethodResult;
    }

    public Object[] collectBindByClassValues(DataFetchingEnvironment environment) {
        Object[] bindByClassValues = new Object[]{environment.getSource(), environment.getContext()};
        return bindByClassValues;
    }

    public static boolean isAllNulls(Object[] array) {
        if (array != null) {
            for (Object obj : array) {
                if (obj != null)
                    return false;
            }
        }
        return true;
    }

    protected boolean canApplySourceObject(DataFetchingEnvironment environment) {
        return environment.getSource() != null && getGraphQLMethodParameters().getNumberOfParameters() > 0?
                getGraphQLMethodParameters().isCompatibleWith(0, environment.getSource().getClass()) :
                false;
    }

    protected void beforeInvocation(DataFetchingEnvironment environment) {
    }

    protected Object afterInvocation(DataFetchingEnvironment environment, Object targetMethodResult) {
        return targetMethodResult;
    }

    public GraphQLMethodParameters getGraphQLMethodParameters() {
        return graphQLMethodParameters;
    }

    public MethodParametersBinder getMethodParametersBinder() {
        return methodParametersBinder;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public Object getTargetObject() {
        return targetObject;
    }
}
