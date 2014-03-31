package com.novoda.gradle.android.jacoco.plugin.tasks;

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskAction;

class ReportTask extends DefaultTask {

    String description = 'Offline instrumentation of classes for JaCoCo'

    // @OutputDirectory
    def File destinationDir


    FileCollection executionDataFiles
    FileCollection sourceFiles
    FileCollection classFiles


    String name

    @TaskAction
    def report() {
        ant.taskdef(name: 'jacocoReport', classname: 'org.jacoco.ant.ReportTask', classpath: project.configurations.jacoco.asPath)
        ant.jacocoReport {
            executiondata {
                fileset(dir: '/home/acsia/dev/gradle/gradle-android-jacoco-plugin/', includes: '**/*.ec')
            }
            structure(name: name) {
                classfiles { classFiles }
                sourcefiles { sourceFiles }
            }
            html(destdir: '/tmp/report-Jacoco')
        }
    }
}