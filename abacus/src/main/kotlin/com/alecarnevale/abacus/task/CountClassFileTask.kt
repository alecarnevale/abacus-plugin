package com.alecarnevale.abacus.task

import kotlinx.ast.common.AstResult
import kotlinx.ast.common.AstSource
import kotlinx.ast.common.ast.Ast
import kotlinx.ast.common.klass.KlassDeclaration
import kotlinx.ast.grammar.kotlin.common.summary
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Count how many class inheritance from at least one of the class provided in [supertypes].
 */
abstract class CountClassFileTask : CountFileTask() {
  @get:Input
  abstract val supertypes: ListProperty<String>

  override val taskType: TaskType
    get() = TaskType.CLASS

  @TaskAction
  fun countClassFile() {
    val supertypesValues = supertypes.orNull ?: emptyList()
    project.logger.log(LogLevel.LIFECYCLE, "Start counting files with supertypes: $supertypesValues")
    var cnt = 0
    project.projectDir.walk()
      // TODO find a better way to use only source directory and no build folders
      .filter { it.isFile && it.extension == "kt" && !it.path.contains("build/") }
      .forEach { file ->
        val source: AstSource.File = AstSource.File(file.path)
        val kotlinFile: Ast = KotlinGrammarAntlrKotlinParser.parseKotlinFile(source)
        val summary: AstResult<Unit, List<Ast>> = kotlinFile.summary(false)
        summary.onSuccess {
          val klass = it.filterIsInstance<KlassDeclaration>().first()
          if (klass.inheritance.firstOrNull()?.type?.identifier in supertypesValues) {
            cnt += 1
          }
        }
      }

    printOutput(cnt)
  }
}