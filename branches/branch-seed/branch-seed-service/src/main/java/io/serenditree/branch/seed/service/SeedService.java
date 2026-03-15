package io.serenditree.branch.seed.service;

import io.serenditree.branch.poll.model.entities.Poll;
import io.serenditree.branch.poll.service.api.PollServiceClientApi;
import io.serenditree.branch.seed.model.entities.Seed;
import io.serenditree.branch.seed.model.filter.SeedFilter;
import io.serenditree.branch.seed.repository.api.SeedRepositoryApi;
import io.serenditree.branch.seed.service.api.GardenServiceApi;
import io.serenditree.branch.seed.service.api.SeedServiceApi;
import io.serenditree.fence.annotation.FencedContext;
import io.serenditree.fence.model.FenceResponse;
import io.serenditree.fence.model.api.FencePrincipal;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.util.List;

@Dependent
public class SeedService implements SeedServiceApi {

    private FencePrincipal principal;

    private SeedRepositoryApi seedRepository;

    private GardenServiceApi gardenService;

    private PollServiceClientApi pollServiceClient;

    private Event<Seed> onCreation;

    private Event<String> onDeletion;

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    @SuppressWarnings("java:S2139")
    public Seed create(Seed seed) {
        seed.prePersist();

        try {
            this.seedRepository.persist(seed);
            if (seed.getPolls() != null && !seed.getPolls().isEmpty()) {
                for (Poll poll : seed.getPolls()) {
                    poll.setUserId(this.principal.getId());
                    poll.setSeedId(seed.getId());
                }
                seed.setPolls(this.pollServiceClient.create(seed.getPolls()));
            }
            this.onCreation.fire(seed);

        } catch (IOException e) {
            Log.errorv("Error while persisting seed {0}.", seed.getId());
            throw new WebApplicationException(e, Response.Status.BAD_GATEWAY);

        } catch (ProcessingException processingException) {
            try {
                this.seedRepository.deleteById(seed.getId());
            } catch (IOException ioException) {
                Log.errorv("Error during deletion of seed {0}.", seed.getId());
            }
            throw new WebApplicationException(processingException, Response.Status.BAD_GATEWAY);
        }

        return seed;
    }

    @Override
    public Seed retrieveById(String id) {
        try {
            return this.seedRepository.retrieveById(id);
        } catch (IOException e) {
            Log.errorv("Error during retrieval of seed {0}.", id);
            throw new WebApplicationException(e, Response.Status.BAD_GATEWAY);
        }
    }

    @Override
    public List<Seed> retrieveByFilter(SeedFilter filter) {
        try {
            return this.seedRepository.retrieveByFilter(filter);
        } catch (IOException e) {
            Log.errorv("Error during seed-retrieval by filter {0}.", filter);
            throw new WebApplicationException(e, Response.Status.BAD_GATEWAY);
        }
    }

    @Override
    public List<String> retrieveTags(String name) {
        try {
            // TODO the lowercase filter of the standard analyzer should take care of that.
            return this.seedRepository.retrieveTags(name.toLowerCase());
        } catch (IOException e) {
            Log.errorv("Error during seed-tag [{0}] retrieval.", name);
            throw new WebApplicationException(e, Response.Status.BAD_GATEWAY);
        }
    }

    @Override
    public FenceResponse water(String id, String gardenId) {
        try {
            if (gardenId != null) {
                this.gardenService.water(gardenId);
            }
            return this.seedRepository.water(id);
        } catch (IOException e) {
            Log.errorv("Could not water seed {0}.", id);
            throw new WebApplicationException(e, Response.Status.BAD_GATEWAY);
        }
    }

    @Override
    public FenceResponse prune(String id, String gardenId) {
        try {
            if (gardenId != null) {
                this.gardenService.prune(gardenId);
            }

            return this.seedRepository.prune(id);
        } catch (IOException e) {
            Log.errorv("Could not prune seed {0}.", id);
            throw new WebApplicationException(e, Response.Status.BAD_GATEWAY);
        }
    }


    @Override
    public FenceResponse delete(String id) {
        try {
            if (this.seedRepository.deleteById(id)) {
                this.onDeletion.fire(id);
            } else {
                throw new InternalServerErrorException("Could not delete seed " + id + ".");
            }
        } catch (IOException e) {
            Log.errorv("Could not delete seed {0}.", id);
            throw new WebApplicationException(e, Response.Status.BAD_GATEWAY);
        }

        return new FenceResponse(id);
    }

    @Override
    public Long deleteByUser(Long userId) {
        try {
            return this.seedRepository.deleteByUser(userId);
        } catch (IOException e) {
            Log.errorv("Could not delete seed(s) by user {0}.", userId);
            throw new WebApplicationException(e, Response.Status.BAD_GATEWAY);
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
    public void setOnDeletion(Event<String> onDeletion) {
        this.onDeletion = onDeletion;
    }

    @Inject
    public void setOnCreation(Event<Seed> onCreation) {
        this.onCreation = onCreation;
    }
}
