package com.serenditree.root.test.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies a {@link Matcher} to the first-, last- or n-th item of an {@link Iterable}.
 *
 * @param <T> Type of the item the {@link Matcher} is applied to.
 */
public class TupleMatcher<T> extends TypeSafeDiagnosingMatcher<Iterable<T>> {

    private final int index;

    private final Matcher<T> matcher;

    /**
     * Private constructor that defines index and matcher.
     *
     * @param index   Index of the item the {@link Matcher} will be applied to.
     * @param matcher {@link Matcher} to apply.
     */
    private TupleMatcher(int index, Matcher<T> matcher) {
        this.index = index;
        this.matcher = matcher;
    }

    /**
     * Applies the given {@link Matcher} to the first item of an {@link Iterable}.
     *
     * @param matcher {@link Matcher} to apply.
     * @param <T>     Type of the item the {@link Matcher} is applied to.
     * @return Instance of {@link TupleMatcher} for the first item.
     */
    public static <T> Matcher<Iterable<T>> firstItem(Matcher<T> matcher) {
        return new TupleMatcher<>(0, matcher);
    }

    /**
     * Applies the given {@link Matcher} to the last item of an {@link Iterable}.
     *
     * @param matcher {@link Matcher} to apply.
     * @param <T>     Type of the item the {@link Matcher} is applied to.
     * @return Instance of {@link TupleMatcher} for the last item.
     */
    public static <T> Matcher<Iterable<T>> lastItem(Matcher<T> matcher) {
        return new TupleMatcher<>(-1, matcher);
    }

    /**
     * Applies the given {@link Matcher} to the n-th item of an {@link Iterable}.
     *
     * @param matcher {@link Matcher} to apply.
     * @param index   Index of the item the {@link Matcher} is applied to.
     * @param <T>     Type of the item the {@link Matcher} is applied to.
     * @return Instance of {@link TupleMatcher} for the n-th item.
     */
    public static <T> Matcher<Iterable<T>> itemAt(int index, Matcher<T> matcher) {
        return new TupleMatcher<>(index, matcher);
    }

    /**
     * @see TypeSafeDiagnosingMatcher#describeTo(Description)
     */
    @Override
    public void describeTo(Description description) {
        description
                .appendText(this.getClass().getSimpleName())
                .appendText(" at index ")
                .appendValue(this.index)
                .appendText(" expects: ")
                .appendDescriptionOf(this.matcher);
    }

    /**
     * @see TypeSafeDiagnosingMatcher#matchesSafely(Object, Description)
     */
    @Override
    protected boolean matchesSafely(Iterable<T> iterable, Description description) {
        boolean matches = false;

        if (iterable == null) {
            description.appendText("Tuple is ")
                    .appendValue(null)
                    .appendText(".");
        } else {
            final List<T> tuple = this.toList(iterable);
            if (tuple.isEmpty()) {
                description.appendText("Tuple is empty.");
            } else if (Math.abs(this.index) >= tuple.size()) {
                description
                        .appendText("Tuple is too small. Its size is ")
                        .appendValue(tuple.size())
                        .appendText(".");
            } else {
                T item = tuple.get(this.calculateIndex(tuple));

                this.matcher.describeMismatch(item, description);
                matches = this.matcher.matches(item);
            }
        }

        return matches;
    }

    /**
     * Maps an {@link Iterable} to an {@link ArrayList}.
     *
     * @param iterable {@link Iterable}.
     * @return {@link ArrayList}.
     */
    protected List<T> toList(Iterable<T> iterable) {
        final List<T> list = new ArrayList<>();
        iterable.forEach(list::add);

        return list;
    }

    /**
     * Returns the index relative to the start if a negative value is provided. A negative index means that the index
     * should be calculated relative to the end of the tuple.
     *
     * @param tuple Tuple the index is calculated for.
     * @return Index relative to the start of the tuple.
     */
    protected int calculateIndex(List<T> tuple) {
        return this.index < 0 ? tuple.size() + this.index : this.index;
    }
}
