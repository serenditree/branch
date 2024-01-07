package com.serenditree.root.etc.maple;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.everyItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class MapleTest {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BEFORE
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static final Map<String, String> OBJECT = Map.of("key", "value");
    static final List<Integer> INTEGER_LIST = List.of(1, 2, 3);
    static final List<String> STRING_LIST = List.of("1", "2", "3");
    static final List<Map<String, String>> OBJECT_LIST = List.of(OBJECT, OBJECT);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TESTS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static Stream<Arguments> mapListSource() {
        return Stream.of(
            Arguments.of(INTEGER_LIST, everyItem(any(String.class))),
            Arguments.of(OBJECT_LIST, everyItem(any(String.class)))
        );
    }

    @ParameterizedTest
    @MethodSource("mapListSource")
    void mapListTest(List<Object> list, Matcher<Object> matcher) {
        assertThat(Maple.mapList(list, Object::toString), matcher);
        assertThat(Maple.mapList(list, Object::toString, new LinkedList<>()), matcher);
        assertThat(Maple.mapSet(new HashSet<>(list), Object::toString), matcher);
        assertThat(Maple.mapSet(new HashSet<>(list), Object::toString, new TreeSet<>()), matcher);
        assertIterableEquals(STRING_LIST, Maple.mapList(INTEGER_LIST, Object::toString));
        assertIterableEquals(STRING_LIST, Maple.mapSetToList(new HashSet<>(INTEGER_LIST), Object::toString));
    }

    @Test
    void prettyJsonTest() {
        assertEquals("{\n    \"key\": \"value\"\n}", Maple.prettyJson(OBJECT));
    }

    @Test
    void jsonTest() {
        assertEquals("{\"key\":\"value\"}", Maple.json(OBJECT));
    }

    @Test
    void fromJsonTest() {
        assertEquals(OBJECT, Maple.fromJson("{\"key\":\"value\"}", HashMap.class));
    }
}
