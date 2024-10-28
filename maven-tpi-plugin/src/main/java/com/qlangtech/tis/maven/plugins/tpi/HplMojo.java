/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.maven.plugins.tpi;

import com.qlangtech.tis.manage.common.Config;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.Manifest.Attribute;
import org.codehaus.plexus.archiver.jar.ManifestException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-10-26 08:13
 **/
@Mojo(name = "hpl", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class HplMojo extends AbstractTISManifestMojo {
//    /**
//     * Path to {@code $JENKINS_HOME}. A .hpl file will be generated to this location.
//     *
//     * @deprecated Use {@link #jenkinsHome}.
//     */
//    @Deprecated
//    @Parameter(property = "hudsonHome")
//    private File hudsonHome;

    /**
     * Path to {@code $JENKINS_HOME}. A .hpl file will be generated to this location.
     */
    @Parameter(property = "tisDataDir")
    private File tisDataDir;

//    @Deprecated
//    public void setHudsonHome(File hudsonHome) {
//        this.hudsonHome = null;
//        this.jenkinsHome = hudsonHome;
//    }
//
//    public void setJenkinsHome(File jenkinsHome) {
//        this.hudsonHome = null;
//        this.jenkinsHome = jenkinsHome;
//    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!project.getPackaging().equals(PluginClassifier.PACAKGE_TPI_EXTENSION_NAME)) {
            getLog().info("Skipping " + project.getName() + " because it's not <packaging>" + PluginClassifier.PACAKGE_TPI_EXTENSION_NAME + "</packaging>");
            return;
        }
        if (tisDataDir == null) {
            getLog().warn(
                    "Please use the `tisDataDir` configuration parameter in place of the deprecated `tisDataDir` parameter");
        }

        File hplFile = computeHplFile();
        getLog().info("Generating " + hplFile);

        try (PrintWriter printWriter =
                     new PrintWriter(Files.newBufferedWriter(hplFile.toPath(), StandardCharsets.UTF_8))) {
            Manifest mf = new Manifest();
            Manifest.ExistingSection mainSection = mf.getMainSection();
            setAttributes(mainSection);

            // compute Libraries entry
            List<String> paths = new ArrayList<>();

            // we want resources to be picked up before target/classes,
            // so that the original (not in the copy) will be picked up first.
            for (Resource r : project.getBuild().getResources()) {
                File dir = new File(r.getDirectory());
                if (!dir.isAbsolute()) {
                    dir = new File(project.getBasedir(), r.getDirectory());
                }
                if (dir.exists()) {
                    paths.add(dir.getPath());
                }
            }

            paths.add(project.getBuild().getOutputDirectory());

            buildLibraries(paths);

            mainSection.addAttributeAndCheck(new Attribute("Libraries", String.join(",", paths)));

            // compute Resource-Path entry
            mainSection.addAttributeAndCheck(new Attribute("Resource-Path", warSourceDirectory.getAbsolutePath()));

            mf.write(printWriter);
        } catch (ManifestException | IOException e) {
            throw new MojoExecutionException("Error preparing the manifest: " + e.getMessage(), e);
        }
    }

    /**
     * Compute library dependencies.
     *
     * <p>
     * The list produced by this function and the list of jars that the 'hpi' mojo
     * puts into WEB-INF/lib should be the same so that the plugins see consistent
     * environment.
     */
    private void buildLibraries(List<String> paths) throws IOException, MojoExecutionException {
        Set<MavenArtifact> artifacts = getProjectArtfacts();

        // List up IDs of Jenkins plugin dependencies
        Set<String> jenkinsPlugins = new HashSet<>();
        for (MavenArtifact artifact : artifacts) {
            if (artifact.isPluginBestEffort(getLog())) {
                jenkinsPlugins.add(artifact.getId());
            }
        }

        OUTER:
        for (MavenArtifact artifact : artifacts) {
            if (jenkinsPlugins.contains(artifact.getId())) {
                continue; // plugin dependencies
            }
            if (artifact.getDependencyTrail().size() < 2) {
                throw new IllegalStateException("invalid dependency trail: " + artifact.getDependencyTrail());
            }
            if (artifact.getDependencyTrail().size() >= 1
                    && jenkinsPlugins.contains(artifact.getDependencyTrail().get(1))) {
                continue; // no need to have transitive dependencies through plugins
            }

            // if the dependency goes through jenkins core, that's not a library
            for (String trail : artifact.getDependencyTrail()) {
                if (trail.contains(":hudson-core:") || trail.contains(":jenkins-core:")) {
                    continue OUTER;
                }
            }

            ScopeArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
            if (!artifact.isOptional() && filter.include(artifact.artifact)) {
                paths.add(artifact.getFile().getPath());
            }
        }
    }

    /**
     * Determine where to produce the .hpl file.
     */
    protected File computeHplFile() throws MojoExecutionException {
        if (tisDataDir == null) {
            throw new MojoExecutionException(
                    "Property tisDataDir needs to be set to $JENKINS_HOME. Please use 'mvn -DtisDataDir=...' or "
                            + "put <settings><profiles><profile><properties><property><tisDataDir>...</...>");
        }


        File hplFile = new File(tisDataDir, Config.LIB_PLUGINS_PATH + "/" + project.getBuild().getFinalName() + ".hpl");
        return hplFile;
    }
}
