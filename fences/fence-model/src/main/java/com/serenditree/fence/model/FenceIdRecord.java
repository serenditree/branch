package com.serenditree.fence.model;

import com.serenditree.root.data.generic.model.entities.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

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
}
