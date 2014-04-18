package com.novoda.gradle.android.jacoco.plugin.tasks

import com.android.builder.VariantConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ReportTask extends DefaultTask {

    String description = 'Add coverage to classes, installs and runs instrumentation tests'

    // @OutputDirectory
    def File destinationDir
    def variant
    def Set<String> excluded = ["**/BuildConfig.class", "**/R.class", "**/R*.class", "**/Manifest*.class"]

    @TaskAction
    def report() {
        VariantConfiguration variantConfiguration = variant.getVariantData().getVariantConfiguration()
        def sourcefilesPath = project.files(variantConfiguration.getDefaultSourceSet().getJavaDirectories()).asPath
        String jacocoreportDirPerVariant = "$project.buildDir/jacocoreport/${variantConfiguration.dirName}/"
        String excludedString = excluded.join(",");

        ant.taskdef(name: "jacocoReport", classname: 'org.jacoco.ant.ReportTask', classpath: project.configurations.jacoco.asPath)
        ant.jacocoReport {
            executiondata {
                fileset(dir: jacocoreportDirPerVariant, includes: '**/*.ec')
            }
            structure(name: variant.getVariantData().getName()) {
                classfiles {
                    fileset(dir: variant.javaCompile.destinationDir, excludes: excludedString) {
                    }
                }
                sourcefiles {
                    fileset(dir: sourcefilesPath)
                }
            }
            html(destdir: jacocoreportDirPerVariant)
        }
        getLogger().lifecycle("Report saved at: $jacocoreportDirPerVariant/index.html")
    }
}