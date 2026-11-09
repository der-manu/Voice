package voice.core.data

/**
 * The order the chapters of a book are played in. Stored per book in [CustomMetadata], so it survives a rescan.
 *
 * [Natural] is Voice's normal order (folders first, then natural by name). [Magic] additionally recognises CD numbers
 * in file names and puts CD 1 completely before CD 2. [Reversed] reverses the chosen order.
 */
public enum class ChapterOrder {
  Natural,
  Magic,
  NaturalReversed,
  MagicReversed,
  ;

  public val magic: Boolean get() = this == Magic || this == MagicReversed
  public val reversed: Boolean get() = this == NaturalReversed || this == MagicReversed
}
