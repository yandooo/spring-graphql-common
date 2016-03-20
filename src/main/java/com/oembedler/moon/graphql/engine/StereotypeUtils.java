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

import com.oembedler.moon.graphql.engine.stereotype.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class StereotypeUtils {

    public static String getGraphQLSchemaName(AnnotatedElement accessibleObject, String defaultValue) {
        return getAnnotationValue(accessibleObject, GraphQLSchema.class, defaultValue);
    }

    public static String getGraphQLObjectName(AnnotatedElement accessibleObject, String defaultValue) {
        return getAnnotationValue(accessibleObject, GraphQLObject.class, defaultValue);
    }

    public static String getGraphQLInterfaceName(AnnotatedElement accessibleObject, String defaultValue) {
        return getAnnotationValue(accessibleObject, GraphQLInterface.class, defaultValue);
    }

    public static String getGraphQLUnionName(AnnotatedElement accessibleObject, String defaultValue) {
        return getAnnotationValue(accessibleObject, GraphQLUnion.class, defaultValue);
    }

    public static String getGraphQLDescription(AnnotatedElement accessibleObject, String defaultValue) {
        return getAnnotationValue(accessibleObject, GraphQLDescription.class, defaultValue);
    }

    public static String getGraphQLMutationName(AnnotatedElement accessibleObject, String defaultValue) {
        return getAnnotationValue(accessibleObject, GraphQLMutation.class, defaultValue);
    }

    private static <T extends Annotation> String getAnnotationValue(AnnotatedElement accessibleObject, Class<T> annotationClass, String defaultValue) {
        Assert.noNullElements(new Object[]{accessibleObject, annotationClass, defaultValue}, "input parameters must not be null");

        String result = defaultValue;
        T annotation = accessibleObject.getAnnotation(annotationClass);
        if (annotation != null && StringUtils.hasText((String) AnnotationUtils.getValue(annotation)))
            result = (String) AnnotationUtils.getValue(annotation);

        return result;
    }

}
