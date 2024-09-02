package org.kvisha.reportgenerator.model

import com.intellij.openapi.project.Project
import java.io.File

object TestProjectFinder {
    fun findTestProjects(project: Project): List<TestProject> {
        val projectDir = File(project.basePath ?: return emptyList())
        return projectDir.walkTopDown()
            .filter { it.isDirectory && hasTestSdk(it) }
            .map { TestProject(it.name, it.absolutePath) }
            .toList()
    }

    private fun hasTestSdk(directory: File): Boolean {
        val csprojFile = directory.listFiles { file -> file.extension == "csproj" }?.firstOrNull() ?: return false
        return csprojFile.readText().contains("Microsoft.NET.Test.Sdk")
    }
}