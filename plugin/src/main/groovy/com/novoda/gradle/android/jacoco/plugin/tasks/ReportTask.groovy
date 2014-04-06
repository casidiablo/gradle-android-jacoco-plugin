package com.novoda.gradle.android.jacoco.plugin.tasks

import com.android.builder.VariantConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ReportTask extends DefaultTask {

    String description = 'Add coverage to classes, installs and runs instrumentation tests'

    // @OutputDirectory
    def File destinationDir
    def String excluded = "**/BuildConfig.class, **/R.class, **/R*.class, **/Manifest*.class"
    def variant

    @TaskAction
    def report() {
        VariantConfiguration variantConfiguration = variant.getVariantData().getVariantConfiguration()
        ant.taskdef(name: "jacocoReport", classname: 'org.jacoco.ant.ReportTask', classpath: project.configurations.jacoco.asPath)
        ant.jacocoReport {
            executiondata {
                fileset(dir: "$project.buildDir/jacocoreport/${variantConfiguration.dirName}/", includes: '**/*.ec')
            }
            structure(name: variant.getVariantData().getName()) {
                classfiles {
                    fileset(dir: variant.javaCompile.destinationDir, excludes: excluded) {
                    }
                }
                sourcefiles {
                    fileset(dir:
                            project.files(
                                    variantConfiguration.getDefaultSourceSet()
                                            .getJavaDirectories()).asPath)
                }
            }
            html(destdir: "$project.buildDir/jacocoreport/${variantConfiguration.dirName}/")
        }
        getLogger().lifecycle("Report saved at: $project.buildDir/jacocoreport/${variantConfiguration.dirName}/index.html")
    }
}