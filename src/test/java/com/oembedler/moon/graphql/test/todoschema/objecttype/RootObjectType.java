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

import com.oembedler.moon.graphql.engine.relay.RelayNode;
import com.oembedler.moon.graphql.engine.stereotype.*;

import java.util.Date;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
@GraphQLObject("Root")
public class RootObjectType {

    @GraphQLNonNull
    @GraphQLField("version")
    @GraphQLDescription("GraphQL Schema Version")
    public final static String VERSION = "2.0";

    @GraphQLField
    public UserObjectType viewer(/** no input parameters expected **/) {
        UserObjectType userObjectType = new UserObjectType();
        userObjectType.setId("user:id:12345");
        userObjectType.setName("Ash Nikolas Kaen");
        userObjectType.setUpdated(new Date());
        userObjectType.setRole(UserObjectType.ROLE.USER);
        return userObjectType;
    }

    @GraphQLField
    public long event(@GraphQLNonNull @GraphQLIn(value = "id") final Long event) {
        return new Date().getTime();
    }

    @GraphQLField
    public RelayNode node(@GraphQLID @GraphQLNonNull @GraphQLIn(value = "id") final String id) {
        /** data fetcher goes here **/
        TodoObjectType todoObjectType = new TodoObjectType();
        todoObjectType.setId(id);
        todoObjectType.setText("Node text for test!");
        todoObjectType.setUpdated(new Date());
        return todoObjectType;
    }
}
