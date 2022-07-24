package com.serenditree.branch.seed.model.serializer;

import org.bson.types.ObjectId;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;

public class ObjectIdSetDeserializer implements JsonbDeserializer<Set<ObjectId>> {

    @Override
    public Set<ObjectId> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext, Type type) {
        Set<ObjectId> objectIdSet = new LinkedHashSet<>();
        while (jsonParser.hasNext()) {
            if (jsonParser.next() == JsonParser.Event.VALUE_STRING) {
                objectIdSet.add(new ObjectId(jsonParser.getString()));
            }
        }

        return objectIdSet;
    }
}
