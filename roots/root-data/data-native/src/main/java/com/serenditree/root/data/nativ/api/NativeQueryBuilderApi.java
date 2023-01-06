package com.serenditree.root.data.nativ.api;

import com.serenditree.branch.seed.model.filter.SortingType;
import com.serenditree.root.data.geo.model.LngLatBounds;
import com.serenditree.root.data.nativ.model.Update;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Set;

public interface NativeQueryBuilderApi {

    NativeQueryBuilderApi createNativeQuery();

    NativeQueryBuilderApi setBounds(LngLatBounds bounds);

    NativeQueryBuilderApi setUserId(Long userId);

    NativeQueryBuilderApi setParent(ObjectId parentId, boolean isTrail);

    NativeQueryBuilderApi setTags(Set<String> tags);

    NativeQueryBuilderApi setPoll();

    NativeQueryBuilderApi setTrail();

    NativeQueryBuilderApi setSort(SortingType sort);

    NativeQueryBuilderApi setRetention(int retention);

    NativeQueryBuilderApi setTextLimit(int maxBytes);

    NativeQueryBuilderApi setSkip(int skip);

    NativeQueryBuilderApi setLimit(int limit);

    NativeQueryBuilderApi createTagsQuery(String name);

    Update water(ObjectId id);

    Update prune(ObjectId id);

    Update nubit(ObjectId id, int value);

    List<Bson> build();

    String toJson();

    String toString();

    String getCollection();
}
