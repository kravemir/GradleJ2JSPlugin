package org.kravemir.gradle.j2js;

import com.github.marschall.pathclassloader.PathClassLoader;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.teavm.tooling.ClassAlias;
import org.teavm.tooling.MethodAlias;
import org.teavm.tooling.TeaVMTool;
import org.teavm.tooling.TeaVMToolException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miroslav Kravec
 */
public class J2JSTask extends DefaultTask {

    private File outDir;
    private String targetFilename;

    private FileCollection libraryClassPath;

    private List<ClassAlias> classAliases = new ArrayList<>();
    private List<MethodAlias> methodAliases = new ArrayList<>();

    @TaskAction
    void compile() {
        try {
            // TODO: fine classloader configuration
            ClassLoader classLoader = new PathClassLoader(
                    Paths.get(getProject().getBuildDir().getAbsolutePath(),"classes/main"),
                    new URLClassLoader(
                            libraryClassPath.getFiles().stream().map(file -> {
                                try {
                                    return file.toURI().toURL();
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
                                return (URL)null;
                            }).toArray(URL[]::new),
                            J2JSTask.class.getClassLoader()
                    )
            );
            TeaVMTool tool = new TeaVMTool();
            tool.setClassLoader(classLoader);
            tool.setMinifying(false);
            tool.getClassAliases().addAll(classAliases);
            tool.getMethodAliases().addAll(methodAliases);
            tool.setTargetDirectory(outDir);
            tool.setTargetFileName(targetFilename);
            tool.setSourceMapsFileGenerated(true);
            tool.generate();
        } catch (TeaVMToolException e) {
            e.printStackTrace();
        }
    }

    public void classAlias(String aliasName, String className) {
        ClassAlias classAlias = new ClassAlias();
        classAlias.setAlias(aliasName);
        classAlias.setClassName(className);
        classAliases.add(classAlias);
    }

    public void methodAlias(String aliasName, String className, String methodName, String methodDescriptor, List<String> types) {
        MethodAlias alias = new MethodAlias();
        alias.setAlias(aliasName);
        alias.setClassName(className);
        alias.setMethodName(methodName);
        alias.setDescriptor(methodDescriptor);
        if(types != null)
            alias.setTypes(types.toArray(new String[types.size()]));
        methodAliases.add(alias);
    }

    @Input
    public File getOutDir() {
        return outDir;
    }

    public void setOutDir(File outDir) {
        this.outDir = outDir;
    }

    public void setOutDir(Object outDirObj) {
        this.outDir = getProject().file(outDirObj);
    }

    public String getTargetFilename() {
        return targetFilename;
    }

    public void setTargetFilename(String targetFilename) {
        this.targetFilename = targetFilename;
    }

    public FileCollection getLibraryClassPath() {
        return libraryClassPath;
    }

    public void setLibraryClassPath(FileCollection libraryClassPath) {
        this.libraryClassPath = libraryClassPath;
    }
}
