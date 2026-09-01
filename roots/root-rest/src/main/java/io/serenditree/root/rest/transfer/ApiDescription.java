package io.serenditree.root.rest.transfer;

import io.serenditree.root.util.maple.Maple;

import java.io.Serializable;

public class ApiDescription implements Serializable {

    private String service;
    private String version;
    private String stage;
    private String openapi;
    private String openapiJson;
    private String swagger;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getOpenapi() {
        return openapi;
    }

    public void setOpenapi(String openapi) {
        this.openapi = openapi;
    }

    public String getOpenapiJson() {
        return openapiJson;
    }

    public void setOpenapiJson(String openapiJson) {
        this.openapiJson = openapiJson;
    }

    public String getSwagger() {
        return swagger;
    }

    public void setSwagger(String swagger) {
        this.swagger = swagger;
    }

    @Override
    public String toString() {
        return Maple.json(this);
    }
}
