package com.serenditree.root.util.maple;

import jakarta.json.bind.adapter.JsonbAdapter;
import org.bson.types.ObjectId;

public class MapleObjectId implements JsonbAdapter<ObjectId, String> {
    @Override
    public String adaptToJson(ObjectId objectId) {
        return objectId.toHexString();
    }

    @Override
    public ObjectId adaptFromJson(String obj) {
        return new ObjectId(obj);
    }
}
