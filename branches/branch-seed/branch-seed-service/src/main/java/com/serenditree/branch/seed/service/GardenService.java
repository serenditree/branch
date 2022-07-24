package com.serenditree.branch.seed.service;

import com.serenditree.branch.seed.model.entities.Garden;
import com.serenditree.branch.seed.model.filter.SeedFilter;
import com.serenditree.branch.seed.repository.api.GardenRepositoryApi;
import com.serenditree.branch.seed.service.api.GardenServiceApi;
import com.serenditree.fence.annotation.FencedContext;
import com.serenditree.fence.model.FenceResponse;
import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.root.etc.oak.Oak;
import org.bson.types.ObjectId;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class GardenService implements GardenServiceApi {

    private GardenRepositoryApi gardenRepository;

    private FencePrincipal principal;

    @Override
    public Garden create(Garden garden) {
        // TODO move to fence decorator
        garden.setText(Oak.html(garden.getText()));
        garden.setUserId(this.principal.getId());
        garden.setUsername(this.principal.getUsername());
        garden.prePersist();

        this.gardenRepository.persist(garden);

        return garden;
    }

    @Override
    public Garden retrieveById(ObjectId id) {
        return this.gardenRepository.findById(id);
    }

    @Override
    public List<Garden> retrieveByFilter(SeedFilter filter) {
        return this.gardenRepository.retrieveByFilter(filter);
    }

    @Override
    public List<String> retrieveTags(String name) {
        return this.gardenRepository.retrieveTags(name);
    }

    @Override
    public FenceResponse water(ObjectId id) {
        return this.gardenRepository.water(id);
    }

    @Override
    public FenceResponse prune(ObjectId id) {
        return this.gardenRepository.prune(id);
    }

    @Override
    public FenceResponse delete(ObjectId id) {
        this.gardenRepository.deleteById(id);

        return new FenceResponse(id);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public void setGardenRepository(GardenRepositoryApi gardenRepository) {
        this.gardenRepository = gardenRepository;
    }

    @Inject
    public void setPrincipal(@FencedContext FencePrincipal principal) {
        this.principal = principal;
    }
}
