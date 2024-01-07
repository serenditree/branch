package com.serenditree.branch.user.model.serializer;

import com.serenditree.branch.user.model.entities.Role;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;

import java.util.Set;

public class RoleSetSerializer implements JsonbSerializer<Set<Role>> {

    @Override
    public void serialize(Set<Role> roles, JsonGenerator jsonGenerator, SerializationContext serializationContext) {
        jsonGenerator.writeStartArray();
        roles.forEach(role -> jsonGenerator.write(role.getRoleType().toString()));
        jsonGenerator.writeEnd();
    }
}
