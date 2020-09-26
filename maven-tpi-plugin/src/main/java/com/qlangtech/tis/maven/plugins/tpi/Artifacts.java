/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.maven.plugins.tpi;

import com.google.common.base.Predicate;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import java.util.*;

/**
 * Collection filter operations on a set of {@link Artifact}s.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class Artifacts extends ArrayList<Artifact> {

    public Artifacts() {
    }

    public Artifacts(Collection<? extends Artifact> c) {
        super(c);
    }

    /**
     * Return the {@link Artifact}s representing dependencies of the given project.
     *
     * A thin-wrapper of p.getArtifacts()
     */
    public static Artifacts of(MavenProject p) {
        return new Artifacts(p.getArtifacts());
    }

    public static Artifacts ofDirectDependencies(MavenProject p) {
        return new Artifacts(p.getDependencyArtifacts());
    }

    public Artifacts retainAll(Predicate<Artifact> filter) {
        for (Iterator<Artifact> itr = iterator(); itr.hasNext(); ) {
            if (!filter.apply(itr.next()))
                itr.remove();
        }
        return this;
    }

    public Artifacts removeAll(Predicate<Artifact> filter) {
        for (Iterator<Artifact> itr = iterator(); itr.hasNext(); ) {
            if (filter.apply(itr.next()))
                itr.remove();
        }
        return this;
    }

    public Artifacts scopeIs(String... scopes) {
        final List<String> s = Arrays.asList(scopes);
        return retainAll(new Predicate<Artifact>() {

            public boolean apply(Artifact a) {
                return s.contains(a.getScope());
            }
        });
    }

    public Artifacts scopeIsNot(String... scopes) {
        final List<String> s = Arrays.asList(scopes);
        return removeAll(new Predicate<Artifact>() {

            public boolean apply(Artifact a) {
                return s.contains(a.getScope());
            }
        });
    }

    public Artifacts typeIs(String... type) {
        final List<String> s = Arrays.asList(type);
        return retainAll(new Predicate<Artifact>() {

            public boolean apply(Artifact a) {
                return s.contains(a.getType());
            }
        });
    }

    public Artifacts typeIsNot(String... type) {
        final List<String> s = Arrays.asList(type);
        return removeAll(new Predicate<Artifact>() {

            public boolean apply(Artifact a) {
                return s.contains(a.getType());
            }
        });
    }

    public Artifacts groupIdIs(String... groupId) {
        final List<String> s = Arrays.asList(groupId);
        return retainAll(new Predicate<Artifact>() {

            public boolean apply(Artifact a) {
                return s.contains(a.getType());
            }
        });
    }

    public Artifacts groupIdIsNot(String... groupId) {
        final List<String> s = Arrays.asList(groupId);
        return removeAll(new Predicate<Artifact>() {

            public boolean apply(Artifact a) {
                return s.contains(a.getType());
            }
        });
    }

    public Artifacts artifactIdIs(String... artifactId) {
        final List<String> s = Arrays.asList(artifactId);
        return retainAll(new Predicate<Artifact>() {

            public boolean apply(Artifact a) {
                return s.contains(a.getType());
            }
        });
    }

    public Artifacts artifactIdIsNot(String... artifactId) {
        final List<String> s = Arrays.asList(artifactId);
        return removeAll(new Predicate<Artifact>() {

            public boolean apply(Artifact a) {
                return s.contains(a.getType());
            }
        });
    }
}
