package com.alecarnevale.abacus.model

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property


interface AbacusPluginExtension {
  /**
   * Names referring interface or class for which count how many implementations are present in codebase.
   */
  val supertypes: ListProperty<String>

  /**
   * File extensions to count in codebase.
   */
  val fileExtensions: ListProperty<String>

  /**
   * For which folder count in codebase.
   */
  val fileFolders: ListProperty<String>

  /**
   * Path pointing the file containing the list of tags.
   */
  val tagsFilePath: Property<String>
}