package com.alecarnevale.abacus

import com.alecarnevale.abacus.model.AbacusPluginExtension
import com.alecarnevale.abacus.task.CountClassFileTask
import com.alecarnevale.abacus.task.CountExtensionFileTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File

private const val SIMPLE_GIT_PLUGIN_ID = "xyz.ronella.simple-git"
private const val OUTPUT_FILE_PATH = "build/abacus/output.csv"
private val startingTagDefault: String? = null

class AbacusPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.pluginManager.apply(SIMPLE_GIT_PLUGIN_ID)
    val extension = project.extensions.create("abacus", AbacusPluginExtension::class.java)
    extension.startingTag.convention(startingTagDefault)
    val outputFile = File(OUTPUT_FILE_PATH)
    outputFile.delete()

    project.tasks.create("countClassFile", CountClassFileTask::class.java)
    project.tasks.create("countExtensionFile", CountExtensionFileTask::class.java)

    val countClassFile: TaskProvider<CountClassFileTask> = project.tasks.named("countClassFile", CountClassFileTask::class.java)
    val countExtensionFile: TaskProvider<CountExtensionFileTask> = project.tasks.named("countExtensionFile", CountExtensionFileTask::class.java)

    countClassFile.configure {
      it.supertypes.set(extension.supertypes)
      it.tag.set(extension.startingTag)
      it.outputFile.set(outputFile)
    }

    countExtensionFile.configure {
      it.fileDescriptors.set(extension.fileDescriptors)
      it.tag.set(extension.startingTag)
      it.outputFile.set(outputFile)
    }

    val abacusTask = project.tasks.create("abacus", AbacusTask::class.java)
    abacusTask.dependsOn(countClassFile, countExtensionFile)
  }
}

abstract class AbacusTask: DefaultTask() {
  @TaskAction
  fun starts() {
    project.logger.log(LogLevel.LIFECYCLE, "Abacus starts")
  }
}

/*
abstract class GitTask: DefaultTask() {
    @TaskAction
    fun switch() {
        val gitStatusTask: Task? = project.tasks.findByPath(":demo:gitStatus")
        println("gitStatusTask $gitStatusTask")
        this.dependsOn(gitStatusTask)
    }
}*/

/*

    project.tasks.create("countFile100", CountFileTask::class.java)
    val countFile100: TaskProvider<CountFileTask> = project.tasks.named("countFile100", CountFileTask::class.java)
    countFile100.configure {
      it.greeting.set(extension.message)
      it.outputFile.set(extension.outputFile)
    }
    project.tasks.create("countFile110", CountFileTask::class.java)
    val countFile110: TaskProvider<CountFileTask> = project.tasks.named("countFile110", CountFileTask::class.java)
    countFile110.configure {
      it.greeting.set(extension.message)
      it.outputFile.set(extension.outputFile)
    }

    project.tasks.create("gitTag100", GitCheckout::class.java)
    val gitTag100: TaskProvider<GitCheckout> = project.tasks.named("gitTag100", GitCheckout::class.java)
    gitTag100.configure {
      it.doFirst {
        extension.outputFile.get().delete()
        extension.outputFile.get().appendText(
          buildString {
            append("1.0.0")
            appendLine()
          }
        )
      }
      it.branch.set("tags/1.0.0")
    }
    project.tasks.create("gitTag110", GitCheckout::class.java)
    val gitTag110: TaskProvider<GitCheckout> = project.tasks.named("gitTag110", GitCheckout::class.java)
    gitTag110.configure {
      it.branch.set("tags/1.1.0")
    }
    gitTag110.get().doFirst {
      extension.outputFile.get().appendText(
        buildString {
          append("1.1.0")
          appendLine()
        }
      )
    }
    project.tasks.create("gitRestoreMaster", GitCheckout::class.java)
    val gitRestoreMaster: TaskProvider<GitCheckout> = project.tasks.named("gitRestoreMaster", GitCheckout::class.java)

    gitRestoreMaster.get().mustRunAfter(countFile110)
    countFile110.get().mustRunAfter(gitTag110)
    gitTag110.get().mustRunAfter(countFile100)
    countFile100.get().mustRunAfter(gitTag100)
    gitRestoreMaster.get().dependsOn(gitTag110, gitTag100, countFile110, countFile100)
 */
