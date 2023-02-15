package com.serenditree.root.test.matcher;

import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.serenditree.root.test.matcher.TupleMatcher.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TupleMatcherTest {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BEFORE
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static final List<Integer> INTEGER_LIST = List.of(1, 2, 3);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TESTS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @MethodSource("firstItemSource")
    void firstItemTest(Matcher<Iterable> matcher) {
        assertThat(INTEGER_LIST, matcher);
    }

    static Stream<Arguments> firstItemSource() {
        return Stream.of(
            Arguments.of(firstItem(is(1))),
            Arguments.of(firstItem(is(not(2))))
        );
    }

    @ParameterizedTest
    @MethodSource("lastItemSource")
    void lastItemTest(Matcher<Iterable> matcher) {
        assertThat(INTEGER_LIST, matcher);
    }

    static Stream<Arguments> lastItemSource() {
        return Stream.of(
            Arguments.of(lastItem(is(3))),
            Arguments.of(lastItem(is(not(2))))
        );
    }

    @ParameterizedTest
    @MethodSource("itemAtSource")
    void itemAtTest(Matcher<Iterable> matcher) {
        assertThat(INTEGER_LIST, matcher);
    }

    static Stream<Arguments> itemAtSource() {
        return Stream.of(
            Arguments.of(itemAt(1, is(2))),
            Arguments.of(itemAt(1, is(not(1)))),
            Arguments.of(itemAt(-1, is(3))),
            Arguments.of(itemAt(-1, is(not(2)))),
            Arguments.of(itemAt(0, any(Integer.class)))
        );
    }

    @ParameterizedTest
    @MethodSource("itemAtFailureSource")
    void itemAtFailureTest(Iterable iterable, Matcher<Iterable> matcher) {
        assertThrows(AssertionError.class, () -> assertThat(iterable, matcher));
    }

    static Stream<Arguments> itemAtFailureSource() {
        return Stream.of(
            Arguments.of(INTEGER_LIST, itemAt(INTEGER_LIST.size(), any(Integer.class))),
            Arguments.of(INTEGER_LIST, itemAt(INTEGER_LIST.size() * -1, any(Integer.class))),
            Arguments.of(new ArrayList<>(), itemAt(0, any(Integer.class))),
            Arguments.of(null, itemAt(0, any(Object.class)))
        );
    }
}
