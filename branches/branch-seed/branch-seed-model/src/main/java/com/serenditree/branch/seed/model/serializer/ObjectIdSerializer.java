package com.serenditree.branch.seed.model.serializer;

import org.bson.types.ObjectId;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

public class ObjectIdSerializer implements JsonbSerializer<ObjectId> {

    @Override
    public void serialize(ObjectId objectId, JsonGenerator jsonGenerator, SerializationContext serializationContext) {
        jsonGenerator.write(objectId.toString());
    }
}