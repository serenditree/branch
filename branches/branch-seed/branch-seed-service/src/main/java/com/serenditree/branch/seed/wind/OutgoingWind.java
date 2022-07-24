package com.serenditree.branch.seed.wind;

import com.serenditree.branch.seed.model.entities.Seed;
import com.serenditree.branch.seed.wind.api.OutgoingWindApi;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

@ApplicationScoped
public class OutgoingWind implements OutgoingWindApi {

    @Inject
    @Channel("seed-created")
    Emitter<Seed> seedCreatedChannel;

    @Inject
    @Channel("seed-deleted")
    Emitter<String> seedDeletedChannel;

    @Override
    public void releaseSeedCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) Seed seed) {
        this.seedCreatedChannel.send(Message.of(seed));
    }

    @Override
    public void releaseSeedDeleted(@Observes(during = TransactionPhase.AFTER_SUCCESS) ObjectId id) {
        this.seedDeletedChannel.send(Message.of(id.toString()));
    }
}
