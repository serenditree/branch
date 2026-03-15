package io.serenditree.leaf.seed.config;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = {
    io.quarkiverse.opensearch.transport.apache.ApacheHttpTransportProvider.class,
    io.quarkiverse.opensearch.transport.spi.OpenSearchTransportProvider.class,
    com.fasterxml.jackson.datatype.jsr310.JavaTimeModule.class,
    com.fasterxml.jackson.datatype.jdk8.Jdk8Module.class,
    com.fasterxml.jackson.module.paramnames.ParameterNamesModule.class
})
public class NativeConfig {
}
