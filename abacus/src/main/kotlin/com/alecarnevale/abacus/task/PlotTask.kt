package com.alecarnevale.abacus.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import space.kscience.plotly.*
import space.kscience.plotly.Plotly.plot
import space.kscience.plotly.StaticPlotlyRenderer.renderPlot
import space.kscience.plotly.models.BarMode
import space.kscience.plotly.models.TraceType
import kotlin.io.path.Path


private const val DEFAULT_PLOT_FILE_PATH = "build/abacus/plot.html"

@JvmInline
value class Tag(val value: String)

data class Counters(val byClass: Int, val byFile: Int)

abstract class PlotTask : DefaultTask() {
  /*TODO
  @get:InputFile
  abstract val inputFile: Property<File>
*/

  @TaskAction
  fun plot() {
    // TODO a fake map only for testing
    val map = mapOf<Tag, Counters>(
      Tag("1.0") to Counters(3, 4),
      Tag("1.1") to Counters(5, 2),
      Tag("1.2") to Counters(7, 9)
    )

    val barChart = plot {
      trace {
        name = "CntByClass"
        x.set(map.keys.map { it.value })
        y.set(map.values.map { it.byClass })
        type = TraceType.bar
      }
      trace {
        name = "CntByFile"
        x.set(map.keys.map { it.value })
        y.set(map.values.map { it.byFile })
        type = TraceType.bar
      }

      layout {
        title = "Number of file"
        xaxis {
          title = "tags"
        }
        yaxis {
          title = "count"
        }
        barmode = BarMode.relative
      }
    }

    val table = plot {
      table {
        header {
          values("Tag", "CntByClass", "CntByFile")
        }
        cells {
          values(map.toTableValues())
        }
      }
    }

    val fragment = Plotly.fragment {
      renderPlot(barChart)
      renderPlot(table)
    }
    //fragment.makeFile(path = Path(DEFAULT_PLOT_FILE_PATH), show = false)

    val page = fragment.toPage()
    page.makeFile(path = Path(DEFAULT_PLOT_FILE_PATH), show = false)

    // currently Page is useless, it's the same output of:
    // fragment.makeFile(path = Path(DEFAULT_PLOT_FILE_PATH), show = false, resourceLocation = ResourceLocation.REMOTE)
    // that loads resource from CDN

    // I'd like to use ResourceLocation.LOCAL for the fragment, but I wasn't able to make it work when integration in a gradle plugin
    // alternative could be EMBED, but it'll produce a very large file.
  }

  private fun Map<Tag, Counters>.toTableValues(): Iterable<Iterable<String>> {
    val tagColumn = keys.map { it.value }.toList()
    val byClassColumn = values.map { it.byClass.toString() }.toList()
    val byFileColumn = values.map { it.byFile.toString() }.toList()
    return listOf(tagColumn, byClassColumn, byFileColumn)
  }
}