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

import graphql.schema.GraphQLFieldDefinition;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class GraphQLFieldDefinitionWrapper {

    private static final ExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

    private final GraphQLFieldDefinition graphQLFieldDefinition;
    private final String complexitySpelExpressionString;
    private final Expression complexitySpelExpression;

    public GraphQLFieldDefinitionWrapper(GraphQLFieldDefinition graphQLFieldDefinition, String complexitySpelExpressionString) {
        this.graphQLFieldDefinition = graphQLFieldDefinition;
        this.complexitySpelExpressionString = complexitySpelExpressionString;
        this.complexitySpelExpression = StringUtils.hasText(this.complexitySpelExpressionString) ?
                SPEL_EXPRESSION_PARSER.parseExpression(this.complexitySpelExpressionString) : null;
    }

    public GraphQLFieldDefinition getGraphQLFieldDefinition() {
        return graphQLFieldDefinition;
    }

    public String getComplexitySpelExpressionString() {
        return complexitySpelExpressionString;
    }

    public Expression getComplexitySpelExpression() {
        return complexitySpelExpression;
    }
}
