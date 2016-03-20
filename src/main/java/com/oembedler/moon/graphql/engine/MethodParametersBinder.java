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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oembedler.moon.graphql.engine.dfs.GraphQLMethodParameters;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class MethodParametersBinder {

    private final ObjectMapper objectMapper;
    private final GraphQLMethodParameters graphQLMethodParameters;

    public MethodParametersBinder(Method targetMethod, Class<?> implClass) {
        this(new GraphQLMethodParameters(targetMethod, implClass));
    }

    public MethodParametersBinder(GraphQLMethodParameters graphQLMethodParameters) {
        this.graphQLMethodParameters = graphQLMethodParameters;
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Object[] bindParameters(Map<String, Object> parametersValues) {
        return bindAllParameters(parametersValues, null);
    }

    public Object[] bindParameters(Map<String, Object> parametersValues, Object[] bindByClassValues) {
        return bindAllParameters(parametersValues, bindByClassValues);
    }

    protected Object[] bindAllParameters(Map<String, Object> parametersValues, Object[] bindByClassValues) {
        if (getGraphQLMethodParameters().hasNoParameters())
            return null;

        final Object[] inputArgs = new Object[getGraphQLMethodParameters().getNumberOfParameters()];
        getGraphQLMethodParameters().getParameters().forEach(mpi ->
        {
            Object toMethodInputObject = null;
            Class<?> parameterType = mpi.isCollectionLike() ? mpi.getRawType() : mpi.getParameterType();
            if (mpi.isValidGraphQLInParameter()) {
                Object inputObject = parametersValues.get(mpi.getName());

                toMethodInputObject = getObjectMapper().convertValue(inputObject, parameterType);
            } else {
                // object is not registered as input parameter so trying to bind by class
                if (bindByClassValues != null) {
                    for (Object value : bindByClassValues) {
                        if (value != null && value.getClass().isAssignableFrom(parameterType)) {
                            toMethodInputObject = value;
                            break;
                        }
                    }
                }
            }
            inputArgs[mpi.getIdx()] = toMethodInputObject;
        });

        return inputArgs;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public GraphQLMethodParameters getGraphQLMethodParameters() {
        return graphQLMethodParameters;
    }
}
