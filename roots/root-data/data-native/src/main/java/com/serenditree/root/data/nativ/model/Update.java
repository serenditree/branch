package com.serenditree.root.data.nativ.model;

import org.bson.Document;
import org.bson.conversions.Bson;

public class Update {

    private final Bson filter;

    private final Bson document;

    private Update(Bson filter, Bson document) {
        this.filter = filter;
        this.document = document;
    }

    public static Update set(Bson filter, Bson document) {
        return new Update(filter, new Document("$set", document));
    }

    public static Update push(Bson filter, Bson document) {
        return new Update(filter, new Document("$push", document));
    }

    public Bson getFilter() {
        return filter;
    }

    public Bson getDocument() {
        return document;
    }
}
