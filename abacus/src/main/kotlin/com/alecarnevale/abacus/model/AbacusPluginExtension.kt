package com.alecarnevale.abacus.model

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property


interface AbacusPluginExtension {
  /**
   * Names referring interface or class for which count how many implementations are present in codebase.
   */
  val supertypes: ListProperty<String>

  /**
   * File to count in codebase.
   */
  val fileDescriptors: ListProperty<Pair<String, String>>
  // TODO I'd like to change Pair for a custom Serializable data class, but it doesn't work

  /**
   * Tag for which start counting.
   */
  val startingTag: Property<String>
}