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
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class ReflectionGraphQLDataMutator implements DataFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionGraphQLDataMutator.class);

    private final Object targetObject;
    private final Method targetMethod;
    private final GraphQLSchemaConfig graphQLSchemaConfig;
    private final GraphQLMethodParameters graphQLMethodParameters;
    private final MethodParametersBinder methodParametersBinder;

    // ---

    public static final class DataMutatorRuntimeException extends NestedRuntimeException {
        public DataMutatorRuntimeException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    // ---

    public ReflectionGraphQLDataMutator(GraphQLSchemaConfig graphQLSchemaConfig, Object targetObject, Method targetMethod) {
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
            Object[] inputArguments = getMethodParametersBinder().bindParameters(unwrapInputArguments(environment), bindByClassValues);
            targetMethodResult = getTargetMethod().invoke(getTargetObject(), inputArguments);

            targetMethodResult = afterInvocation(environment, targetMethodResult);

        } catch (Exception e) {
            String msg = "Exception while calling data fetcher [" + getTargetMethod().getName() + "]";
            if (LOGGER.isErrorEnabled())
                LOGGER.error(msg, e);
            throw new DataMutatorRuntimeException(msg, e);
        }

        return targetMethodResult;
    }

    protected void beforeInvocation(DataFetchingEnvironment environment) {
    }

    protected Object afterInvocation(DataFetchingEnvironment environment, Object targetMethodResult) {

        Map<String, Object> inputMap = unwrapInputArguments(environment);

        Map<String, Object> outputMap = new HashMap<>();
        outputMap.put(getGraphQLMethodParameters().getReturnTypeName(), targetMethodResult);
        injectClientMutationIdIfRequired(inputMap, outputMap);

        return outputMap;
    }

    public Object[] collectBindByClassValues(DataFetchingEnvironment environment) {
        Object[] bindByClassValues = new Object[]{environment.getSource(), environment.getContext()};
        return bindByClassValues;
    }

    protected Map<String, Object> unwrapInputArguments(DataFetchingEnvironment environment) {
        Map<String, Object> inputObject = null;
        String inArgName = getGraphQLSchemaConfig().getMutationInputArgumentName();
        if (environment.getArguments() != null && (environment.getArguments() instanceof Map)) {
            inputObject = (Map<String, Object>) environment.getArguments().get(inArgName);
        }
        return inputObject;
    }

    private void injectClientMutationIdIfRequired(Map<String, Object> inputMap, Map<String, Object> outputMap) {
        if (getGraphQLSchemaConfig().isInjectClientMutationId()) {
            String clientMutationId = (String) inputMap.get(getGraphQLSchemaConfig().getClientMutationIdName());
            outputMap.put(getGraphQLSchemaConfig().getClientMutationIdName(), clientMutationId);
        }
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

    public GraphQLSchemaConfig getGraphQLSchemaConfig() {
        return graphQLSchemaConfig;
    }
}
