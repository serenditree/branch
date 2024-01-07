package com.serenditree.branch.user.model.serializer;

import com.serenditree.branch.user.model.entities.Role;
import com.serenditree.fence.model.enums.RoleType;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class RoleSetDeserializer implements JsonbDeserializer<Set<Role>> {

    @Override
    public Set<Role> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext, Type type) {
        Set<Role> roleSet = new HashSet<>();

        while (jsonParser.hasNext()) {
            if (jsonParser.next() == JsonParser.Event.VALUE_STRING) {
                roleSet.add(new Role(null, RoleType.valueOf(jsonParser.getString())));
            }
        }

        return roleSet;
    }
}
