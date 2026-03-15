package com.serenditree.root.rest.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;

import java.lang.reflect.ParameterizedType;
import java.util.List;

public abstract class AbstractClientRest<T> {

    private final Client client = ClientBuilder.newClient();

    private final Class<T> entityType;

    @SuppressWarnings("unchecked")
    protected AbstractClientRest() {
        ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.entityType = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
    }

    protected T post(T request, String target) {

        return this.client
            .target(target)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.json(request), this.entityType);
    }

    protected List<T> post(List<T> request, String target) {

        return this.client
            .target(target)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.json(request), new GenericType<List<T>>() {
            });
    }

    protected void delete(String id, String target) {

        this.client
            .target(target)
            .path(id)
            .request()
            .delete();
    }
}
