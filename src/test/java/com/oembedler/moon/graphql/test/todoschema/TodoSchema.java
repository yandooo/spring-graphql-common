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

package com.oembedler.moon.graphql.test.todoschema;

import com.oembedler.moon.graphql.engine.stereotype.*;
import com.oembedler.moon.graphql.test.todoschema.objecttype.RootObjectType;
import com.oembedler.moon.graphql.test.todoschema.objecttype.TodoObjectType;

import java.util.Date;

@GraphQLSchema("TodoSchema")
public class TodoSchema {

    @GraphQLSchemaQuery
    private RootObjectType root;

    public static class AddTodoIn {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class AddTodoIn2 {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public AddTodoIn getAddTodoInputDefaultValue() {
        AddTodoIn addTodoInput = new AddTodoIn();
        addTodoInput.setText("--- default text ---");
        return addTodoInput;
    }

    @GraphQLMutation("addTodoMutation")
    public
    @GraphQLOut("todoEdge")
    TodoObjectType.TodoEdgeObjectType addTodoMutation(@GraphQLIn(value = "addTodoInput", defaultProvider = "getAddTodoInputDefaultValue") AddTodoIn addTodoInput,
                                                      AddTodoIn2 addTodoInput2) {

        TodoObjectType.TodoEdgeObjectType todoEdgeObjectType = new TodoObjectType.TodoEdgeObjectType();
        todoEdgeObjectType.setCursor("test-cursor");
        todoEdgeObjectType.setNode(new TodoObjectType());
        todoEdgeObjectType.getNode().setId("id-12345");
        todoEdgeObjectType.getNode().setText("simple text");
        todoEdgeObjectType.getNode().setComplete(false);
        todoEdgeObjectType.getNode().setUpdated(new Date());

        return todoEdgeObjectType;
    }

    @GraphQLMutation("updateTodoMutation")
    public
    @GraphQLOut("todoEdge")
    String updateTodoMutation(@GraphQLIn(value = "updateTodoInput") String newText) {

        return "Simple output string";
    }

    @GraphQLMutation("updateTodoLongMutation")
    public
    @GraphQLOut("outLong")
    Long updateTodoLongMutation(@GraphQLIn(value = "longInput") Long newText) {

        return Long.MAX_VALUE;
    }

    @GraphQLMutation("updateTodoDateMutation")
    public
    @GraphQLOut("outDate")
    Date updateTodoDateMutation(@GraphQLIn(value = "dateInput") Date date) {

        return new Date();
    }

    @GraphQLMutation("updateComplexObjectToReturnMutation")
    public
    @GraphQLOut("complexObject")
    ComplexObject updateComplexObjectToReturnMutation() {

        ComplexObject complexObject = new ComplexObject();
        complexObject.setContent("New content");
        complexObject.setReachContent(true);
        complexObject.setTimestamp(new Date().getTime());

        return complexObject;
    }

    @GraphQLObject("ComplexObject")
    public static class ComplexObject {
        private String content;
        private boolean reachContent;
        private Long timestamp;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isReachContent() {
            return reachContent;
        }

        public void setReachContent(boolean reachContent) {
            this.reachContent = reachContent;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }

}
