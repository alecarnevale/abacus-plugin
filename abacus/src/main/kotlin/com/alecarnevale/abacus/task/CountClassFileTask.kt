package com.alecarnevale.abacus.task

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
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

  private lateinit var supertypesValues: List<String>

  private var cntError = 0

  @TaskAction
  fun countClassFile() {
    supertypesValues = supertypes.orNull ?: emptyList()
    if (supertypesValues.isEmpty()) {
      return
    }

    project.logger.log(LogLevel.LIFECYCLE, "Start counting files with supertypes: $supertypesValues")
    val cnt = countKotlinFiles() + countJavaFiles()
    project.logger.log(LogLevel.WARN, "Error parsing $cntError files")
    printOutput(cnt)
  }

  private fun countKotlinFiles(): Int {
    var cnt = 0
    project.projectDir.walk()
      // TODO find a better way to use only source directory and no build folders
      .filter { it.isFile && it.extension == "kt" && !it.path.contains("build/") }
      .forEach { file ->
        try {
          val source: AstSource.File = AstSource.File(file.path)
          val kotlinFile: Ast = KotlinGrammarAntlrKotlinParser.parseKotlinFile(source)
          val summary: AstResult<Unit, List<Ast>> = kotlinFile.summary(false)
          summary.onSuccess {
            val klass = it.filterIsInstance<KlassDeclaration>().first()
            if (klass.inheritance.firstOrNull()?.type?.identifier in supertypesValues) {
              cnt += 1
            }
          }
        } catch (ex: Exception) {
          cntError += 1
        }
      }
    return cnt
  }

  private fun countJavaFiles(): Int {
    var cnt = 0
    project.projectDir.walk()
      // TODO find a better way to use only source directory and no build folders
      .filter { it.isFile && it.extension == "java" && !it.path.contains("build/") }
      .forEach { file ->
        try {
          val compilationUnit: CompilationUnit = StaticJavaParser.parse(file)
          val declarations: MutableList<ClassOrInterfaceDeclaration> =
            compilationUnit.findAll(ClassOrInterfaceDeclaration::class.java)
          val implementsAny = declarations.any {
            it.implementedTypes.any {
              it.nameAsString in supertypesValues
            }
          }
          val extendsAny = declarations.any {
            it.extendedTypes.any {
              it.nameAsString in supertypesValues
            }
          }
          if (implementsAny || extendsAny) {
            cnt++
          }
        } catch (ex: Exception) {
          cntError += 1
        }
      }
    return cnt
  }
}