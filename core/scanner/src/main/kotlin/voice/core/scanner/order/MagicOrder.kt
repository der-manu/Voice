package voice.core.scanner.order

/**
 * "Magic" ordering: finds out from the file names of a folder whether several CDs are mixed into one folder and puts
 * CD 1 completely before CD 2.
 *
 * Example: "01 - Title (CD1).mp3", "01 - Title (CD2).mp3", "02 - Title (CD1).mp3" ...
 * A plain name comparison gives 01/CD1, 01/CD2, 02/CD1 ..., because the track number comes first.
 *
 * The guiding rule: reordering only pays off when the leading number is NOT unique, that is the same track number
 * appears on several CDs. If it is unique (a continuous count), a plain name comparison already solves it and this
 * returns no key.
 */
public object MagicOrder {

  private val NUMBER = Regex("""\d+""")
  private val LEADING = Regex("""^[^\p{L}\d]*(\d+)""")
  private val DISC = Regex(
    """(?i)(?<!\p{L})(?:cd|disc|disk|dvd|teil|part|band|vol|volume|side|seite|kassette|mc)[\s._\-]*\(?(\d{1,3})""",
  )

  private const val MIN_SHARE = 0.6
  private const val MIN_COMPLETENESS = 0.5
  private const val MAX_DISCS = 12
  private const val MAX_DIGITS = 15
  private const val MISSING = -1L

  private data class Fields(
    val disc: Long?,
    val leading: Long?,
    val trailing: Long?,
  )

  public fun keys(fileNames: List<String>): List<List<Long>?> {
    val none = List<List<Long>?>(fileNames.size) { null }
    if (fileNames.size < 2) return none
    val fields = fileNames.map(::parse)
    val coarse = listOf<(Fields) -> Long?>({ it.disc }, { it.trailing })
      .firstOrNull { selector -> formsGrid(fields, selector) }
      ?: return none
    return fields.map { listOf(coarse(it) ?: MISSING, it.leading ?: MISSING) }
  }

  private fun formsGrid(
    fields: List<Fields>,
    coarse: (Fields) -> Long?,
  ): Boolean {
    val pairs = fields.mapNotNull { f ->
      val c = coarse(f)
      val t = f.leading
      if (c != null && t != null) c to t else null
    }
    if (pairs.size < MIN_SHARE * fields.size) return false
    val discs = pairs.map { it.first }.toSet()
    val tracks = pairs.map { it.second }.toSet()
    if (discs.size !in 2..MAX_DISCS) return false
    if (discs.size >= tracks.size) return false
    if (tracks.size == pairs.size) return false
    val filled = pairs.toSet().size.toDouble() / (discs.size * tracks.size)
    return filled >= MIN_COMPLETENESS
  }

  private fun parse(fileName: String): Fields {
    val base = fileName.substringBeforeLast('.', fileName)
    val leadingMatch = LEADING.find(base)?.groups?.get(1)
    val leading = leadingMatch?.value?.toNumber()
    val last = NUMBER.findAll(base).lastOrNull()
    val trailing = last?.takeIf { leadingMatch == null || it.range.first != leadingMatch.range.first }
      ?.value?.toNumber()
    val disc = DISC.find(base)?.groupValues?.get(1)?.toNumber()
    return Fields(disc = disc, leading = leading, trailing = trailing)
  }

  private fun String.toNumber(): Long = take(MAX_DIGITS).toLong()
}
