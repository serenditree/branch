package com.serenditree.branch.seed.service;

import com.serenditree.branch.poll.model.entities.Poll;
import com.serenditree.branch.poll.service.api.PollServiceClientApi;
import com.serenditree.branch.seed.model.entities.Seed;
import com.serenditree.branch.seed.model.filter.SeedFilter;
import com.serenditree.branch.seed.repository.api.SeedRepositoryApi;
import com.serenditree.branch.seed.service.api.GardenServiceApi;
import com.serenditree.branch.seed.service.api.SeedServiceApi;
import com.serenditree.fence.annotation.FencedContext;
import com.serenditree.fence.model.FenceResponse;
import com.serenditree.fence.model.api.FencePrincipal;
import org.bson.types.ObjectId;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import java.util.List;

@Dependent
public class SeedService implements SeedServiceApi {

    private FencePrincipal principal;

    private SeedRepositoryApi seedRepository;

    private GardenServiceApi gardenService;

    private PollServiceClientApi pollServiceClient;

    private Event<Seed> onCreation;

    private Event<ObjectId> onDeletion;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Seed create(Seed seed) {
        seed.prePersist();
        this.seedRepository.persist(seed);

        if (seed.getPolls() != null && !seed.getPolls().isEmpty()) {
            for (Poll poll : seed.getPolls()) {
                poll.setUserId(this.principal.getId());
                poll.setSeedId(seed.getId().toString());
            }
            seed.setPolls(this.pollServiceClient.create(seed.getPolls()));
        }

        this.onCreation.fire(seed);

        return seed;
    }

    @Override
    public Seed retrieveById(ObjectId id) {
        return this.seedRepository.findById(id);
    }

    @Override
    public List<Seed> retrieveByFilter(SeedFilter filter) {
        return this.seedRepository.retrieveByFilter(filter);
    }

    @Override
    public List<String> retrieveTags(String name) {
        return this.seedRepository.retrieveTags(name);
    }

    @Override
    public FenceResponse water(ObjectId id, ObjectId gardenId) {
        if (gardenId != null) {
            this.gardenService.water(gardenId);
        }

        return this.seedRepository.water(id);
    }

    @Override
    public FenceResponse prune(ObjectId id, ObjectId gardenId) {
        if (gardenId != null) {
            this.gardenService.prune(gardenId);
        }

        return this.seedRepository.prune(id);
    }


    @Override
    public FenceResponse delete(ObjectId id) {
        if (this.seedRepository.deleteById(id)) {
            this.onDeletion.fire(id);
        } else {
            throw new InternalServerErrorException("Could not delete seed with id " + id);
        }

        return new FenceResponse(id);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public void setPrincipal(@FencedContext FencePrincipal principal) {
        this.principal = principal;
    }

    @Inject
    public void setSeedRepository(SeedRepositoryApi seedRepository) {
        this.seedRepository = seedRepository;
    }

    @Inject
    public void setGardenService(GardenServiceApi gardenService) {
        this.gardenService = gardenService;
    }

    @Inject
    public void setPollServiceClient(PollServiceClientApi pollServiceClient) {
        this.pollServiceClient = pollServiceClient;
    }

    @Inject
    public void setOnDeletion(Event<ObjectId> onDeletion) {
        this.onDeletion = onDeletion;
    }

    @Inject
    public void setOnCreation(Event<Seed> onCreation) {
        this.onCreation = onCreation;
    }
}
