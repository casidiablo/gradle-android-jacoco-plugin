package com.novoda.gradle.android.jacoco.plugin.tasks

import org.gradle.api.DefaultTask


import org.gradle.api.tasks.*

class InstrumentTask extends DefaultTask {

    String description = 'Offline instrumentation of classes for JaCoCo'

    def File classDir

    // @OutputDirectory
    def File destinationDir

    //@Input
    def Set<String> includes = ['**/*.class']

    //@Input
    def Set<String> excludes = ['**/R.class', "**/BuildConfig.class", "android/**/*.class"]

    @TaskAction
    def instrument() {
        ant.taskdef(name: 'instrument', classname: 'org.jacoco.ant.InstrumentTask', classpath: project.configurations.jacoco.asPath)
        ant.instrument(destdir: destinationDir) {
            fileset(dir: classDir, includes: '**/*.class', excludes: excludes)
        }
    }
}