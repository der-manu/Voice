package voice.features.bookOverview.chapterOrder

import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn
import voice.core.data.BookId
import voice.core.data.isRemote
import voice.features.bookOverview.bottomSheet.BottomSheetItem
import voice.features.bookOverview.bottomSheet.BottomSheetItemViewModel
import voice.features.bookOverview.di.BookActionsScope
import voice.navigation.Destination
import voice.navigation.Navigator

@SingleIn(BookActionsScope::class)
@ContributesIntoSet(BookActionsScope::class)
class ChapterOrderActionViewModel(private val navigator: Navigator) : BottomSheetItemViewModel {

  override suspend fun items(bookId: BookId): List<BottomSheetItem> {
    // the order of a server book is decided by the server
    if (bookId.isRemote) return emptyList()
    return listOf(BottomSheetItem.ChapterOrder)
  }

  override suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  ) {
    if (item == BottomSheetItem.ChapterOrder) {
      navigator.goTo(Destination.ChapterOrder(bookId))
    }
  }
}
