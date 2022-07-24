package com.serenditree.root.rest.transfer;

import com.serenditree.root.etc.maple.Maple;

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
