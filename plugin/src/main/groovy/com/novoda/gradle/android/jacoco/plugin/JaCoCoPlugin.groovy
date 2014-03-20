package com.novoda.gradle.android.jacoco.plugin
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask
import com.android.build.gradle.tasks.Dex
import com.novoda.gradle.android.jacoco.plugin.tasks.InstrumentTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.compile.JavaCompile

class JaCoCoPluginExtension {
    def String version = '0.7.1-SNAPSHOT'
}

class JaCoCoPlugin implements Plugin<Project> {

    void apply(Project project) {
        def hasApp = project.plugins.hasPlugin(AppPlugin)
        def hasLib = project.plugins.hasPlugin(LibraryPlugin)
        if (!hasApp && !hasLib) {
            throw new ProjectConfigurationException("'android' or 'android-library' plugin required.")
        }

        def extension = project.android.extensions.create("jacoco", JaCoCoPluginExtension)

        final def variants
        if (hasApp) {
            variants = project.android.applicationVariants
        } else {
            variants = project.android.libraryVariants
        }

        println "-----------------------------> " + project.android.jacoco.version

        Configuration configuration = project.configurations.create("jacoco")
        project.dependencies {
            jacoco 'org.jacoco:org.jacoco.ant:' + project.android.jacoco.version
            jacoco 'org.jacoco:org.jacoco.agent:' + project.android.jacoco.version
        }
        configuration.dependencies.whenObjectAdded {
            println it.version
            if (it.version != '0.7.1-SNAPSHOT' ) {println "got it!"}
           // configuration.dependencies.remove(it)
        }


        variants.all { variant2 ->
            if (variant2.getTestVariant()) {

                JavaCompile javaCompile = variant2.javaCompile
                String variantName = variant2.getVariantData().getVariantConfiguration().fullName.capitalize()
                InstrumentTask instrument =
                        project.task("instrument${variant2.getVariantData().getVariantConfiguration().fullName.capitalize()}",
                                type: InstrumentTask) {
                            classDir = javaCompile.destinationDir
                            destinationDir = project.file("${project.buildDir}/instrumented-classes/${variant2.getVariantData().getVariantConfiguration().dirName}/")
                        }

                DeviceProviderInstrumentTestTask task = variant2.getTestVariant().getConnectedInstrumentTest()
                def connectedTestRerpo = project.task("connectedAndroidTest${variantName}Coverage", type: DeviceProviderInstrumentTestTask) {

                    plugin = task.plugin
                    variant = task.variant
                     testApp = task.testApp
                     testedApp = task.testedApp
                     reportsDir = task.reportsDir
                     resultsDir = task.resultsDir
                     flavorName = task.flavorName
                     deviceProvider = task.deviceProvider
                }

                connectedTestRerpo.dependsOn task.getTaskDependencies()


//
//                def extractAgent = project.task("${variantName}extractJacocoAgent", type: Copy) {
//                    from(project.configurations.jacoco.collect { project.zipTree(it) }) {
//                        include 'jacocoagent.jar'
//                    }
//                    into "$project.buildDir/jacocoagent/"
//                }

//                println variant.dexTask
//                Dex dex = variant.dexTask
//                dex.conventionMapping.inputFiles = {
//                    def files = project.files(instrument.destinationDir) + project.files("$project.buildDir/jacocoagent/jacocoagent.jar")
//                    files.files
//                }
//
//                dex.dependsOn(instrument, extractAgent)
//                instrument.mustRunAfter javaCompile
//
//                def inst = project.task("${variantName}instrument", type:Exec) {
//                    com.android.build.gradle.BasePlugin plugin = project.plugins.findPlugin('android')
//                    def pkg = variant.getTestVariant().getVariantData().getPackageName()
//                    def runner = variant.getTestVariant().getVariantData().getVariantConfiguration().getInstrumentationRunner()
//                    def asstring = pkg + '/' + runner
//                    commandLine plugin.extension.adbExe, 'shell', 'am', 'instrument', '-e', 'coverage', 'true', '-w', asstring
//                }
//
//                def pullingCoverageFile = project.task("${variantName}pullCoverage", type:Exec) {
//                    def f = variant.getVariantData().getPackageName()
//                    com.android.build.gradle.BasePlugin plugin = project.plugins.findPlugin('android')
//                    commandLine plugin.extension.adbExe, 'pull', "/data/data/$f/files/coverage.ec","$project.buildDir/jacocoreport/coverage.ec"
//                }
//
//                inst.dependsOn variant.getApkVariantData().installTask, variant.getTestVariant().getApkVariantData().installTask
//                pullingCoverageFile.dependsOn inst
//
//                pullingCoverageFile.doLast {
//                    ant.taskdef(name: "${variantName}jacocoReport", classname: 'org.jacoco.ant.ReportTask', classpath: project.configurations.jacoco.asPath)
//                    ant.jacocoReport {
//                        executiondata {
//                            fileset(dir: "$project.buildDir/jacocoreport/", includes: '**/*.ec')
//                        }
//                        structure(name: variant.getVariantData().getName()) {
//                            classfiles {
//                                fileset(dir: javaCompile.destinationDir)
//                            }
//                            sourcefiles {
//                                project.files(
//                                        variant.getVariantData().getVariantConfiguration().getDefaultSourceSet()
//                                                .getJavaDirectories())
//                            }
//                        }
//                        html(destdir: "$project.buildDir/jacocoreport/")
//                    }
//                    getLogger().lifecycle("Report saved at: $project.buildDir/jacocoreport/index.html")
//                }
//
//                project.task("${variantName}coverage").dependsOn pullingCoverageFile
            }
        }
    }
}