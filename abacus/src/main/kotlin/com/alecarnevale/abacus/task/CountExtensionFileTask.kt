package com.alecarnevale.abacus.task

import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Count how many file with extensions in [fileExtensions] and contained in one of [fileFolders].
 *
 * If [fileFolders] is empty, a file with extension in [fileExtensions] is counted wherever it is.
 */
abstract class CountExtensionFileTask : CountFileTask() {
  @get:Input
  abstract val fileExtensions: ListProperty<String>

  @get:Input
  abstract val fileFolders: ListProperty<String>

  override val taskType: TaskType
    get() = TaskType.EXTENSION

  @TaskAction
  fun countXmlFile() {
    fileExtensions.orNull ?: emptyList()
    project.logger.log(
      LogLevel.LIFECYCLE,
      "Start counting files with extensions=$fileExtensions contained in folders=$fileFolders "
    )
    val cnt = project.projectDir.walk()
      .count { file ->
        val parent = file.parentFile
        // TODO find a better way to use only source directory and no build folders
        if (file.isFile && parent.isDirectory && !file.path.contains("build/")) {
          file.extension in fileExtensions.get() && (fileFolders.get().isEmpty() || parent.name in fileFolders.get())
        } else false
      }
    printOutput(cnt)
  }
}