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

package com.oembedler.moon.graphql.engine.dfs;

import com.google.common.collect.Lists;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public abstract class MethodParameters {

    private final Method targetMethod;
    private final Class<?> implClass;
    private final String[] autoDiscoveredNames;
    private MethodParameterInfo[] methodParameterInfos;
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    // ---

    public static class MethodParameterInfo {
        private final int idx;
        private final String name;
        private final boolean isRequired;
        private final ResolvableTypeAccessor resolvableTypeAccessor;

        public MethodParameterInfo(int idx,
                                   String name,
                                   ResolvableTypeAccessor resolvableTypeAccessor) {
            this.idx = idx;
            this.name = name;
            this.resolvableTypeAccessor = resolvableTypeAccessor;
            this.isRequired = resolvableTypeAccessor.isRequiredGraphQLInParameter();
        }

        public int getIdx() {
            return idx;
        }

        public String getName() {
            return name;
        }

        public ResolvableTypeAccessor getResolvableTypeAccessor() {
            return resolvableTypeAccessor;
        }

        public Class<?> getRawType() {
            return getResolvableTypeAccessor().resolve();
        }

        public String getDescription() {
            return getResolvableTypeAccessor().getDescription();
        }

        public Class<?> getParameterType() {
            return getResolvableTypeAccessor().getActualType();
        }

        public boolean isCollectionLike() {
            return getResolvableTypeAccessor().isCollectionLike();
        }

        public boolean isRequired() {
            return isRequired;
        }

        public boolean isGraphQLInParameter() {
            return getResolvableTypeAccessor().isGraphQLInParameter();
        }

        public String getGraphQLInDefaultValueProviderMethodName() {
            return getResolvableTypeAccessor().getGraphQLInDefaultValueProviderMethodName();
        }

        public String getGraphQLInDefaultValueSpel() {
            return getResolvableTypeAccessor().getGraphQLInDefaultValueSpel();
        }

        public boolean isValidGraphQLInParameter() {
            return StringUtils.hasText(getName()) && isGraphQLInParameter();
        }
    }

    // ---

    public MethodParameters(Method targetMethod, Class<?> implClass) {
        this.targetMethod = targetMethod;
        this.implClass = implClass;
        this.autoDiscoveredNames = parameterNameDiscoverer.getParameterNames(this.targetMethod);
        discoverMethodParameters();
    }

    public void discoverMethodParameters() {
        if (methodParameterInfos == null) {
            methodParameterInfos = new MethodParameterInfo[targetMethod.getParameterCount()];
            for (int i = 0; i < targetMethod.getParameterCount(); i++) {
                MethodParameter methodParameter = new MethodParameter(targetMethod, i);
                String inputParameterName = getParameterName(i, methodParameter);

                if (StringUtils.isEmpty(inputParameterName)) {
                    inputParameterName = getAutoDiscoveredName(i);
                }

                ResolvableTypeAccessor resolvableTypeAccessor =
                        ResolvableTypeAccessor.forMethodParameter(targetMethod, i, implClass);

                methodParameterInfos[i] = new MethodParameterInfo(i, inputParameterName, resolvableTypeAccessor);
            }
        }
    }

    public List<MethodParameterInfo> getParameters() {
        return methodParameterInfos == null ? Collections.EMPTY_LIST : Lists.newArrayList(methodParameterInfos);
    }

    public int getNumberOfParameters() {
        return methodParameterInfos == null ? 0 : methodParameterInfos.length;
    }

    public boolean isCompatibleWith(int argIdx, Class<?> cls) {
        return methodParameterInfos[argIdx].getParameterType().isAssignableFrom(cls);
    }

    private String getAutoDiscoveredName(int idx) {
        return (autoDiscoveredNames != null && autoDiscoveredNames.length > 0 && idx < autoDiscoveredNames.length) ?
                autoDiscoveredNames[idx] : null;
    }

    protected abstract String getParameterName(int idx, MethodParameter methodParameter);

    public boolean hasParameters() {
        return methodParameterInfos != null && methodParameterInfos.length > 0;
    }

    public boolean hasNoParameters() {
        return !hasParameters();
    }
}
