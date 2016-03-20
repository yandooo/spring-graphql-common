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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class GraphQLDateType extends GraphQLScalarType {

    public GraphQLDateType(String name, String description, String dateFormat) {
        super(name, description, new Coercing() {
            private final TimeZone timeZone = TimeZone.getTimeZone("UTC");

            @Override
            public Object serialize(Object input) {
                if (input instanceof String) {
                    return parse((String) input);
                } else if (input instanceof Date) {
                    return format((Date) input);
                } else if (input instanceof Long) {
                    return new Date((Long) input);
                } else if (input instanceof Integer) {
                    return new Date(((Integer) input).longValue());
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

            private String format(Date input) {
                return getSimpleDateFormat().format(input.getTime());
            }

            private Date parse(String input) {
                Date date = null;
                try {
                    date = getSimpleDateFormat().parse(input);
                } catch (Exception e) {
                    throw new GraphQLException("Can not parse input date", e);
                }
                return date;
            }

            private SimpleDateFormat getSimpleDateFormat() {
                SimpleDateFormat df = new SimpleDateFormat(dateFormat);
                df.setTimeZone(timeZone);
                return df;
            }
        });
        Assert.notNull(dateFormat, "Date format must not be null");
    }

}
