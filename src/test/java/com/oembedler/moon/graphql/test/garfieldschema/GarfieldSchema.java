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

package com.oembedler.moon.graphql.test.garfieldschema;

import com.oembedler.moon.graphql.engine.stereotype.*;
import com.oembedler.moon.graphql.test.todoschema.objecttype.RootObjectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
@GraphQLSchema
public class GarfieldSchema {

    @GraphQLSchemaQuery
    private Person root;

    public static class RootObjectType{

    }

    @GraphQLInterface
    public interface Named {
        @GraphQLField("name")
        String getName();
    }

    @GraphQLObject
    public static class Dog implements Named, PetsUnionType {
        @GraphQLIgnore
        private String name;
        private boolean barks;

        public Dog() {
        }

        public Dog(String name, boolean barks) {
            this.name = name;
            this.barks = barks;
        }

        public boolean isBarks() {
            return barks;
        }

        @GraphQLField("name")
        public String getName() {
            return name;
        }
    }

    @GraphQLObject
    public static class Cat implements Named, PetsUnionType {
        @GraphQLIgnore
        private String name;

        private boolean meows;

        public Cat() {
        }

        public Cat(String name, boolean meows) {
            this.name = name;
            this.meows = meows;
        }

        public boolean isMeows() {
            return meows;
        }

        @GraphQLField("name")
        public String getName() {
            return name;
        }
    }

    @GraphQLUnion(value = "Pet", possibleTypes = {Dog.class, Cat.class})
    public interface PetsUnionType {
    }

    @GraphQLObject
    public static class Person implements Named {
        @GraphQLIgnore
        private String name;
        @GraphQLIgnore
        private List<Dog> dogs;
        private List<Cat> cats;

        @GraphQLIgnore
        private List<Named> friends;

        public Person() {
        }

        public Person(String name) {
            this(name, Collections.<Cat>emptyList(), Collections.<Dog>emptyList(), Collections.<Named>emptyList());
        }

        public Person(String name, List<Cat> cats, List<Dog> dogs, List<Named> friends) {
            this.name = name;
            this.dogs = dogs;
            this.cats = cats;
            this.friends = friends;
        }

        @GraphQLField("pets")
        public List<PetsUnionType> getPets() {
            List<PetsUnionType> pets = new ArrayList<>();
            pets.addAll(cats);
            pets.addAll(dogs);
            return pets;
        }

        @GraphQLField("name")
        public String getName() {
            return name;
        }

        @GraphQLField("friends")
        public List<Named> getFriends() {
            return friends;
        }
    }

    @GraphQLIgnore
    public static Cat garfield = new Cat("Garfield", false);
    @GraphQLIgnore
    public static Dog odie = new Dog("Odie", true);
    @GraphQLIgnore
    public static Person liz = new Person("Liz");
    @GraphQLIgnore
    public static Person john = new Person("John", Arrays.asList(garfield), Arrays.asList(odie), Arrays.asList(liz, odie));


/*    public static GraphQLUnionType PetType = newUnionType()
            .name("Pet")
            .possibleType(CatType)
            .possibleType(DogType)
            .typeResolver(new TypeResolver() {
                @Override
                public GraphQLObjectType getType(Object object) {
                    if (object instanceof Cat) {
                        return CatType;
                    }
                    if (object instanceof Dog) {
                        return DogType;
                    }
                    return null;
                }
            })
            .build();*/

/*    public static GraphQLObjectType PersonType = newObject()
            .name("Person")
            .field(newFieldDefinition()
                    .name("name")
                    .type(GraphQLString)
                    .build())
            .field(newFieldDefinition()
                    .name("pets")
                    .type(new GraphQLList(PetType))
                    .build())
            .field(newFieldDefinition()
                    .name("friends")
                    .type(new GraphQLList(NamedType))
                    .build())
            .withInterface(NamedType)
            .build();*/

}
