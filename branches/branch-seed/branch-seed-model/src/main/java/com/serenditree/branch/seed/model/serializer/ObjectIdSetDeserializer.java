package com.serenditree.branch.seed.model.serializer;

import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;
import org.bson.types.ObjectId;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class ObjectIdSetDeserializer implements JsonbDeserializer<Set<ObjectId>> {

    @Override
    public Set<ObjectId> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext, Type type) {
        Set<ObjectId> objectIdSet = new HashSet<>();
        while (jsonParser.hasNext()) {
            if (jsonParser.next() == JsonParser.Event.VALUE_STRING) {
                objectIdSet.add(new ObjectId(jsonParser.getString()));
            }
        }

        return objectIdSet;
    }
}
