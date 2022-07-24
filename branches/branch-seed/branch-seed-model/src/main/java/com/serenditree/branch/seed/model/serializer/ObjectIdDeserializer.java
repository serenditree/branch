package com.serenditree.branch.seed.model.serializer;

import org.bson.types.ObjectId;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;

public class ObjectIdDeserializer implements JsonbDeserializer<ObjectId> {

    @Override
    public ObjectId deserialize(JsonParser jsonParser, DeserializationContext deserializationContext, Type type) {
        return new ObjectId(jsonParser.getString());
    }
}