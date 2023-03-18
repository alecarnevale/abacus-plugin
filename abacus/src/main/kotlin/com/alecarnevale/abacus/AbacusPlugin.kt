package com.alecarnevale.abacus

import kotlinx.ast.common.AstResult
import kotlinx.ast.common.AstSource
import kotlinx.ast.common.ast.Ast
import kotlinx.ast.common.klass.KlassDeclaration
import kotlinx.ast.grammar.kotlin.common.summary
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction


interface GreetingPluginExtension {
  val message: Property<String>
}

class AbacusPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.pluginManager.apply("xyz.ronella.simple-git")

    val extension = project.extensions.create("greeting", GreetingPluginExtension::class.java)

    val countFileTask: CountFileTask = project.tasks.create("countFile", CountFileTask::class.java)
    countFileTask.greeting.set(extension.message)

    val gitStatusTask: Task? = project.tasks.findByPath("gitStatus")
    println("gitStatusTask $gitStatusTask")
    countFileTask.dependsOn(gitStatusTask)
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

abstract class CountFileTask : DefaultTask() {
  @get:Input
  abstract val greeting: Property<String>

  @TaskAction
  fun countXmlFile() {
    println("Start counting xml file...")
    val projectRootDir = project.projectDir
    var cntXml = 0
    projectRootDir.walk()
      .filter { it.isFile && it.extension == "xml" }
      .forEach {
        val parent = it.parentFile
        val parentParent = parent.parentFile
        if (parent.isDirectory && parent.name == "layout" && parentParent.isDirectory && parentParent.name == "resources") {
          cntXml += 1
        }
      }
    println("CntXML = $cntXml")
  }

  @TaskAction
  fun countClassFile() {
    println("Start counting class file...")
    val projectRootDir = project.projectDir
    var cntClass = 0
    projectRootDir.walk()
      .filter { it.isFile && it.extension == "kt" }
      .forEach {
        val source: AstSource.File = AstSource.File(it.path)
        val kotlinFile: Ast = KotlinGrammarAntlrKotlinParser.parseKotlinFile(source)
        val summary: AstResult<Unit, List<Ast>> = kotlinFile.summary(false)
        summary.onSuccess {
          val klass = it.filter { it is KlassDeclaration }.first() as KlassDeclaration

          if (klass.inheritance.firstOrNull()?.type?.identifier == "Foo") {
            cntClass += 1
          }
        }
      }
    println("CntClass = $cntClass")
  }
}

