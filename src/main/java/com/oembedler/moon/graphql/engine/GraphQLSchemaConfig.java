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

import org.springframework.util.Assert;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
public class GraphQLSchemaConfig {

    // ---

    private String clientMutationIdName = "clientMutationId";
    private boolean injectClientMutationId = true;
    private boolean allowEmptyClientMutationId = false;
    private String mutationInputArgumentName = "input";
    private String outputObjectNamePrefix = "Payload";
    private String inputObjectNamePrefix = "Input";
    private String schemaMutationObjectName = "Mutation";
    private boolean dateAsTimestamp = true;
    private String dateFormat = "yyyy-MM-dd'T'HH:mm'Z'";

    // ---

    public String getClientMutationIdName() {
        return clientMutationIdName;
    }

    public GraphQLSchemaConfig setClientMutationIdName(String clientMutationIdName) {
        Assert.hasText(clientMutationIdName, "Client mutation identity value can not be null!");
        this.clientMutationIdName = clientMutationIdName;
        return this;
    }

    public boolean isInjectClientMutationId() {
        return injectClientMutationId;
    }

    public GraphQLSchemaConfig setInjectClientMutationId(boolean injectClientMutationId) {
        this.injectClientMutationId = injectClientMutationId;
        return this;
    }

    public boolean isAllowEmptyClientMutationId() {
        return allowEmptyClientMutationId;
    }

    public GraphQLSchemaConfig setAllowEmptyClientMutationId(boolean allowEmptyClientMutationId) {
        this.allowEmptyClientMutationId = allowEmptyClientMutationId;
        return this;
    }

    public String getMutationInputArgumentName() {
        return mutationInputArgumentName;
    }

    public GraphQLSchemaConfig setMutationInputArgumentName(String mutationInputArgumentName) {
        Assert.notNull(mutationInputArgumentName, "Mutation input argument value can not be null!");
        this.mutationInputArgumentName = mutationInputArgumentName;
        return this;
    }

    public String getOutputObjectNamePrefix() {
        return outputObjectNamePrefix;
    }

    public GraphQLSchemaConfig setOutputObjectNamePrefix(String outputObjectNamePrefix) {
        Assert.notNull(outputObjectNamePrefix, "Output object value can not be null!");
        this.outputObjectNamePrefix = outputObjectNamePrefix;
        return this;
    }

    public String getInputObjectNamePrefix() {
        return inputObjectNamePrefix;
    }

    public GraphQLSchemaConfig setInputObjectNamePrefix(String inputObjectNamePrefix) {
        Assert.notNull(inputObjectNamePrefix, "Input object value can not be null!");
        this.inputObjectNamePrefix = inputObjectNamePrefix;
        return this;
    }

    public String getSchemaMutationObjectName() {
        return schemaMutationObjectName;
    }

    public GraphQLSchemaConfig setSchemaMutationObjectName(String schemaMutationObjectName) {
        Assert.notNull(schemaMutationObjectName, "Schema mutation object value can not be null!");
        this.schemaMutationObjectName = schemaMutationObjectName;
        return this;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        Assert.notNull(dateFormat, "Date format string can not be null!");
        this.dateFormat = dateFormat;
    }

    public boolean isDateAsTimestamp() {
        return dateAsTimestamp;
    }

    public void setDateAsTimestamp(boolean dateAsTimestamp) {
        this.dateAsTimestamp = dateAsTimestamp;
    }
}
