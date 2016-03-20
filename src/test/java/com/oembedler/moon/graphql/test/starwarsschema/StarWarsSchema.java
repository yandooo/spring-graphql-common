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

package com.oembedler.moon.graphql.test.starwarsschema;

import com.oembedler.moon.graphql.engine.stereotype.*;

import java.util.List;

/**
 * @author <a href="mailto:java.lang.RuntimeException@gmail.com">oEmbedler Inc.</a>
 */
@GraphQLSchema
public class StarWarsSchema {

    @GraphQLSchemaQuery
    private QueryType queryType;

    @GraphQLEnum(valueProvider = "getValue")
    public enum Episode {
        @GraphQLDescription("Released in 1977.")
        NEWHOPE,
        @GraphQLDescription("Released in 1980.")
        EMPIRE,
        @GraphQLDescription("Released in 1983.")
        JEDI;

        public static Object getValue(Episode self) {
            if (self == NEWHOPE)
                return 4;
            else if (self == EMPIRE)
                return 5;
            else
                return 6;
        }
    }

    @GraphQLEnum(defaultSpel = "#obj.getActualValue()")
    public enum EpisodeV2 {
        @GraphQLDescription("Released in 1977.")
        NEWHOPE(4),
        @GraphQLDescription("Released in 1980.")
        EMPIRE(5),
        @GraphQLDescription("Released in 1983.")
        JEDI(6);

        int actualValue;

        EpisodeV2(int val) {
            actualValue = val;
        }

        public int getActualValue() {
            return actualValue;
        }
    }

    @GraphQLInterface
    @GraphQLDescription("A character in the Star Wars Trilogy")
    public interface Character {

        @GraphQLNonNull
        @GraphQLField("id")
        String getId();

        @GraphQLField("name")
        String getName();

        @GraphQLField("friends")
        List<Character> getFriends();

        @GraphQLField("appearsIn")
        List<Episode> getAppearsIn();
    }

    public static class BaseCharacter implements Character {
        @GraphQLIgnore
        private String id;
        @GraphQLIgnore
        private String name;
        @GraphQLIgnore
        private List<Character> friends;

        @Override
        @GraphQLNonNull
        @GraphQLField("id")
        @GraphQLDescription("The id of the human.")
        public String getId() {
            return null;
        }

        @Override
        @GraphQLField("name")
        @GraphQLDescription("The name of the human.")
        public String getName() {
            return null;
        }

        @Override
        @GraphQLField("friends")
        @GraphQLDescription("The friends of the human, or an empty list if they have none.")
        public List<Character> getFriends() {
            return null /** StarWarsData.getFriendsDataFetcher() **/;
        }

        @Override
        @GraphQLField("appearsIn")
        @GraphQLDescription("Which movies they appear in.")
        public List<Episode> getAppearsIn() {
            return null;
        }
    }

    @GraphQLObject
    @GraphQLDescription("A humanoid creature in the Star Wars universe.")
    public static class Human extends BaseCharacter {
        @GraphQLDescription("The home planet of the human, or null if unknown.")
        private String homePlanet;

        public String getHomePlanet() {
            return homePlanet;
        }

        public void setHomePlanet(String homePlanet) {
            this.homePlanet = homePlanet;
        }
    }

    @GraphQLObject
    @GraphQLDescription("A mechanical creature in the Star Wars universe.")
    public static class Droid extends BaseCharacter {
        @GraphQLDescription("The primary function of the droid.")
        private String primaryFunction;

        public String getPrimaryFunction() {
            return primaryFunction;
        }

        public void setPrimaryFunction(String primaryFunction) {
            this.primaryFunction = primaryFunction;
        }
    }

    @GraphQLObject
    public static class QueryType {

        @GraphQLField
        public Character hero(@GraphQLIn("hero") Episode episode) {
            return null /** StarWarsData.getArtoo() **/;
        }

        @GraphQLField
        public Human human(@GraphQLNonNull @GraphQLIn("id") String id) {
            return null /** StarWarsData.getHumanDataFetcher() **/;
        }

        @GraphQLField
        public Droid droid(@GraphQLNonNull @GraphQLIn("id") String id) {
            return null /** StarWarsData.getDroidDataFetcher() **/;
        }
    }
}

