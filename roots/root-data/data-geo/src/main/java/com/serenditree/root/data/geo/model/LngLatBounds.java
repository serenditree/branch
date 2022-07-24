package com.serenditree.root.data.geo.model;

import com.serenditree.root.data.generic.model.entities.AbstractEntity;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;


public class LngLatBounds extends AbstractEntity {

    private LngLat southWest;

    private LngLat northEast;

    public LngLatBounds() {
    }

    @JsonbCreator
    public LngLatBounds(@JsonbProperty("_sw") LngLat southWest, @JsonbProperty("_ne") LngLat northEast) {
        this.southWest = southWest;
        this.northEast = northEast;
    }

    /**
     * Convenience method for getting the southernmost or top bounding - the latitude of southWest.
     *
     * @return The latitude of southWest.
     */
    @JsonbTransient
    public Double getSouth() {
        return this.southWest.getLat();
    }

    /**
     * Convenience method for getting the westernmost or left bounding - the longitude of southWest.
     *
     * @return The longitude of southWest.
     */
    @JsonbTransient
    public Double getWest() {
        return this.southWest.getLng();
    }

    /**
     * Convenience method for getting the northernmost or bottom bounding - the latitude of northEast.
     *
     * @return The latitude of northEast.
     */
    @JsonbTransient
    public Double getNorth() {
        return this.northEast.getLat();
    }

    /**
     * Convenience method for getting the easternmost or right bounding - the longitude of northEast.
     *
     * @return The longitude of northEast.
     */
    @JsonbTransient
    public Double getEast() {
        return this.northEast.getLng();
    }

    public LngLat getSouthWest() {
        return southWest;
    }

    public void setSouthWest(LngLat southWest) {
        this.southWest = southWest;
    }

    public LngLat getNorthEast() {
        return northEast;
    }

    public void setNorthEast(LngLat northEast) {
        this.northEast = northEast;
    }
}
