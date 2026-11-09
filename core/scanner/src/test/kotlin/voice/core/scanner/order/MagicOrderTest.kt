package voice.core.scanner.order

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MagicOrderTest {

  private fun discBook(vararg tracksPerDisc: Int): List<String> =
    tracksPerDisc.flatMapIndexed { disc, tracks ->
      (1..tracks).map { "%02d - Der Brief aus Rom (CD%d).mp3".format(it, disc + 1) }
    }

  private fun sortBy(names: List<String>): List<String> {
    val keys = MagicOrder.keys(names)
    if (keys.all { it == null }) return names.sorted()
    return names.indices.sortedWith(
      Comparator { a, b ->
        val ka = keys[a]
        val kb = keys[b]
        when {
          ka != null && kb != null -> {
            for (i in 0 until minOf(ka.size, kb.size)) {
              val c = ka[i].compareTo(kb[i])
              if (c != 0) return@Comparator c
            }
            0
          }
          ka != null -> -1
          kb != null -> 1
          else -> 0
        }
      },
    ).map { names[it] }
  }

  @Test
  fun putsEachCdCompletelyBeforeTheNextWhenTrackNumbersRepeat() {
    assertEquals(discBook(20, 20, 16), sortBy(discBook(20, 20, 16).shuffled(java.util.Random(5))))
  }

  @Test
  fun aPlainNameSortGetsThisWrongWhichIsTheReasonForMagic() {
    assertEquals(
      listOf(
        "01 - Der Brief aus Rom (CD1).mp3",
        "01 - Der Brief aus Rom (CD2).mp3",
        "01 - Der Brief aus Rom (CD3).mp3",
      ),
      discBook(5, 5, 5).sorted().take(3),
    )
    assertEquals(
      listOf(
        "01 - Der Brief aus Rom (CD1).mp3",
        "02 - Der Brief aus Rom (CD1).mp3",
        "03 - Der Brief aus Rom (CD1).mp3",
      ),
      sortBy(discBook(5, 5, 5)).take(3),
    )
  }

  @Test
  fun worksWithoutACdWordWhenASmallTrailingNumberIsTheCd() {
    val names = listOf(1, 2).flatMap { disc -> (1..8).map { "%02d - Titel (%d).mp3".format(it, disc) } }
    assertEquals(names, sortBy(names.shuffled(java.util.Random(1))))
  }

  @Test
  fun leavesAContinuousCountAlone() {
    val names = (1..40).map { "%02d - Titel (CD%d).mp3".format(it, if (it <= 20) 1 else 2) }
    assertTrue(MagicOrder.keys(names).all { it == null })
  }

  @Test
  fun leavesDiscPrefixedNamesAlone() {
    val names = (1..3).flatMap { disc -> (1..9).map { "$disc-%03d.mp3".format(it) } }
    assertTrue(MagicOrder.keys(names).all { it == null })
  }

  @Test
  fun refusesAmbiguousSquareGrids() {
    val names = (1..3).flatMap { disc -> (1..3).map { "%02d - Titel (CD%d).mp3".format(it, disc) } }
    assertTrue(MagicOrder.keys(names).all { it == null })
  }

  @Test
  fun refusesNamesWithoutNumbersAndTooFewFiles() {
    assertTrue(MagicOrder.keys(listOf("Anfang.mp3", "Mitte.mp3", "Ende.mp3")).all { it == null })
    assertTrue(MagicOrder.keys(listOf("01 - A (CD1).mp3")).all { it == null })
    assertEquals(emptyList<List<Long>?>(), MagicOrder.keys(emptyList()))
  }
}
