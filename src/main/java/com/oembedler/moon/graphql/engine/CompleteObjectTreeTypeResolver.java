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

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.TypeResolver;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class CompleteObjectTreeTypeResolver implements TypeResolver {

    private ConcurrentHashMap<Class<?>, GraphQLType>
            objectTypeResolverMap = new ConcurrentHashMap<Class<?>, GraphQLType>();

    public CompleteObjectTreeTypeResolver(ConcurrentHashMap<Class<?>, GraphQLType> objectTypeResolverMap) {
        this.objectTypeResolverMap = objectTypeResolverMap;
    }

    @Override
    public GraphQLObjectType getType(Object object) {
        Class<?> cls = object.getClass();
        return (GraphQLObjectType) objectTypeResolverMap.get(cls);
    }
}
