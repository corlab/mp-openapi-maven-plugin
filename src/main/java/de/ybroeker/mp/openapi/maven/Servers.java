package de.ybroeker.mp.openapi.maven;

import java.util.Set;

import org.apache.maven.plugins.annotations.Parameter;

public class Servers {

    @Parameter(name = "servers")
    Set<String> servers;

    public Set<String> getServers() {
        return servers;
    }
}
