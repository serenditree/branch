package com.serenditree.branch.seed.model.serializer;

import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import org.bson.types.ObjectId;

import java.util.Set;

public class ObjectIdSetSerializer implements JsonbSerializer<Set<ObjectId>> {

    @Override
    public void serialize(Set<ObjectId> objectIds,
                          JsonGenerator jsonGenerator,
                          SerializationContext serializationContext) {
        jsonGenerator.writeStartArray();
        objectIds.forEach(objectId -> jsonGenerator.write(objectId.toString()));
        jsonGenerator.writeEnd();
    }
}
