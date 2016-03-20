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

package com.oembedler.moon.graphql.test.simpleschema;

import com.oembedler.moon.graphql.engine.stereotype.*;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
@GraphQLSchema
public class ComputationDelaySchema {

    private static final long DELAY_MS = 2000;

    @GraphQLSchemaQuery
    private QueryType queryType;

    @GraphQLInterface
    public interface Marker {

        @GraphQLNonNull
        @GraphQLField("id")
        String getId() throws InterruptedException;
    }

    public static class BaseMarker implements Marker {
        @GraphQLIgnore
        private String id;

        @Override
        @GraphQLNonNull
        @GraphQLField("id")
        public String getId() throws InterruptedException {
            Thread.sleep(DELAY_MS);
            return null;
        }
    }

    @GraphQLObject
    public static class ColorMarker extends BaseMarker {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @GraphQLObject
    public static class DynamicMarker extends BaseMarker {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @GraphQLObject
    public static class QueryType {

        @GraphQLField
        public ColorMarker color(@GraphQLIn("id") String id) throws InterruptedException {
            Thread.sleep(DELAY_MS);
            return null;
        }

        @GraphQLField
        public DynamicMarker dynamic(@GraphQLNonNull @GraphQLIn("id") String id) throws InterruptedException {
            Thread.sleep(DELAY_MS);
            return null;
        }

        @GraphQLField
        public Marker any(@GraphQLNonNull @GraphQLIn("id") String id) throws InterruptedException {
            Thread.sleep(DELAY_MS);
            return null;
        }

    }
}

