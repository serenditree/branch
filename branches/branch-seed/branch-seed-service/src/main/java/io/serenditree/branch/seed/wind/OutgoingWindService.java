package io.serenditree.branch.seed.wind;

import io.serenditree.branch.seed.model.entities.Seed;
import io.serenditree.branch.seed.wind.api.OutgoingWind;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

@ApplicationScoped
public class OutgoingWindService implements OutgoingWind {

    private final Emitter<Seed> seedCreatedChannel;
    private final Emitter<String> seedDeletedChannel;

    @Inject
    public OutgoingWindService(@Channel("seed-created") Emitter<Seed> seedCreatedChannel,
                               @Channel("seed-deleted") Emitter<String> seedDeletedChannel) {
        this.seedCreatedChannel = seedCreatedChannel;
        this.seedDeletedChannel = seedDeletedChannel;
    }

    @Override
    public void releaseSeedCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) Seed seed) {
        var metadata = OutgoingKafkaRecordMetadata
            .<String>builder()
            .withKey(seed.getId())
            .build();
        this.seedCreatedChannel.send(Message.of(seed).addMetadata(metadata));
    }

    @Override
    public void releaseSeedDeleted(@Observes(during = TransactionPhase.AFTER_SUCCESS) String id) {
        var metadata = OutgoingKafkaRecordMetadata
            .<String>builder()
            .withKey(id)
            .build();
        this.seedDeletedChannel.send(Message.of(id).addMetadata(metadata));
    }
}
