package voice.core.scanner.order

import kotlin.test.Test
import kotlin.test.assertEquals

class ChapterOrderingTest {

  private fun disc(vararg tracksPerDisc: Int): List<String> =
    tracksPerDisc.flatMapIndexed { disc, tracks ->
      (1..tracks).map { "%02d - Rom (CD%d).mp3".format(it, disc + 1) }
    }

  @Test
  fun naturalKeepsTheInputOrder() {
    val files = listOf("01.mp3", "02.mp3", "03.mp3")
    assertEquals(files, applyChapterOrder(files, magic = false, reversed = false) { it })
  }

  @Test
  fun reversedReversesTheInput() {
    val files = listOf("01.mp3", "02.mp3", "03.mp3")
    assertEquals(files.reversed(), applyChapterOrder(files, magic = false, reversed = true) { it })
  }

  @Test
  fun magicGroupsCdsWhenTheyAreMixed() {
    val mixed = disc(5, 5, 5).sorted()
    val out = applyChapterOrder(mixed, magic = true, reversed = false) { it }
    assertEquals(
      listOf("01 - Rom (CD1).mp3", "02 - Rom (CD1).mp3", "03 - Rom (CD1).mp3"),
      out.take(3),
    )
    assertEquals(out.reversed(), applyChapterOrder(mixed, magic = true, reversed = true) { it })
  }

  @Test
  fun magicKeepsTheInputWhenNoPatternIsFound() {
    val files = listOf("Anfang.mp3", "Mitte.mp3", "Ende.mp3")
    assertEquals(files, applyChapterOrder(files, magic = true, reversed = false) { it })
  }

  @Test
  fun handlesEmptyInput() {
    assertEquals(emptyList(), applyChapterOrder(emptyList<String>(), magic = true, reversed = false) { it })
  }
}
