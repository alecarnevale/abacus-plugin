package com.alecarnevale.abacus

import com.alecarnevale.abacus.model.AbacusPluginExtension
import com.alecarnevale.abacus.task.CountClassFileTask
import com.alecarnevale.abacus.task.CountExtensionFileTask
import com.alecarnevale.abacus.task.PlotTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import xyz.ronella.gradle.plugin.simple.git.task.GitCheckout
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

private const val SIMPLE_GIT_PLUGIN_ID = "xyz.ronella.simple-git"
private const val OUTPUT_FILE_PATH = "build/abacus/output.csv"
private const val DEFAULT_TAGS_FILE_PATH = "/abacus/tags.txt"
internal const val ABACUS_PLUGIN_EXTENSION_NAME = "abacus"

class AbacusPlugin : Plugin<Project> {
  private lateinit var pluginExtension: AbacusPluginExtension
  private lateinit var countingOutputFile: File
  private lateinit var tagsFile: File

  override fun apply(project: Project) {
    project.setup()

    project.listTags(tagsFile)

    val tags = tagsFile.readLines()
    val tasks = if (tags.isEmpty()) {
      project.singleCounting()
    } else {
      with(project) {
        plotting(multipleCounting(tags))
      }
    }

    val abacusTask = project.tasks.create("abacus", AbacusTask::class.java)
    abacusTask.dependsOn(tasks)
  }

  private fun Project.setup() {
    project.pluginManager.apply(SIMPLE_GIT_PLUGIN_ID)
    pluginExtension = project.extensions.create(ABACUS_PLUGIN_EXTENSION_NAME, AbacusPluginExtension::class.java)
    val tagsFilePathDefault = project.buildDir.path + DEFAULT_TAGS_FILE_PATH
    pluginExtension.tagsFilePath.convention(tagsFilePathDefault)
    setupFiles()
  }

  private fun setupFiles() {
    countingOutputFile = File(OUTPUT_FILE_PATH)
    countingOutputFile.delete()
    val tagsFilePath = pluginExtension.tagsFilePath.get()
    val tagsParentDirPath = tagsFilePath.substringBeforeLast("/")
    Files.createDirectories(Path.of(tagsParentDirPath))
    if (!File(tagsFilePath).exists()) {
      Files.createFile(Path.of(tagsFilePath))
    }
    tagsFile = File(tagsFilePath)
  }

  /**
   * Counts only for the current codebase.
   */
  private fun Project.singleCounting(): List<TaskProvider<out Task>> {
    project.tasks.create("countClassFile", CountClassFileTask::class.java)
    project.tasks.create("countExtensionFile", CountExtensionFileTask::class.java)

    val countClassFile: TaskProvider<CountClassFileTask> =
      project.tasks.named("countClassFile", CountClassFileTask::class.java)
    val countExtensionFile: TaskProvider<CountExtensionFileTask> =
      project.tasks.named("countExtensionFile", CountExtensionFileTask::class.java)

    countClassFile.configure {
      it.supertypes.set(pluginExtension.supertypes)
      it.outputFile.set(countingOutputFile)
    }

    countExtensionFile.configure {
      it.fileExtensions.set(pluginExtension.fileExtensions)
      it.fileFolders.set(pluginExtension.fileFolders)
      it.outputFile.set(countingOutputFile)
    }

    return listOf(countClassFile, countExtensionFile)
  }

  /**
   * Counts every tags requested.
   */
  private fun Project.multipleCounting(tags: List<String>): List<TaskProvider<out Task>> {
    val resultList = mutableListOf<TaskProvider<out Task>>()

    project.tasks.create("gitCheckoutMaster", GitCheckout::class.java)
    val gitCheckoutMaster = project.tasks.named("gitCheckoutMaster", GitCheckout::class.java)
    gitCheckoutMaster.configure {
      it.branch.set("master")
    }

    repeat(tags.size) { index ->
      val currentTag = tags[index]
      val gitCheckoutTagTaskName = "gitCheckoutTag$index"
      val countClassFileTaskName = "countClassFile$index"
      val countExtensionsFileTaskName = "countExtensionFile$index"

      project.tasks.create(gitCheckoutTagTaskName, GitCheckout::class.java)
      project.tasks.create(countClassFileTaskName, CountClassFileTask::class.java)
      project.tasks.create(countExtensionsFileTaskName, CountExtensionFileTask::class.java)

      val gitCheckoutTag: TaskProvider<GitCheckout> =
        project.tasks.named(gitCheckoutTagTaskName, GitCheckout::class.java)
      gitCheckoutTag.configure {
        it.branch.set("tags/$currentTag")
      }

      val countClassFile: TaskProvider<CountClassFileTask> =
        project.tasks.named(countClassFileTaskName, CountClassFileTask::class.java)
      val countExtensionFile: TaskProvider<CountExtensionFileTask> =
        project.tasks.named(countExtensionsFileTaskName, CountExtensionFileTask::class.java)

      countClassFile.configure {
        it.supertypes.set(pluginExtension.supertypes)
        it.tag.set(currentTag)
        it.outputFile.set(countingOutputFile)
      }

      countExtensionFile.configure {
        it.fileExtensions.set(pluginExtension.fileExtensions)
        it.fileFolders.set(pluginExtension.fileFolders)
        it.tag.set(currentTag)
        it.outputFile.set(countingOutputFile)
      }

      if (resultList.isNotEmpty()) {
        gitCheckoutTag.get().dependsOn(resultList.last())
      }
      countClassFile.get().dependsOn(gitCheckoutTag.get())
      countExtensionFile.get().dependsOn(countClassFile.get())

      resultList.add(countExtensionFile)
    }

    if (resultList.isNotEmpty()) {
      gitCheckoutMaster.get().dependsOn(resultList.last())
      resultList.add(gitCheckoutMaster)
    }

    return resultList
  }

  private fun Project.plotting(taskList: List<TaskProvider<out Task>>): List<TaskProvider<out Task>> {
    project.tasks.create("abacusPlot", PlotTask::class.java)
    val abacusPlot: TaskProvider<PlotTask> = project.tasks.named("abacusPlot", PlotTask::class.java)
    abacusPlot.configure {
      it.inputFile.set(countingOutputFile)
    }
    abacusPlot.get().setDependsOn(taskList)
    return taskList.toMutableList().apply { add(abacusPlot) }
  }
}

abstract class AbacusTask : DefaultTask() {
  @TaskAction
  fun starts() {
    project.logger.log(LogLevel.LIFECYCLE, "Abacus starts")
  }
}
