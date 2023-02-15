package com.serenditree.branch.seed.repository.query;

import com.serenditree.root.etc.maple.Maple;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.serenditree.root.test.matcher.TupleMatcher.lastItem;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AbstractMongoNativeQueryBuilderTest {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BEFORE
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Test implementation.
    static class MongoNativeQueryBuilderTest extends AbstractMongoNativeQueryBuilder {
        @Override
        public String getCollection() {
            return "Test";
        }
    }

    final String tagsQuery = new MongoNativeQueryBuilderTest()
        .createNativeQuery()
        .createTagsQuery("tag")
        .toJson();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TESTS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @MethodSource("buildTagsQuerySchemaSource")
    void buildTagsQuerySchemaTest(JsonSchemaValidator validator) {
        assertThat(this.tagsQuery, validator);
    }

    static Stream<Arguments> buildTagsQuerySchemaSource() {
        return Stream.of(
            Arguments.of(matchesJsonSchemaInClasspath("initial-match-schema.json")),
            Arguments.of(matchesJsonSchemaInClasspath("project-tag-only-schema.json"))
        );
    }

    @Test
    void buildTagsQueryTest() {
        assertThat(
            Maple.mapList(from(this.tagsQuery).getList(""), Object::toString),
            lastItem(is("{$project={tag=1}}"))
        );
    }
}
