package com.novoda.gradle.android.jacoco.plugin.tasks

import org.gradle.api.DefaultTask


import org.gradle.api.tasks.*

class InstrumentTask extends DefaultTask {

    String description = 'Offline instrumentation of classes for JaCoCo'

    def File classDir

    // @OutputDirectory
    def File destinationDir

    //@Input
    def String includes = "**/*.class"

    //@Input
    def String excludes = "**/R.class, android/**/BuildConfig.class, android/**/*.class"

    @TaskAction
    def instrument() {
        ant.taskdef(name: 'instrument', classname: 'org.jacoco.ant.InstrumentTask', classpath: project.configurations.jacoco.asPath)
        ant.instrument(destdir: destinationDir) {
            fileset(dir: classDir, includes: includes, excludes: excludes)
        }
    }
}