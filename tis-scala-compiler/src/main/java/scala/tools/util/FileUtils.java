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
package scala.tools.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public final class FileUtils {

    private FileUtils() {
    }

    public static File fileOf(File f, boolean canonical) throws Exception {
        return canonical ? f.getCanonicalFile() : f.getAbsoluteFile();
    }

    /**
     * @param canonical
     *            Should use CanonicalPath to normalize path (true =>
     *            getCanonicalPath, false =&gt; getAbsolutePath)
     * @see <a href="https://github.com/davidB/maven-scala-plugin/issues/50">#50</a>
     */
    public static String pathOf(File f, boolean canonical) throws Exception {
        return canonical ? f.getCanonicalPath() : f.getAbsolutePath();
    }

    public static List<File> listDirectoryContent(Path directory, Function<File, Boolean> filter) throws IOException {
        List<File> files = new ArrayList<>();
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                File f = file.toFile();
                if (filter.apply(f)) {
                    files.add(f);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                File f = dir.toFile();
                if (!dir.equals(directory) && filter.apply(f)) {
                    files.add(f);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return files;
    }

    public static void deleteDirectory(Path directory) {
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
        // life...
        }
    }
}
