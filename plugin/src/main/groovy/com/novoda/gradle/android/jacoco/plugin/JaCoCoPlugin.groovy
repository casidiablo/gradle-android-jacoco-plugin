package com.novoda.gradle.android.jacoco.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.build.gradle.tasks.Dex
import com.android.builder.VariantConfiguration
import com.android.builder.testing.api.DeviceAction
import com.android.builder.testing.api.DeviceConnector
import com.novoda.gradle.android.jacoco.plugin.tasks.InstrumentTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.Copy
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

        Configuration configuration = project.configurations.create("jacoco")
        project.dependencies {
            jacoco 'org.jacoco:org.jacoco.ant:' + project.android.jacoco.version
            jacoco 'org.jacoco:org.jacoco.agent:' + project.android.jacoco.version
        }

        variants.all { variant ->
            if (variant.getTestVariant()) {

                BaseVariantData variantData = variant.getVariantData()
                VariantConfiguration variantConfiguration = variantData.getVariantConfiguration()
                String variantName = variantConfiguration.fullName.capitalize()
                Map<String, String> instrumentationOptions = variantConfiguration.getInstrumentationOptions()
                variantConfiguration.instrumentationOptions.put("coverage", "true")

                JavaCompile javaCompile = variant.javaCompile

                InstrumentTask instrument = project.task("instrument${variantName}", type: InstrumentTask)
                instrument.classDir = javaCompile.destinationDir
                instrument.destinationDir = project.file("${project.buildDir}/instrumented-classes/${variantConfiguration.dirName}/")

                String deviceCoverageLocation = instrumentationOptions.coverageFile ?: "/data/data/${variantData.getPackageName()}/files/coverage.ec"

                project.logger.info("Will fetch from : " + deviceCoverageLocation);

                DeviceProviderInstrumentTestTask task = variant.getTestVariant().getConnectedInstrumentTest()
                DeviceProviderInstrumentTestTask testCoverageTask = project.task("connectedAndroidCoverageTest${variantName}", type: DeviceProviderInstrumentTestTask)
                testCoverageTask.plugin = task.plugin
                testCoverageTask.variant = task.variant
                testCoverageTask.testApp = task.testApp
                testCoverageTask.testedApp = task.testedApp
                testCoverageTask.reportsDir = task.reportsDir
                testCoverageTask.resultsDir = task.resultsDir
                testCoverageTask.flavorName = task.flavorName
                testCoverageTask.deviceProvider = task.deviceProvider
                testCoverageTask.scrubDevice += new DeviceAction() {
                    void apply(DeviceConnector device) {
                        device.pullFile(deviceCoverageLocation, "$project.buildDir/jacocoreport/coverage.ec")
                    }
                }

                testCoverageTask.dependsOn task.taskDependencies
                testCoverageTask.group = JavaBasePlugin.VERIFICATION_GROUP
                testCoverageTask.description = "Add coverage to classes, installs and runs instrumentation tests"

                def extractAgent = project.task("${variantName}extractJacocoAgent", type: Copy) {
                    from(project.configurations.jacoco.collect { project.zipTree(it) }) {
                        include 'jacocoagent.jar'
                    }
                    into "$project.buildDir/jacocoagent/"
                }

                Dex dex = variant.getApkVariantData().dexTask
                dex.conventionMapping.inputFiles = {
                    def files = project.files(instrument.destinationDir) + project.files("$project.buildDir/jacocoagent/jacocoagent.jar")
                    files.files
                }

                dex.dependsOn(instrument, extractAgent)
                instrument.mustRunAfter javaCompile

                testCoverageTask.doLast {
                    ant.taskdef(name: "jacocoReport", classname: 'org.jacoco.ant.ReportTask', classpath: project.configurations.jacoco.asPath)
                    ant.jacocoReport {
                        executiondata {
                            fileset(dir: "$project.buildDir/jacocoreport/", includes: '**/*.ec')
                        }
                        structure(name: variant.getVariantData().getName()) {
                            classfiles {
                                fileset(dir: javaCompile.destinationDir) {
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
        }
    }
}