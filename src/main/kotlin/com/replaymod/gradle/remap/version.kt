package com.replaymod.gradle.remap

import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.cli.common.config.KotlinSourceRoot
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.psi.KtFile
import java.nio.file.Path

fun analyze(
    environment: KotlinCoreEnvironment,
    ktFiles: List<KtFile>
): AnalysisResult {
    return try {
        analyze1521(environment, ktFiles)
    } catch (e: Throwable) {
        analyze1620(environment, ktFiles)
    }
}

fun createSourceRoot(
    tempDir: String,
    isCommon: Boolean
): KotlinSourceRoot {
    return try {
        KotlinSourceRoot(tempDir, isCommon)
    } catch (e: Throwable) {
        createSourceRoot190(tempDir, isCommon)
    }
}
