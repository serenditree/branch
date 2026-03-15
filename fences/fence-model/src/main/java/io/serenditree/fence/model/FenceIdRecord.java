package io.serenditree.fence.model;

import io.serenditree.root.data.generic.model.entities.AbstractEntity;
import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class FenceIdRecord extends AbstractEntity {

    @Id
    String subject;

    public FenceIdRecord() {
    }

    public FenceIdRecord(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    @Generated("IDE")
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FenceIdRecord that = (FenceIdRecord) o;
        return Objects.equals(subject, that.subject);
    }

    @Override
    @Generated("IDE")
    public int hashCode() {
        return Objects.hashCode(subject);
    }
}
