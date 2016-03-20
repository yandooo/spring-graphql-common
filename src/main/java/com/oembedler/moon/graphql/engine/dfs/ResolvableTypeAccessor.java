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
import com.oembedler.moon.graphql.engine.stereotype.*;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class ResolvableTypeAccessor {

    private final String name;
    private final Class<?> implClass;
    private final ResolvableType resolvableType;
    private final List<Annotation> annotations;

    public ResolvableTypeAccessor(String name, ResolvableType resolvableType, List<Annotation> annotations, Class<?> implClass) {
        this.implClass = implClass;
        this.resolvableType = resolvableType;
        this.annotations = annotations;
        this.name = name;
    }

    public static ResolvableTypeAccessor forClass(Class<?> rawClass) {
        return new ResolvableTypeAccessor(
                rawClass.getSimpleName(),
                null,
                Lists.newArrayList(rawClass.getAnnotations()),
                rawClass);
    }

    public static ResolvableTypeAccessor forField(Field field, Class<?> implClass) {
        ResolvableType resolvableType = ResolvableType.forField(field, implClass);
        return new ResolvableTypeAccessor(
                field.getName(),
                resolvableType,
                Lists.newArrayList(field.getAnnotations()),
                implClass);
    }

    public static ResolvableTypeAccessor forEnumField(Enum en) {
        Field field = ReflectionUtils.findField(en.getClass(), ((Enum) en).name());
        return new ResolvableTypeAccessor(
                en.toString(),
                null,
                Lists.newArrayList(field.getAnnotations()),
                en.getClass());
    }

    public static ResolvableTypeAccessor forMethodReturnType(Method method, Class<?> implClass) {
        ResolvableType resolvableType = ResolvableType.forMethodReturnType(method, implClass);
        return new ResolvableTypeAccessor(
                method.getName(),
                resolvableType,
                Lists.newArrayList(method.getAnnotations()),
                implClass);
    }

    public static ResolvableTypeAccessor forMethodParameter(Method method, int argIndex, Class<?> implClass) {
        MethodParameter methodParameter = new MethodParameter(method, argIndex);
        ResolvableType resolvableType = ResolvableType.forMethodParameter(method, argIndex, implClass);
        return new ResolvableTypeAccessor(
                "",
                resolvableType,
                Lists.newArrayList(methodParameter.getParameterAnnotations()),
                implClass);
    }

    private ResolvableType getResolvableType() {
        return resolvableType;
    }

    public Class<?> resolve() {
        return getResolvableType().resolve();
    }

    public Type getComponentType() {
        return getResolvableType().getComponentType().resolve();
    }

    public boolean isArrayType() {
        Class<?> rawCls = resolve();
        return rawCls.isArray() || Iterable.class.equals(rawCls);
    }

    public boolean isCollectionType() {
        Class<?> rawCls = resolve();
        return Collection.class.isAssignableFrom(rawCls);
    }

    public boolean isCollectionLike() {
        return isArrayType() || isCollectionType();
    }

    private <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T foundAnnotation = null;
        for (Annotation itAnnotation : annotations) {
            if (annotationClass.isAssignableFrom(itAnnotation.getClass())) {
                foundAnnotation = (T) itAnnotation;
                break;
            }
        }
        return foundAnnotation;
    }

    private <T extends Annotation, V> V getAnnotationValue(Class<T> annotationClass, V fallback) {
        V result = fallback;
        T annotation = getAnnotation(annotationClass);
        if (annotation != null && StringUtils.hasText((String) AnnotationUtils.getValue(annotation)))
            result = (V) AnnotationUtils.getValue(annotation);
        return result;
    }

    private <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    public Class<?> getImplClass() {
        return implClass;
    }

    public boolean isGraphQLIdOrGraphQLField() {
        return isGraphQLId() || isGraphQLField();
    }

    public boolean isGraphQLField() {
        return isAnnotationPresent(GraphQLField.class);
    }

    public boolean isGraphQLId() {
        return isAnnotationPresent(GraphQLID.class);
    }

    public boolean isNotNull() {
        return isAnnotationPresent(GraphQLNonNull.class);
    }

    public boolean isIgnorable() {
        return isAnnotationPresent(GraphQLIgnore.class);
    }

    public boolean isNotIgnorable() {
        return !isIgnorable();
    }

    public boolean isGraphQLInterface() {
        return implClass.isInterface() && isAnnotationPresent(GraphQLInterface.class);
    }

    public boolean isGraphQLUnion() {
        return implClass.isInterface() && isAnnotationPresent(GraphQLUnion.class);
    }

    public Class<?>[] getGraphQLUnionPossibleTypes() {
        GraphQLUnion graphQLUnion = getAnnotation(GraphQLUnion.class);
        return graphQLUnion != null ? graphQLUnion.possibleTypes() : new Class<?>[]{};
    }

    public String getName() {
        String result = getAnnotationValue(GraphQLField.class, this.name);
        result = getAnnotationValue(GraphQLID.class, result);
        result = getAnnotationValue(GraphQLMutation.class, result);
        result = getAnnotationValue(GraphQLObject.class, result);
        result = getAnnotationValue(GraphQLInterface.class, result);
        result = getAnnotationValue(GraphQLUnion.class, result);
        result = getAnnotationValue(GraphQLEnum.class, result);

        return result;
    }

    public String getGraphQLOutName() {
        String result = getAnnotationValue(GraphQLOut.class, this.name);
        return result;
    }

    public String getGraphQLInName() {
        String result = getAnnotationValue(GraphQLIn.class, "");
        return result;
    }

    public String getGraphQLDeprecationReason() {
        String result = getAnnotationValue(GraphQLDeprecate.class, null);
        return result;
    }

    public String getGraphQLComplexitySpelExpression() {
        String result = getAnnotationValue(GraphQLComplexity.class, null);
        return result;
    }

    public boolean hasGraphQLComplexity() {
        return StringUtils.hasText(getGraphQLComplexitySpelExpression());
    }

    public boolean hasGraphQLDeprecationReason() {
        return StringUtils.hasText(getGraphQLDeprecationReason());
    }

    public boolean isGraphQLInParameter() {
        return StringUtils.hasText(getGraphQLInName());
    }

    public boolean isRequiredGraphQLInParameter() {
        GraphQLIn graphQLIn = getAnnotation(GraphQLIn.class);
        boolean isRequired = false;
        if (graphQLIn != null) {
            isRequired = graphQLIn.required();
        }
        return isRequired;
    }

    public String getGraphQLInDefaultValueProviderMethodName() {
        GraphQLIn graphQLIn = getAnnotation(GraphQLIn.class);
        String defaultValueProviderMethod = "";
        if (graphQLIn != null) {
            defaultValueProviderMethod = graphQLIn.defaultProvider();
        }
        return defaultValueProviderMethod;
    }

    public String getGraphQLEnumValueProviderMethodName() {
        GraphQLEnum graphQLIn = getAnnotation(GraphQLEnum.class);
        String defaultValueProviderMethod = "";
        if (graphQLIn != null) {
            defaultValueProviderMethod = graphQLIn.valueProvider();
        }
        return defaultValueProviderMethod;
    }

    public String getGraphQLInDefaultValueSpel() {
        GraphQLIn graphQLIn = getAnnotation(GraphQLIn.class);
        String GraphQLInValueSpel = "";
        if (graphQLIn != null) {
            GraphQLInValueSpel = graphQLIn.defaultSpel();
        }
        return GraphQLInValueSpel;
    }

    public String getGraphQLEnumDefaultValueSpel() {
        GraphQLEnum graphQLIn = getAnnotation(GraphQLEnum.class);
        String defaultValueSpel = "";
        if (graphQLIn != null) {
            defaultValueSpel = graphQLIn.defaultSpel();
        }
        return defaultValueSpel;
    }

    public boolean isGraphQLMutation() {
        return isAnnotationPresent(GraphQLMutation.class);
    }

    public String getDescription() {
        return getAnnotationValue(GraphQLDescription.class, this.name);
    }

    public Class<?> getActualType() {
        Class<?> type = resolve();

        if (isArrayType()) {
            type = (Class<?>) getComponentType();
        }

        if (isCollectionType()) {
            type = resolvableType.getGeneric(0).resolve();
        }

        return type;
    }
}
