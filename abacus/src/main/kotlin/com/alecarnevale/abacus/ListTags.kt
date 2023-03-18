package com.alecarnevale.abacus

import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import java.io.File

private const val TASK_NAME = "abacusTags"
private const val GIT_EXEC_NAME = "git"
private const val GIT_TAG_NAME = "tag"
private const val GIT_TAG_ORDERING_ARGS = "--sort=v:refname"

/**
 * Register and configure a task that lists all tags found in [tagsFile].
 *
 * This is intended as a helper task to meet abacus requirements.
 */
internal fun Project.listTags(tagsFile: File) {
  // TODO this is under analysis
  // The best solution, from user prospective, would be to run just a single task.
  // The problem is that I can not start configuring counting task without the tag file.
  // TODO: mixing Exec task and simple plugin is weird
  // Simple git plugin was introduced to simplify the usage of git through a plugin,
  // unfortunately it didn't find a solution for redirecting the output of gitTags into a file
  // but I start write raw Exec command like this, adding a dependency like simple git become pointless
  val abacusTagsTask: TaskProvider<Exec> = project.tasks.register(TASK_NAME, Exec::class.java)
  abacusTagsTask.configure {
    it.executable = GIT_EXEC_NAME
    it.args = listOf(GIT_TAG_NAME, GIT_TAG_ORDERING_ARGS)
    it.standardOutput = tagsFile.outputStream()
  }
}