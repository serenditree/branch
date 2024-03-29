package com.serenditree.branch.seed.wind;

import com.serenditree.branch.seed.model.entities.Seed;
import com.serenditree.branch.seed.wind.api.OutgoingWindApi;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

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
        var metadata = OutgoingKafkaRecordMetadata
            .<String>builder()
            .withKey(seed.getId().toString())
            .build();
        this.seedCreatedChannel.send(Message.of(seed).addMetadata(metadata));
    }

    @Override
    public void releaseSeedDeleted(@Observes(during = TransactionPhase.AFTER_SUCCESS) ObjectId id) {
        var metadata = OutgoingKafkaRecordMetadata
            .<String>builder()
            .withKey(id.toString())
            .build();
        this.seedDeletedChannel.send(Message.of(id.toString()).addMetadata(metadata));
    }
}
