package com.serenditree.branch.seed.model.serializer;

import org.bson.types.ObjectId;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;
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
