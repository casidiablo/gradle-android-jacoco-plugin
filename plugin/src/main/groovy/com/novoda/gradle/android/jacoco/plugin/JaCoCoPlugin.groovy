package com.novoda.gradle.android.jacoco.plugin;

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class JaCoCoPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        def hasApp = project.plugins.hasPlugin(AppPlugin)
        def hasLib = project.plugins.hasPlugin(LibraryPlugin)
        if (!hasApp && !hasLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }

        final def log = project.logger
        final def variants
        final def plugin
        if (hasApp) {
            variants = project.android.applicationVariants
            plugin = project.plugins.getPlugin(AppPlugin)
        } else {
            variants = project.android.libraryVariants
            plugin = project.plugins.getPlugin(LibraryPlugin)
        }

        project.dependencies {
            debugCompile 'org.jacoco:org.jacoco.agent:0.6.6-SNAPSHOT'
        }

        variants.all { variant ->
            if (!variant.buildType.isDebuggable()) {
                log.debug("Skipping non-debuggable build type '${variant.buildType.name}'.")
                return;
            }

            JavaCompile javaCompile = variant.javaCompile
            javaCompile.doLast {
                File original = new File(javaCompile.destinationDir.getParent(), 'original-classes')

//                project.copy {
//                    from javaCompile.destinationDir
//                    into original
//                }

                println 'jacoco against ' + javaCompile.destinationDir
                ant.taskdef(name: 'jacoco', classname: 'org.jacoco.ant.InstrumentTask', classpath: project.configurations.jacoco.asPath)
                ant.jacoco(destdir: javaCompile.destinationDir) {
                    fileset (dir: original, includes: '**/*.class')
                }
            }
        }
    }
}
