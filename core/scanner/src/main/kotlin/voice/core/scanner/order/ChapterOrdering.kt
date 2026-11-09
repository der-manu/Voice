package voice.core.scanner.order

/**
 * Reorders a flat list of audio files of one book according to the chosen [magic] and [reversed] settings.
 *
 * The input is expected to already be in Voice's natural order (folders first, then natural by name). This keeps that
 * order for [magic] = false. For [magic] = true, [MagicOrder] looks at the file names and, only when several CDs are
 * mixed into one folder, puts CD 1 completely before CD 2. When no such pattern is found, the natural order is kept.
 * [reversed] reverses the result.
 *
 * @param T the caller's file type.
 * @param fileName how to read the plain file name (with extension) of an item.
 */
public fun <T> applyChapterOrder(
  files: List<T>,
  magic: Boolean,
  reversed: Boolean,
  fileName: (T) -> String,
): List<T> {
  val ordered =
    if (magic) {
      val keys = MagicOrder.keys(files.map(fileName))
      if (keys.all { it == null }) {
        files
      } else {
        // Keep the natural order as the tie-breaker, so files without a key stay where they were.
        files.indices
          .sortedWith(
            Comparator { a, b ->
              val keyA = keys[a]
              val keyB = keys[b]
              when {
                keyA != null && keyB != null -> {
                  for (i in 0 until minOf(keyA.size, keyB.size)) {
                    val result = keyA[i].compareTo(keyB[i])
                    if (result != 0) return@Comparator result
                  }
                  a.compareTo(b)
                }
                keyA != null -> -1
                keyB != null -> 1
                else -> a.compareTo(b)
              }
            },
          )
          .map { files[it] }
      }
    } else {
      files
    }
  return if (reversed) ordered.reversed() else ordered
}
