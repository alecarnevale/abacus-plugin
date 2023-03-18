package com.alecarnevale.abacus.task

import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import java.io.File

enum class TaskType(val typeDescriptor: String) {
  CLASS("cls"),
  EXTENSION("ext")
}

/**
 * If [tag] is provided, it will be used in the output info.
 * If [outputFile] is provided, output is printed into it, otherwise it will be printed in console.
 */
abstract class CountFileTask: DefaultTask() {
  @get:Input
  @get:Optional
  abstract val tag: Property<String?>

  @get:OutputFile
  @get:Optional
  abstract val outputFile: Property<File?>

  @get:Internal
  protected abstract val taskType: TaskType

  protected fun printOutput(cnt: Int) {
    outputFile.orNull?.printOutputInFile(cnt = cnt, tag = tag.orNull)
      ?: printOutputInConsole(cnt = cnt, tag = tag.orNull)
  }

  private fun printOutputInConsole(cnt: Int, tag: String?) {
    val msgBody = tag?.let { "Tag:$tag - Cnt:$cnt" } ?: cnt
    logger.log(LogLevel.QUIET, "$taskType task --> $msgBody")
  }

  private fun File.printOutputInFile(cnt: Int, tag: String?) {
    val msg = tag?.let { "$tag, ${taskType.typeDescriptor} $cnt" } ?: "${taskType.typeDescriptor}, $cnt"
    appendText(
      buildString {
        append(msg)
        appendLine()
      }
    )
  }
}