package com.novoda.gradle.android.jacoco.plugin.tasks
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class ReportTask extends DefaultTask {

    String description = 'Add coverage to classes, installs and runs instrumentation tests'

    // @OutputDirectory
    def File destinationDir

    Project project
    def variant

    @TaskAction
    def report() {
        ant.taskdef(name: "jacocoReport", classname: 'org.jacoco.ant.ReportTask', classpath: project.configurations.jacoco.asPath)
        ant.jacocoReport {
            executiondata {
                fileset(dir: "$project.buildDir/jacocoreport/", includes: '**/*.ec')
            }
            structure(name: variant.getVariantData().getName()) {
                classfiles {
                    fileset(dir: variant.javaCompile.destinationDir) {
                    }
                }
                sourcefiles {
                    fileset(dir:
                            project.files(
                                    variant.getVariantData().getVariantConfiguration().getDefaultSourceSet()
                                            .getJavaDirectories()).asPath)
                }
            }
            html(destdir: "$project.buildDir/jacocoreport/")
        }
        getLogger().lifecycle("Report saved at: $project.buildDir/jacocoreport/index.html")
    }
}