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

package com.oembedler.moon.graphql.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oembedler.moon.graphql.GraphQLSchemaBeanFactory;
import com.oembedler.moon.graphql.SpringGraphQLSchemaBeanFactory;
import com.oembedler.moon.graphql.engine.GraphQLQueryTemplate;
import com.oembedler.moon.graphql.engine.GraphQLSchemaBuilder;
import com.oembedler.moon.graphql.engine.GraphQLSchemaConfig;
import com.oembedler.moon.graphql.engine.GraphQLSchemaHolder;
import com.oembedler.moon.graphql.engine.execute.GraphQLQueryExecutor;
import com.oembedler.moon.graphql.engine.execute.GraphQLRxExecutionResult;
import com.oembedler.moon.graphql.test.todoschema.TodoSchema;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
@ContextConfiguration(classes = GenericTodoSchemaParserTest.TodoSchemaConfiguration.class)
public class GenericTodoSchemaParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericTodoSchemaParserTest.class);

    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();
    @Rule
    public SpringMethodRule springMethodRule = new SpringMethodRule();

    private ObjectMapper mapper = new ObjectMapper();

    @Configuration
    @ComponentScan(basePackages = "com.oembedler.moon.graphql.test.todoschema")
    public static class TodoSchemaConfiguration {

        @Bean
        public GraphQLSchemaBeanFactory graphQLSchemaBeanFactory() {
            return new SpringGraphQLSchemaBeanFactory();
        }

        @Bean
        public GraphQLSchemaConfig graphQLSchemaConfig() {
            GraphQLSchemaConfig graphQLSchemaConfig = new GraphQLSchemaConfig();
            return graphQLSchemaConfig;
        }

        @Bean
        public GraphQLSchemaBuilder graphQLSchemaBuilder() {
            return new GraphQLSchemaBuilder(graphQLSchemaConfig(), graphQLSchemaBeanFactory());
        }

        @Bean
        public GraphQLSchemaHolder graphQLSchemaHolder() {
            return graphQLSchemaBuilder().buildSchema(TodoSchema.class);
        }
    }

    @Autowired
    public GraphQLSchemaBuilder graphQLSchemaBuilder;
    @Autowired
    public GraphQLSchemaHolder graphQLSchemaHolder;

    private Object prettifyPrint(Object input) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(input);
        } catch (JsonProcessingException e) {
            LOGGER.error("Prettifying error", e);
        }
        return input;
    }

    public String readClasspathResourceToString(final String resourceName) {
        String resourceAsString = null;

        ClassPathResource classPathResource = new ClassPathResource(resourceName);
        try (InputStream is = classPathResource.getInputStream()) {
            resourceAsString = StreamUtils.copyToString(is, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resourceAsString;
    }

    @Test
    public void introspectionQuery_Success() throws IOException {
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query(readClasspathResourceToString("introspection.query"))
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void viewerQuery_Success() throws IOException {
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query("{viewer{ id }}")
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void viewerQueryEstimateComplexity_Success() throws IOException {
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query("{viewer{ id }}")
                        .execute();
        LOGGER.info("Complexity: {}", result.getComplexity());
    }

    @Test
    public void relayNodeQueryEstimateComplexity_Success() throws IOException {
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query(readClasspathResourceToString("user-node.query"))
                        .execute();

        LOGGER.info("Complexity: {}", result.getComplexity());
    }

    @Test
    public void relayNodeQuery_Success() throws IOException {
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query(readClasspathResourceToString("user-node.query"))
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    // test graphql context to pass to executor
    public static class GraphQLContext<K, V> extends HashMap<K, V> {
    }

    @Test
    public void viewerTodosQuery_Success() throws IOException {
        GraphQLContext<String, Object> graphQLContext = new GraphQLContext<>();
        graphQLContext.put("Key1", "value1");
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query(readClasspathResourceToString("root-nodes.query"))
                        .context(graphQLContext)
                        .execute();

        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void eventQuery_Success() throws IOException {
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query(readClasspathResourceToString("event.query"))
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void addTodoMutation_Success() throws IOException {
        GraphQLQueryTemplate graphQLQueryTemplate = new GraphQLQueryTemplate(graphQLSchemaHolder);
        GraphQLQueryExecutor graphQLQueryExecutor = GraphQLQueryExecutor.create(graphQLSchemaHolder);

        TodoSchema.AddTodoIn addTodoIn = new TodoSchema.AddTodoIn();
        addTodoIn.setText("new text to persist!");
        GraphQLQueryTemplate.MutationQuery mQuery = graphQLQueryTemplate.forMutation("addTodoMutation", addTodoIn);

        GraphQLRxExecutionResult result = graphQLQueryExecutor
                        .query(mQuery.getQuery())
                        .arguments(mQuery.getVariables())
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));

        mQuery = graphQLQueryTemplate.forMutation("addTodoMutation");
        result = graphQLQueryExecutor
                .query(mQuery.getQuery())
                .arguments(mQuery.getVariables())
                .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void addTodoMutationNoVariables_Success() throws IOException {
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query(readClasspathResourceToString("addtodo-mutate-novariables.query"))
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void updateTodoMutation_Success() throws IOException {
        GraphQLQueryTemplate graphQLQueryTemplate = new GraphQLQueryTemplate(graphQLSchemaHolder);

        GraphQLQueryTemplate.MutationQuery mQuery = graphQLQueryTemplate.forMutation("updateTodoMutation", "!!!new updated text!!!");

        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query(mQuery.getQuery())
                        .arguments(mQuery.getVariables())
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void updateTodoLongMutation_Success() throws IOException {
        GraphQLQueryTemplate graphQLQueryTemplate = new GraphQLQueryTemplate(graphQLSchemaHolder);

        GraphQLQueryTemplate.MutationQuery mQuery = graphQLQueryTemplate.forMutation("updateTodoLongMutation", Long.MIN_VALUE);

        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query(mQuery.getQuery())
                        .arguments(mQuery.getVariables())
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void updateTodoDateMutation_Success() throws IOException {
        GraphQLQueryTemplate graphQLQueryTemplate = new GraphQLQueryTemplate(graphQLSchemaHolder);

        GraphQLQueryTemplate.MutationQuery mQuery = graphQLQueryTemplate.forMutation("updateTodoDateMutation", new Date());

        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query(mQuery.getQuery())
                        .arguments(mQuery.getVariables())
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void updateComplexObjectToReturnMutation_Success() throws IOException {
        GraphQLQueryTemplate graphQLQueryTemplate = new GraphQLQueryTemplate(graphQLSchemaHolder);

        GraphQLQueryTemplate.MutationQuery mQuery = graphQLQueryTemplate.forMutation("updateComplexObjectToReturnMutation");

        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query(mQuery.getQuery())
                        .arguments(mQuery.getVariables())
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void getViewerManagerRecursive_Exceeded() throws IOException {
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query(readClasspathResourceToString("viewer-employee-recursive.query"))
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void getViewerManagerRecursiveConcurrent_Exceeded() throws IOException {

        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .forkJoinExecutorService()
                        .query(readClasspathResourceToString("viewer-employee-recursive.query"))
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

    @Test
    public void viewerTodosQueryConcurrent_Success() throws IOException {
        GraphQLContext<String, Object> graphQLContext = new GraphQLContext<>();
        graphQLContext.put("Key1", "value1");
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .forkJoinExecutorService()
                        .query(readClasspathResourceToString("root-nodes.query"))
                        .context(graphQLContext)
                        .execute();

        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }


    @Test
    public void getSelectiveOperationCall_Success() throws IOException {
        GraphQLRxExecutionResult result =
                GraphQLQueryExecutor
                        .create(graphQLSchemaHolder)
                        .query(readClasspathResourceToString("selection-operation-call.query"))
                        .execute();

        Assert.assertTrue(result.getErrors().size() == 0);
        LOGGER.info("Complexity: {}. Result: {}", result.getComplexity(), prettifyPrint(result.getData()));
    }

}
