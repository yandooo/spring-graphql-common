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

package com.oembedler.moon.graphql.test.todoschema.objecttype;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.oembedler.moon.graphql.engine.stereotype.*;
import com.oembedler.moon.graphql.test.GenericTodoSchemaParserTest;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
@GraphQLObject("User")
public class UserObjectType extends BaseObjectType {

    private String name;
    private ROLE role;

    @GraphQLIgnore
    private UserObjectType manager;

    @GraphQLDescription("User roles")
    public enum ROLE {
        USER, ADMIN, MANAGER
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ROLE getRole() {
        return role;
    }

    public UserObjectType getManager() {
        return manager;
    }

    public void setManager(UserObjectType manager) {
        this.manager = manager;
    }

    public void setRole(ROLE role) {
        this.role = role;
    }

    @GraphQLField("manager")
    public UserObjectType getManager(UserObjectType employee,
                                     @GraphQLIn(value = "ids", defaultSpel = "T(java.util.Collections).EMPTY_SET") Set<String> ids) {
        UserObjectType userManagerObjectType = new UserObjectType();
        userManagerObjectType.setId("user:id:manager:1234567");
        userManagerObjectType.setName("Wolf Adolf Riverswood");
        userManagerObjectType.setUpdated(new Date());
        userManagerObjectType.setRole(UserObjectType.ROLE.MANAGER);
        return userManagerObjectType;
    }

    @GraphQLField("getAllUsersByRole")
    public List<UserObjectType> getAllUsersByRole(UserObjectType employee, @GraphQLIn("role") ROLE role) {
        UserObjectType userManagerObjectType = new UserObjectType();
        userManagerObjectType.setId("user:id:manager:000001");
        userManagerObjectType.setName("Adam Smith");
        userManagerObjectType.setUpdated(new Date());
        userManagerObjectType.setRole(UserObjectType.ROLE.MANAGER);

        return Lists.newArrayList(userManagerObjectType);
    }

    public Integer getFirstDefaultValue() {
        return 1;
    }

    public Integer getLastDefaultValue() {
        return 1;
    }

    @GraphQLField
    @GraphQLComplexity("1 + #first * #childScore")
    public TodoObjectType.TodoConnectionObjectType todos(@GraphQLIn("before") String before,
                                                         @GraphQLIn("after") String after,
                                                         @GraphQLIn(value = "first", defaultProvider = "getFirstDefaultValue") Integer first,
                                                         @GraphQLIn(value = "last", defaultProvider = "getLastDefaultValue") Integer last,
                                                         GenericTodoSchemaParserTest.GraphQLContext graphQLContext) {

        TodoObjectType.TodoEdgeObjectType todoEdgeObjectType = new TodoObjectType.TodoEdgeObjectType();
        todoEdgeObjectType.setCursor("base64;test:cursor");

        TodoObjectType todoObjectType = new TodoObjectType();
        todoObjectType.setText("user:12345:todo-987");
        todoObjectType.setComplete(true);
        todoObjectType.setId("todo:id:56789");
        todoObjectType.setUpdated(new Date());
        todoEdgeObjectType.setNode(todoObjectType);

        TodoObjectType.TodoConnectionObjectType todoConnectionObjectType = new TodoObjectType.TodoConnectionObjectType();
        todoConnectionObjectType.setEdges(Lists.newArrayList(todoEdgeObjectType));

        return todoConnectionObjectType;
    }

}
