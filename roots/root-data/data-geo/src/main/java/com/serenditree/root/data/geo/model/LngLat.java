package com.serenditree.root.data.geo.model;

import com.serenditree.root.data.generic.model.entities.AbstractEntity;

import javax.persistence.Embeddable;

/**
 * Represents a geographical point. Latitude defines the position on the north-south-axis and longitude the position
 * on the east-west-axis.
 */
@Embeddable
public class LngLat extends AbstractEntity {

    private Double lng;

    private Double lat;

    public LngLat() {
    }

    public LngLat(Double lng, Double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }
}
