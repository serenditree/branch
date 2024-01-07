package com.serenditree.branch.seed.model.serializer;

import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;
import org.bson.types.ObjectId;

import java.lang.reflect.Type;

public class ObjectIdDeserializer implements JsonbDeserializer<ObjectId> {

    @Override
    public ObjectId deserialize(JsonParser jsonParser, DeserializationContext deserializationContext, Type type) {
        return new ObjectId(jsonParser.getString());
    }
}
