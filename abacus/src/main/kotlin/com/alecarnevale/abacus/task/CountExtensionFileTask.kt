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

  private lateinit var fileExtensionsValues: List<String>
  private lateinit var fileFoldersValues: List<String>

  override val taskType: TaskType
    get() = TaskType.EXTENSION

  @TaskAction
  fun countXmlFile() {
    fileExtensionsValues = fileExtensions.orNull ?: emptyList()
    fileFoldersValues = fileFolders.orNull ?: emptyList()
    if (fileExtensionsValues.isEmpty()) {
      return
    }

    project.logger.log(
      LogLevel.LIFECYCLE,
      "Start counting files with extensions=$fileExtensionsValues contained in folders=$fileFoldersValues "
    )
    val cnt = project.projectDir.walk()
      .count { file ->
        val parent = file.parentFile
        // TODO find a better way to use only source directory and no build folders
        if (file.isFile && parent.isDirectory && !file.path.contains("build/")) {
          file.extension in fileExtensionsValues && (fileFoldersValues.isEmpty() || parent.name in fileFoldersValues)
        } else false
      }
    printOutput(cnt)
  }
}