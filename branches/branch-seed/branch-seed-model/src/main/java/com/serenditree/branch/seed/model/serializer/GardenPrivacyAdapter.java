package com.serenditree.branch.seed.model.serializer;

import com.serenditree.branch.seed.model.entities.Garden;

import javax.json.bind.adapter.JsonbAdapter;

public class GardenPrivacyAdapter implements JsonbAdapter<Garden, Garden> {

    @Override
    public Garden adaptToJson(Garden garden) {
        if (garden.isAnonymous()) {
            garden.setUserId(null);
            garden.setUsername(null);
        }

        return garden;
    }

    @Override
    public Garden adaptFromJson(Garden garden) {
        return garden;
    }
}
