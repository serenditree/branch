package io.serenditree.branch.seed.service;

import io.serenditree.branch.seed.model.entities.Garden;
import io.serenditree.branch.seed.model.filter.SeedFilter;
import io.serenditree.branch.seed.repository.api.GardenRepositoryApi;
import io.serenditree.branch.seed.service.api.GardenServiceApi;
import io.serenditree.fence.model.FenceResponse;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.util.List;

@Dependent
public class GardenService implements GardenServiceApi {

    private GardenRepositoryApi gardenRepository;

    @Override
    public Garden create(Garden garden) {
        garden.prePersist();

        try {
            this.gardenRepository.persist(garden);
        } catch (IOException e) {
            Log.errorv("Error while persisting garden {0}.", garden.getId());
            throw new WebApplicationException(e, Response.Status.BAD_GATEWAY);
        }

        return garden;
    }

    @Override
    public Garden retrieveById(String id) {
        try {
            return this.gardenRepository.retrieveById(id);
        } catch (IOException e) {
            Log.errorv("Error during retrieval of garden {0}.", id);
            throw new WebApplicationException(e, Response.Status.BAD_GATEWAY);
        }
    }

    @Override
    public List<Garden> retrieveByFilter(SeedFilter filter) {
        try {
            return this.gardenRepository.retrieveByFilter(filter);
        } catch (IOException e) {
            Log.errorv("Error during garden-retrieval by filter {0}.", filter);
            throw new WebApplicationException(e, Response.Status.BAD_GATEWAY);
        }
    }

    @Override
    public List<String> retrieveTags(String name) {
        try {
            return this.gardenRepository.retrieveTags(name);
        } catch (IOException e) {
            Log.errorv("Error during garden-tag [{0}] retrieval.", name);
            throw new WebApplicationException(e, Response.Status.BAD_GATEWAY);
        }
    }

    @Override
    public FenceResponse water(String id) throws IOException {
        return this.gardenRepository.water(id);
    }

    @Override
    public FenceResponse prune(String id) throws IOException {
        return this.gardenRepository.prune(id);
    }

    @Override
    public FenceResponse delete(String id) {
        try {
            this.gardenRepository.deleteById(id);
        } catch (IOException e) {
            Log.errorv("Could not delete garden {0}.", id);
            throw new WebApplicationException(e, Response.Status.BAD_GATEWAY);
        }

        return new FenceResponse(id);
    }

    @Override
    public Long deleteByUser(Long userId) {
        try {
            return this.gardenRepository.deleteByUser(userId);
        } catch (IOException e) {
            Log.errorv("Could not delete garden(s) by user {0}.", userId);
            throw new WebApplicationException(e, Response.Status.BAD_GATEWAY);
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public void setGardenRepository(GardenRepositoryApi gardenRepository) {
        this.gardenRepository = gardenRepository;
    }
}
