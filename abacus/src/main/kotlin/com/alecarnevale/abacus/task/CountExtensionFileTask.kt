package com.alecarnevale.abacus.task

import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Count how many file with description provided in at least one of [fileDescriptors].
 */
abstract class CountExtensionFileTask : CountFileTask() {
  @get:Input
  abstract val fileDescriptors: ListProperty<Pair<String, String>>

  override val taskType: TaskType
    get() = TaskType.EXTENSION

  @TaskAction
  fun countXmlFile() {
    val fileDescriptorsValues = fileDescriptors.orNull ?: emptyList()
    project.logger.log(LogLevel.LIFECYCLE, "Start counting files with descriptors: $fileDescriptorsValues")
    val cnt = project.projectDir.walk()
      .count { file ->
        val parent = file.parentFile
        if (file.isFile && parent.isDirectory) {
          fileDescriptorsValues.firstOrNull {
            file.extension == it.first && parent.name == it.second
          } != null
        } else false
      }
    printOutput(cnt)
  }
}