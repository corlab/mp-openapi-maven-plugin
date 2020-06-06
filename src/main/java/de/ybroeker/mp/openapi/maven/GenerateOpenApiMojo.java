package de.ybroeker.mp.openapi.maven;

import static io.smallrye.openapi.runtime.io.OpenApiSerializer.Format.JSON;
import static io.smallrye.openapi.runtime.io.OpenApiSerializer.Format.YAML;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JarIndexer;
import org.jboss.jandex.Result;

import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;

@Mojo(name = "generate-openapi",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateOpenApiMojo extends AbstractMojo {

    static enum OutputFormat {
        JSON, YAML, JSON_AND_YAML;

        boolean isJson() {
            return this != YAML;
        }

        boolean isYaml() {
            return this != JSON;
        }

    }

    @Parameter(defaultValue = "openapi", property = "outputFileName")
    private String outputFileName;
    @Parameter(defaultValue = "YAML", property = "outputFormat")
    private OutputFormat outputFormat;
    @Parameter(defaultValue = "${project.build.directory}/generated", property = "outputPath")
    private String outputPath;

    @Parameter(defaultValue = "false", property = "includeDependencies")
    private boolean includeDependencies;

    @Parameter(defaultValue = "compile,runtime,provided", property = "includeDependenciesScopes")
    private List<String> includeDependenciesScopes;

    @Parameter(defaultValue = "jar", property = "includeDependenciesTypes")
    private List<String> includeDependenciesTypes;

    @Parameter(defaultValue = "${project}")
    private MavenProject mavenProject;

    @Parameter(property = "config")
    private MavenConfig config;



    /**
     * Compiled classes of the project.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "classesDir")
    private File classesDir;

    @Override
    public void execute() throws MojoExecutionException {
        IndexView index = createIndex();
        OpenAPI openApi = generateSchema(index);
        write(openApi);
    }

    private IndexView createIndex() throws MojoExecutionException {
        List<IndexView> indexes = new ArrayList<>();

        try {
            indexes.add(indexModuleClasses());
        } catch (IOException e) {
            throw new MojoExecutionException("Can't compute index", e);
        }

        if (includeDependencies) {
            for (Object a : mavenProject.getArtifacts()) {
                Artifact artifact = (Artifact) a;
                if (shouldInclude(artifact)) {
                    try {
                        indexes.add(indexArtifact(artifact));
                    } catch (Exception e) {
                        getLog().error(
                                "Can't compute index of " + artifact.getFile().getAbsolutePath() + ", skipping", e);
                    }
                }
            }
        }
        return CompositeIndex.create(indexes);
    }

    private boolean shouldInclude(Artifact artifact) {
        return includeDependenciesScopes.contains(artifact.getScope())
                && includeDependenciesTypes.contains(artifact.getType());
    }

    private Index indexModuleClasses() throws IOException {
        Indexer indexer = new Indexer();

        try (Stream<Path> files = Files.walk(classesDir.toPath())) {
            List<Path> classFiles = files
                    .filter(path -> path.toString().endsWith(".class"))
                    .collect(Collectors.toList());
            for (Path path : classFiles) {
                indexClass(indexer, path);
            }
        }

        return indexer.complete();
    }

    private Index indexArtifact(Artifact artifact) throws IOException {
        getLog().debug("Indexing file " + artifact.getFile());
            Result result = JarIndexer.createJarIndex(artifact.getFile(), new Indexer(),
                    false, false, false);
            return result.getIndex();
    }

    private void indexClass(Indexer indexer, Path path) throws IOException {
        try (final InputStream inputStream = Files.newInputStream(path)) {
            indexer.index(inputStream);
        }
    }

    private OpenAPI generateSchema(IndexView index) {
        return OpenApiProcessor.modelFromAnnotations(config, index);
    }

    private void write(OpenAPI openApi) throws MojoExecutionException {
        try {
            Path destinationPath = Paths.get(outputPath);
            Files.createDirectories(destinationPath);

            if (outputFormat.isJson()) {
                Path file = destinationPath.resolve(outputFileName + ".json");
                Files.write(file, Collections.singleton(OpenApiSerializer.serialize(openApi, JSON)));
                getLog().info("Wrote the schema to " + file.toAbsolutePath().toString());
            }

            if (outputFormat.isYaml()) {
                Path file = destinationPath.resolve(outputFileName + ".yaml");
                Files.write(file, Collections.singleton(OpenApiSerializer.serialize(openApi, YAML)));
                getLog().info("Wrote the schema to " + file.toAbsolutePath().toString());
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Can't write the result", e);
        }
    }

}
