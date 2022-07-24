package com.serenditree.root.rest.transfer;

import com.serenditree.root.etc.maple.Maple;

import java.io.Serializable;
import java.util.Map;

public class ApiDescription implements Serializable {

    private String serviceName;

    private String artifactVersion;

    private String stage;

    private Map<String, String> api;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getArtifactVersion() {
        return artifactVersion;
    }

    public void setArtifactVersion(String artifactVersion) {
        this.artifactVersion = artifactVersion;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Map<String, String> getApi() {
        return api;
    }

    public void setApi(Map<String, String> api) {
        this.api = api;
    }

    @Override
    public String toString() {
        return Maple.json(this);
    }
}
