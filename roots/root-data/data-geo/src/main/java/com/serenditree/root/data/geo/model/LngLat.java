package com.serenditree.root.data.geo.model;

import com.serenditree.root.data.generic.model.entities.AbstractEntity;

import javax.persistence.Embeddable;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Represents a geographical point. Latitude defines the position on the north-south-axis and longitude the position
 * on the east-west-axis.
 */
@Embeddable
public class LngLat extends AbstractEntity {

    @Size(min = -180, max = 180)
    private Double lng;

    @Size(min = -90, max = 90)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LngLat lngLat = (LngLat) o;
        return Objects.equals(lng, lngLat.lng) && Objects.equals(lat, lngLat.lat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lng, lat);
    }
}
