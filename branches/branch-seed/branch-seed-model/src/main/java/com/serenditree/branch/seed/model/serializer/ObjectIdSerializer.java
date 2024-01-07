package com.serenditree.branch.seed.model.serializer;

import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import org.bson.types.ObjectId;

public class ObjectIdSerializer implements JsonbSerializer<ObjectId> {

    @Override
    public void serialize(ObjectId objectId, JsonGenerator jsonGenerator, SerializationContext serializationContext) {
        jsonGenerator.write(objectId.toString());
    }
}
