/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package scala.tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qlangtech.tis.manage.common.Config;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.tools.scala_maven_executions.JavaMainCaller;
import scala.tools.scala_maven_executions.JavaMainCallerByFork;
import scala.tools.scala_maven_executions.LogProcessorUtils;
import scala.tools.scala_maven_executions.MainHelper;
import scala.tools.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Abstract parent of all Scala Mojo who run compilation
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ScalaCompilerSupport {

    public static final String KEY_COMPILE_FAILD_FILE = "compile_faild";

    public static boolean incrStreamCodeCompileFaild(File sourceRootDir) {
        File compileFaildToken = new File(sourceRootDir, KEY_COMPILE_FAILD_FILE);
        boolean exist = compileFaildToken.exists();
        try {
            if (exist) {
                // 如果不存在需要将之前的文件夹清空一下
                org.apache.commons.io.FileUtils.deleteDirectory(sourceRootDir);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return exist;
    }

    private static final Logger logger = LoggerFactory.getLogger(ScalaCompilerSupport.class);

    private final LogProcessorUtils.LoggerListener loggerListener;

    private ScalaCompilerSupport(LogProcessorUtils.LoggerListener loggerListener) {
        this.loggerListener = loggerListener;
    }

    /**
     * 开始编译scala 脚本
     *
     * @param sourceRoot
     * @param dependencyDBNodesClasspaths
     * @param loggerListener
     * @return hasError
     * @throws Exception
     */
    public static boolean streamScriptCompile(File sourceRoot, Set<String> dependencyDBNodesClasspaths, LogProcessorUtils.LoggerListener loggerListener) throws Exception {
        ScalaCompilerSupport scalaCompiler = new ScalaCompilerSupport(loggerListener);
        List<File> sourceRootDirs = Lists.newArrayList(new File(sourceRoot, "/src/main/scala"));
        File outputDir = new File(sourceRoot, "/classes");
        Set<String> classpathElements = ScalaCompilerSupport.getStreamScriptCompilerClasspath();
        classpathElements.addAll(dependencyDBNodesClasspaths);
        boolean compileInLoop = true;
        scalaCompiler.compile(sourceRootDirs, outputDir, classpathElements, compileInLoop);
        if (scalaCompiler.hasCompileErrors()) {
            org.apache.commons.io.FileUtils.touch(new File(sourceRoot, KEY_COMPILE_FAILD_FILE));
        }
        return scalaCompiler.hasCompileErrors();
    }

    protected static final String scalaClassName = "scala.tools.nsc.Main";

    protected String[] jvmArgs;

    /**
     * Adds appropriate compiler plugins to the scalac command.
     *
     * @param scalac
     * @throws Exception
     */
    protected void addCompilerPluginOptions(JavaMainCaller scalac) throws Exception {
        for (String option : getCompilerPluginOptions()) {
            scalac.addArgs(option);
        }
    }

    private List<String> getCompilerPluginOptions() throws Exception {
        List<String> options = new ArrayList<>();
        // }
        return options;
    }

    /**
     * Additional parameter to use to call the main class. Use this parameter only
     * from command line ("-DaddScalacArgs=arg1|arg2|arg3|..."), not from pom.xml.
     * To define compiler arguments in pom.xml see the "args" parameter.
     */
    // @Parameter(property = "addScalacArgs")
    private String addScalacArgs;

    /**
     * compiler additional arguments
     */
    // @Parameter
    protected String[] args = new String[]{"-usejavacp", "-nobootcp", "-encoding", "utf8"};

    // protected String[] args = new String[]{"-help"};
    /**
     * Display the command line called ? (property 'maven.scala.displayCmd' replaced
     * by 'displayCmd')
     */
    // @Parameter(property = "displayCmd", defaultValue = "false", required = true)
    public final boolean displayCmd = true;

    /**
     * Forks the execution of scalac into a separate process.
     */
    protected boolean fork = false;

    private Logger getLog() {
        return logger;
    }

    public enum RecompileMode {

        /**
         * all sources are recompiled
         */
        all,
        /**
         * incrementally recompile modified sources and other affected sources
         */
        incremental
    }

    /**
     * Keeps track of if we get compile errors in incremental mode
     */
    private boolean compileErrors;

    /**
     * Recompile mode to use when sources were previously compiled and there is at
     * least one change, see {@link RecompileMode}.
     */
    // @Parameter(property = "recompileMode", defaultValue = "incremental")
    final RecompileMode recompileMode = RecompileMode.all;

    /**
     * notifyCompilation if true then print a message "path: compiling" for each
     * root directory or files that will be compiled. Useful for debug, and for
     * integration with Editor/IDE to reset markers only for compiled files.
     */
    // @Parameter(property = "notifyCompilation", defaultValue = "true")
    private boolean notifyCompilation;

    /**
     * Compile order for Scala and Java sources for sbt incremental compile.
     * <p>
     * Can be Mixed, JavaThenScala, or ScalaThenJava.
     */
    // @Parameter(property = "compileOrder", defaultValue = "Mixed")
    // private CompileOrder compileOrder;
    /**
     * Location of the incremental compile will install compiled compiler bridge
     * jars. Default is sbt's "~/.sbt/1.0/zinc/org.scala-sbt".
     */
    // @Parameter(property = "secondaryCacheDir")
    // private File secondaryCacheDir;
    // abstract protected File getOutputDir() throws Exception;
    // abstract protected List<String> getClasspathElements() throws Exception;
    private long _lastCompileAt = -1;

    // private SbtIncrementalCompiler incremental;
    /**
     * Analysis cache file for incremental recompilation.
     */
    // abstract protected File getAnalysisCacheFile() throws Exception;
    protected final boolean useCanonicalPath = true;

    // // @Override
    // protected void doExecute() throws Exception {
    // //    if (getLog().isDebugEnabled()) {
    // //      for (File directory : getSourceDirectories()) {
    // //        getLog().debug(FileUtils.pathOf(directory, useCanonicalPath));
    // //      }
    // //    }
    // File outputDir = FileUtils.fileOf(getOutputDir(), useCanonicalPath);
    // File analysisCacheFile = FileUtils.fileOf(getAnalysisCacheFile(), useCanonicalPath);
    // int nbFiles = compile(getSourceDirectories(), outputDir, analysisCacheFile, getClasspathElements(), false);
    // switch (nbFiles) {
    // case -1:
    // getLog().info("No sources to compile");
    // break;
    // case 0:
    // getLog().info("Nothing to compile - all classes are up to date");
    // break;
    // default:
    // break;
    // }
    // }

    /**
     * Retrieves the list of *all* root source directories. We need to pass all
     * .java and .scala files into the scala compiler
     */
    // abstract protected List<File> getSourceDirectories() throws Exception;
    public int compile(List<File> sourceRootDirs, File outputDir, Set<String> classpathElements, boolean compileInLoop) throws Exception {
        // if (!compileInLoop && recompileMode == RecompileMode.incremental) {
        // // if not compileInLoop, invoke incrementalCompile immediately
        // long n0 = System.nanoTime();
        // int res = incrementalCompile(classpathElements, sourceRootDirs, outputDir, analysisCacheFile, false);
        // getLog().info(String.format("compile in %.1f s", (System.nanoTime() - n0) / 1_000_000_000.0));
        // return res;
        // }
        long t0 = System.currentTimeMillis();
        long n0 = System.nanoTime();
        LastCompilationInfo lastCompilationInfo = LastCompilationInfo.find(sourceRootDirs, outputDir);
        if (_lastCompileAt < 0) {
            _lastCompileAt = lastCompilationInfo.getLastSuccessfullTS();
        }
        // 将会被编译的代码
        List<File> files = getFilesToCompile(sourceRootDirs, _lastCompileAt);
        if (files == null) {
            return -1;
        }
        if (files.size() < 1) {
            return 0;
        }
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        long n1 = System.nanoTime();
        long t1 = t0 + ((n1 - n0) / 1_000_000);
        // if (compileInLoop && recompileMode == RecompileMode.incremental) {
        // // if compileInLoop, do not invoke incrementalCompile when there's no change
        // int retCode = incrementalCompile(classpathElements, sourceRootDirs, outputDir, analysisCacheFile, true);
        // _lastCompileAt = t1;
        // if (retCode == 1) {
        // lastCompilationInfo.setLastSuccessfullTS(t1);
        // }
        // return retCode;
        // }
        getLog().info(String.format("Compiling %d source files to %s at %d", files.size(), outputDir.getAbsolutePath(), t1));
        JavaMainCaller jcmd = getScalaCommand();
        jcmd.redirectToLog();
        if (!classpathElements.isEmpty()) {
            jcmd.addJvmArgs("-classpath", MainHelper.toMultiPath(classpathElements));
        }
        jcmd.addArgs("-d", outputDir.getAbsolutePath());
        // jcmd.addArgs("-sourcepath", sourceDir.getAbsolutePath());
        for (File f : files) {
            jcmd.addArgs(f.getAbsolutePath());
        }
        /**
         * ======================================
         * 开始执行编译
         * =======================================
         */
        if (jcmd.run(displayCmd, !compileInLoop)) {
            lastCompilationInfo.setLastSuccessfullTS(t1);
        } else {
            compileErrors = true;
        }
        getLog().info(String.format("prepare-compile in %.1f s", (n1 - n0) / 1_000_000_000.0));
        getLog().info(String.format("compile in %.1f s", (System.nanoTime() - n1) / 1_000_000_000.0));
        _lastCompileAt = t1;
        if (compileErrors) {
        }
        return files.size();
    }

    protected JavaMainCaller getScalaCommand() throws Exception {
        return getScalaCommand(fork, scalaClassName);
    }

    /**
     * Get a {@link JavaMainCaller} used invoke a Java process. Typically this will
     * be one of the Scala utilities (Compiler, ScalaDoc, REPL, etc.).
     * <p>
     * This method does some setup on the {@link JavaMainCaller} which is not done
     * by merely invoking {@code new} on one of the implementations. Specifically,
     * it adds any Scala compiler plugin options, JVM options, and Scalac options
     * defined on the plugin.
     *
     * @param forkOverride override the setting for {@link #fork}. Currently this should only
     *                     be set if you are invoking the REPL.
     * @param mainClass    the JVM main class to invoke.
     * @return a {@link JavaMainCaller} to use to invoke the given command.
     */
    final JavaMainCaller getScalaCommand(final boolean forkOverride, final String mainClass) throws Exception {
        JavaMainCaller cmd = getEmptyScalaCommand(mainClass, forkOverride);
        cmd.addArgs(args);
        if (StringUtils.isNotEmpty(addScalacArgs)) {
            cmd.addArgs(StringUtils.split(addScalacArgs, "|"));
        }
        addCompilerPluginOptions(cmd);
        cmd.addJvmArgs(jvmArgs);
        return cmd;
    }

    /**
     * Get a {@link JavaMainCaller} used invoke a Java process. Typically this will
     * be one of the Scala utilities (Compiler, ScalaDoc, REPL, etc.).
     *
     * @param mainClass the JVM main class to invoke.
     * @return a {@link JavaMainCaller} to use to invoke the given command.
     */
    final JavaMainCaller getEmptyScalaCommand(final String mainClass) throws Exception {
        return getEmptyScalaCommand(mainClass, fork);
    }

    /**
     * Get a {@link JavaMainCaller} used invoke a Java process. Typically this will
     * be one of the Scala utilities (Compiler, ScalaDoc, REPL, etc.).
     *
     * @param mainClass    the JVM main class to invoke.
     * @param forkOverride override the setting for {@link #fork}. Currently this should only
     *                     be set if you are invoking the REPL.
     * @return a {@link JavaMainCaller} to use to invoke the given command.
     */
    private JavaMainCaller getEmptyScalaCommand(final String mainClass, final boolean forkOverride) throws Exception {
        // what's going on.
        if (forkOverride != fork) {
            getLog().info("Fork behavior overridden");
            getLog().info(String.format("Fork for this execution is %s.", String.valueOf(forkOverride)));
        }
        // TODO - Fork or not depending on configuration?
        JavaMainCaller cmd;
        // String toolcp = getToolClasspath();
        String toolcp = null;
        cmd = new JavaMainCallerByFork(mainClass, toolcp, null, null, this.loggerListener);
        // }
        return cmd;
    }

    private String getToolClasspath() throws Exception {
        Set<String> classpath = getStreamScriptCompilerClasspath();
        // }
        return MainHelper.toMultiPath(classpath.toArray(new String[]{}));
    }

    /**
     * @return
     */
    public static Set<String> getStreamScriptCompilerClasspath() {
        Set<String> classpath = new LinkedHashSet<>();
        // new File("/Users/mozhenghua/j2ee_solution/project/tis-saturn2/tis-scala-compiler-dependencies/target/dependency");
        File dependencies = new File(Config.getDataDir(), "libs/tis-scala-compiler-dependencies");
        if (!dependencies.exists() || dependencies.list().length < 1) {
            throw new IllegalStateException("dependencies list can not be null,path:" + dependencies.getAbsolutePath());
        }
        File jar = null;
        for (String child : dependencies.list()) {
            jar = new File(dependencies, child);
            if (!jar.exists()) {
                throw new IllegalStateException("file is not exist:" + jar.getAbsolutePath());
            }
            classpath.add(jar.getAbsolutePath());
        }
        logger.info("classpath:" + dependencies.getAbsolutePath() + ",childjar count:" + classpath.size());
        // return classpath;
        return Sets.newHashSet(dependencies.getAbsolutePath() + "/*");
        // return Collections.singleton();
    }

    /**
     * @param classpath
     * @throws Exception
     */
    void addLibraryToClasspath(Set<String> classpath) throws Exception {
        classpath.add(FileUtils.pathOf(getLibraryJar(), useCanonicalPath));
    }

    private static final String SCALA_LIBRARY_ARTIFACTID = "scala-library";

    private static final String SCALA_REFLECT_ARTIFACTID = "scala-reflect";

    private static final String SCALA_COMPILER_ARTIFACTID = "scala-compiler";

    protected File getLibraryJar() throws Exception {
        // return getArtifactJar(getScalaOrganization(), SCALA_LIBRARY_ARTIFACTID, findScalaVersion().toString());
        return null;
    }

    /**
     * Returns true if the previous compile failed
     */
    public boolean hasCompileErrors() {
        return compileErrors;
    }

    void clearCompileErrors() {
        compileErrors = false;
    }

    // @Parameter
    private Set<String> excludes = new HashSet<>();

    private List<File> getFilesToCompile(List<File> sourceRootDirs, long lastSuccessfulCompileTime) throws Exception {
        List<File> sourceFiles = findSourceWithFilters(sourceRootDirs);
        if (sourceFiles.size() == 0) {
            return null;
        }
        // filter uptodate
        // filter is not applied to .java, because scalac failed to used existing .class
        // for unmodified .java
        // failed with "error while loading Xxx, class file
        // '.../target/classes/.../Xxxx.class' is broken"
        // (restore how it work in 2.11 and failed in 2.12)
        // TODO a better behavior : if there is at least one .scala to compile then add
        // all .java, if there is at least one .java then add all .scala (because we
        // don't manage class dependency)
        List<File> files = new ArrayList<>(sourceFiles.size());
        if (_lastCompileAt > 0 || (recompileMode != RecompileMode.all && (lastSuccessfulCompileTime > 0))) {
            ArrayList<File> modifiedScalaFiles = new ArrayList<>(sourceFiles.size());
            ArrayList<File> modifiedJavaFiles = new ArrayList<>(sourceFiles.size());
            for (File f : sourceFiles) {
                if (f.lastModified() >= lastSuccessfulCompileTime) {
                    if (f.getName().endsWith(".java")) {
                        modifiedJavaFiles.add(f);
                    } else if (f.getName().endsWith(".scala")) {
                        modifiedScalaFiles.add(f);
                    }
                }
            }
            if ((modifiedScalaFiles.size() != 0) || (modifiedJavaFiles.size() != 0)) {
                files.addAll(sourceFiles);
                notifyCompilation(sourceRootDirs);
            }
        } else {
            files.addAll(sourceFiles);
            notifyCompilation(sourceRootDirs);
        }
        return files;
    }

    /**
     * Finds all source files in a set of directories with a given extension.
     */
    List<File> findSourceWithFilters(List<File> sourceRootDirs) throws Exception {
        List<File> sourceFiles = new ArrayList<>();
        // for existence here...
        for (File dir : sourceRootDirs) {
            // String[] tmpFiles = MainHelper.findFiles(dir, includes.toArray(new String[]{}),
            // excludes.toArray(new String[]{}));
            Collection<File> tmpFiles = org.apache.commons.io.FileUtils.listFiles(dir, new String[]{"java", "scala"}, true);
            for (File tmpLocalFile : tmpFiles) {
                File tmpAbsFile = FileUtils.fileOf(tmpLocalFile, useCanonicalPath);
                sourceFiles.add(tmpAbsFile);
            }
        }
        // scalac is sensitive to scala file order, file system can't guarantee file
        // order => unreproducible build error across platforms
        // sort files by path (OS dependent) to guarantee reproducible command line.
        Collections.sort(sourceFiles);
        return sourceFiles;
    }

    // private void initFilters() throws Exception {
    // if (includes.isEmpty()) {
    // includes.add("**/*.scala");
    // if (sendJavaToScalac && isJavaSupportedByCompiler()) {
    // includes.add("**/*.java");
    // }
    // }
    // if (!_filterPrinted && getLog().isDebugEnabled()) {
    // StringBuilder builder = new StringBuilder("includes = [");
    // for (String include : includes) {
    // builder.append(include).append(",");
    // }
    // builder.append("]");
    // getLog().debug(builder.toString());
    //
    // builder = new StringBuilder("excludes = [");
    // for (String exclude : excludes) {
    // builder.append(exclude).append(",");
    // }
    // builder.append("]");
    // getLog().debug(builder.toString());
    // _filterPrinted = true;
    // }
    // }
    private void notifyCompilation(List<File> files) throws Exception {
        if (notifyCompilation) {
            for (File f : files) {
                getLog().info(String.format("%s:-1: info: compiling", FileUtils.pathOf(f, useCanonicalPath)));
            }
        }
    }

    private static class LastCompilationInfo {

        static LastCompilationInfo find(List<File> sourceRootDirs, File outputDir) {
            StringBuilder hash = new StringBuilder();
            for (File f : sourceRootDirs) {
                hash.append(f.toString());
            }
            return new LastCompilationInfo(new File(outputDir.getAbsolutePath() + "." + hash.toString().hashCode() + ".timestamp"), outputDir);
        }

        private final File _lastCompileAtFile;

        private final File _outputDir;

        private LastCompilationInfo(File f, File outputDir) {
            _lastCompileAtFile = f;
            _outputDir = outputDir;
        }

        long getLastSuccessfullTS() {
            long back = -1;
            if (_lastCompileAtFile.exists() && _outputDir.exists() && _outputDir.list().length > 0) {
                back = _lastCompileAtFile.lastModified();
            }
            return back;
        }

        void setLastSuccessfullTS(long v) throws Exception {
            if (!_lastCompileAtFile.exists()) {
                // FileUtils.fileWrite(_lastCompileAtFile.getAbsolutePath(), ".");
                org.apache.commons.io.FileUtils.writeStringToFile(_lastCompileAtFile, ".", "utf8");
            }
            _lastCompileAtFile.setLastModified(v);
        }
    }

    //
    // Incremental compilation
    //
    // private int incrementalCompile(List<String> classpathElements, List<File> sourceRootDirs, File outputDir,
    // File cacheFile, boolean compileInLoop) throws Exception {
    // List<File> sources = findSourceWithFilters(sourceRootDirs);
    // if (sources.isEmpty()) {
    // return -1;
    // }
    //
    // // TODO - Do we really need this duplicated here?
    // if (!outputDir.exists()) {
    // outputDir.mkdirs();
    // }
    //
    // if (incremental == null) {
    // File libraryJar = getLibraryJar();
    // List<File> extraJars = getCompilerDependencies();
    // extraJars.remove(libraryJar);
    // incremental = new SbtIncrementalCompiler(//
    // libraryJar, //
    // getReflectJar(), //
    // getCompilerJar(), //
    // findScalaVersion(), //
    // extraJars, //
    // new MavenArtifactResolver(factory, session), //
    // secondaryCacheDir, //
    // getLog(), //
    // cacheFile, //
    // compileOrder);
    // }
    //
    // classpathElements.remove(outputDir.getAbsolutePath());
    // List<String> scalacOptions = getScalaOptions();
    // List<String> javacOptions = getJavacOptions();
    //
    // try {
    // incremental.compile(classpathElements, sources, outputDir, scalacOptions, javacOptions);
    // } catch (xsbti.CompileFailed e) {
    // if (compileInLoop) {
    // compileErrors = true;
    // } else {
    // throw e;
    // }
    // }
    //
    // return 1;
    // }
    // @Parameter(property = "scala.organization", defaultValue = "org.scala-lang")
    private String scalaOrganization = "org.scala-lang";

    public String getScalaOrganization() {
        return scalaOrganization;
    }
}
