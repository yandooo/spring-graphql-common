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

import com.oembedler.moon.graphql.engine.stereotype.GraphQLSchema;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public abstract class GraphQLSchemaDiscoverer {

    private static final Class<? extends Annotation> GRAPH_QL_SCHEMA_ANNOTATION = GraphQLSchema.class;

    // ---

    public static Set<Class<?>> findSchemaClasses(final String basePackage) throws ClassNotFoundException {
        
        Set<Class<?>> initialEntitySet = new HashSet<Class<?>>();

        if (StringUtils.hasText(basePackage)) {
            ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(
                    false);
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(GRAPH_QL_SCHEMA_ANNOTATION));

            for (BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {
                initialEntitySet.add(ClassUtils.forName(candidate.getBeanClassName(),
                        GraphQLSchemaDiscoverer.class.getClassLoader()));
            }
        }
        return initialEntitySet;
    }

}
