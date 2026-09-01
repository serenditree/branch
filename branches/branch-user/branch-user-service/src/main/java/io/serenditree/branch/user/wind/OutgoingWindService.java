package io.serenditree.branch.user.wind;

import io.serenditree.branch.user.wind.api.OutgoingWind;
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

    private final Emitter<Long> userDeletedChannel;

    @Inject
    public OutgoingWindService(@Channel("user-deleted") Emitter<Long> userDeletedChannel) {
        this.userDeletedChannel = userDeletedChannel;
    }

    @Override
    public void releaseUserDeleted(@Observes(during = TransactionPhase.AFTER_SUCCESS) Long id) {
        var metadata = OutgoingKafkaRecordMetadata
            .<Long>builder()
            .withKey(id)
            .build();
        this.userDeletedChannel.send(Message.of(id).addMetadata(metadata));
    }
}
