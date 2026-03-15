package com.serenditree.root.data.geo.model;

import com.serenditree.root.data.generic.model.entities.AbstractEntity;
import jakarta.annotation.Generated;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Represents a geographical point. Latitude defines the position on the north-south-axis and longitude the position
 * on the east-west-axis.
 */
@Embeddable
public class LonLat extends AbstractEntity {

    @Size(min = -180, max = 180)
    private Double lon;

    @Size(min = -90, max = 90)
    private Double lat;

    public LonLat() {
    }

    public LonLat(Double lon, Double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    @Override
    @Generated("IDE")
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LonLat lngLat = (LonLat) o;
        return Objects.equals(lon, lngLat.lon) && Objects.equals(lat, lngLat.lat);
    }

    @Override
    @Generated("IDE")
    public int hashCode() {
        return Objects.hash(lon, lat);
    }
}
