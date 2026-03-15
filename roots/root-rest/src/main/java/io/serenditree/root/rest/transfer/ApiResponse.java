package io.serenditree.root.rest.transfer;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.serenditree.root.util.maple.Maple;

@RegisterForReflection
public class ApiResponse {

    private String message;

    public ApiResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return Maple.json(this);
    }
}
