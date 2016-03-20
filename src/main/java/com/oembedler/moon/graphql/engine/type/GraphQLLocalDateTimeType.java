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

package com.oembedler.moon.graphql.engine.type;

import graphql.GraphQLException;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class GraphQLLocalDateTimeType extends GraphQLScalarType {

    public GraphQLLocalDateTimeType(String name, String description, String dateFormat) {
        super(name, description, new Coercing() {
            private final TimeZone timeZone = TimeZone.getTimeZone("UTC");

            @Override
            public Object serialize(Object input) {
                if (input instanceof String) {
                    return parse((String) input);
                } else if (input instanceof LocalDateTime) {
                    return format((LocalDateTime) input);
                } else if (input instanceof Long) {
                    return LocalDateTime.ofEpochSecond((Long) input, 0, ZoneOffset.UTC);
                } else if (input instanceof Integer) {
                    return LocalDateTime.ofEpochSecond((((Integer) input).longValue()), 0, ZoneOffset.UTC);
                } else {
                    throw new GraphQLException("Wrong timestamp value");
                }
            }

            @Override
            public Object parseValue(Object input) {
                return serialize(input);
            }

            @Override
            public Object parseLiteral(Object input) {
                if (!(input instanceof StringValue)) return null;
                return parse(((StringValue) input).getValue());
            }

            private String format(LocalDateTime input) {
                return getDateTimeFormatter().format(input);
            }

            private LocalDateTime parse(String input) {
                LocalDateTime date = null;
                try {
                    date = LocalDateTime.parse(input, getDateTimeFormatter());
                } catch (Exception e) {
                    throw new GraphQLException("Can not parse input date", e);
                }
                return date;
            }

            private DateTimeFormatter getDateTimeFormatter() {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
                return formatter;
            }
        });
        Assert.notNull(dateFormat, "Date format must not be null");
    }

}
