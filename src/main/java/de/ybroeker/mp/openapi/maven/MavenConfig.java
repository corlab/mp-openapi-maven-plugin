package de.ybroeker.mp.openapi.maven;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import io.smallrye.openapi.api.OpenApiConfig;
import org.apache.maven.plugins.annotations.Parameter;

public class MavenConfig implements OpenApiConfig {

    @Parameter(property = "modelReader")
    String modelReader;

    @Parameter(property = "filter")
    String filter;

    @Parameter(property = "scanDisable")
    boolean scanDisable;

    @Parameter(property = "scanDependenciesDisable")
    boolean scanDependenciesDisable;

    @Parameter(property = "schemaReferencesEnable")
    boolean schemaReferencesEnable;

    @Parameter(property = "customSchemaRegistryClass")
    String customSchemaRegistryClass;

    @Parameter(property = "applicationPathDisable")
    boolean applicationPathDisable;

    @Parameter(property = "servers")
    Set<String> servers;

    @Parameter(property = "pathServers")
    Map<String, Servers> pathServers;

    @Parameter(property = "operationServers")
    Map<String, Servers> operationServers;

    @Parameter(property = "scanDependenciesJars")
    Set<String> scanDependenciesJars;

    @Parameter(property = "schemas")
    Map<String, String> schemas;

    @Parameter(property = "scanPackages")
    Set<String> scanPackages;
    @Parameter(property = "scanClasses")
    Set<String> scanClasses;
    @Parameter(property = "scanExcludePackages")
    Set<String> scanExcludePackages;
    @Parameter(property = "scanExcludeClasses")
    Set<String> scanExcludeClasses;


    @Override
    public String modelReader() {
        return modelReader;
    }

    @Override
    public String filter() {
        return filter;
    }

    @Override
    public boolean scanDisable() {
        return scanDisable;
    }

    @Override
    public Set<String> scanPackages() {
        if (scanPackages == null) {
            return Collections.emptySet();
        }
        return scanPackages;
    }

    @Override
    public Set<String> scanClasses() {
        if (scanClasses == null) {
            return Collections.emptySet();
        }
        return scanClasses;
    }

    @Override
    public Set<String> scanExcludePackages() {
        if (scanExcludePackages == null) {
            return Collections.emptySet();
        }
        return scanExcludePackages;
    }

    @Override
    public Set<String> scanExcludeClasses() {
        if (scanExcludeClasses == null) {
            return Collections.emptySet();
        }
        return scanExcludeClasses;
    }

    @Override
    public Set<String> servers() {
        if (servers == null) {
            return Collections.emptySet();
        }
        return servers;
    }

    @Override
    public Set<String> pathServers(final String path) {
        if (pathServers == null) {
            return Collections.emptySet();
        }
        Servers servers = pathServers.get(path);
        if (servers != null) {
            return servers.getServers();
        }
        return Collections.emptySet();
    }

    @Override
    public Set<String> operationServers(final String operationId) {
        if (operationServers == null) {
            return Collections.emptySet();
        }
        Servers servers = operationServers.get(operationId);
        if (servers != null) {
            return servers.getServers();
        }
        return Collections.emptySet();
    }

    @Override
    public boolean scanDependenciesDisable() {
        return scanDependenciesDisable;
    }

    @Override
    public Set<String> scanDependenciesJars() {
        if (scanDependenciesJars == null) {
            return Collections.emptySet();
        }
        return scanDependenciesJars;
    }

    @Override
    public boolean schemaReferencesEnable() {
        return schemaReferencesEnable;
    }

    @Override
    public String customSchemaRegistryClass() {
        return customSchemaRegistryClass;
    }

}
