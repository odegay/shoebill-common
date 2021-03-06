/**
 * Copyright (C) 2012-2014 MK124

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.gtaun.shoebill.common.dialog

import net.gtaun.shoebill.Shoebill
import net.gtaun.shoebill.common.AllOpen
import net.gtaun.shoebill.data.Color
import net.gtaun.shoebill.entities.Player
import net.gtaun.util.event.EventManager

/**
 * @author MK124
 * @author Marvin Haschker
 */
@AllOpen
class PageListDialog protected constructor(eventManager: EventManager) : ListDialog(eventManager) {

    @AllOpen
    class PageListDialogBuilder(parentEventManager: EventManager) :
            AbstractListDialogBuilder<PageListDialog, PageListDialogBuilder>() {

        fun itemsPerPage(count: Int) = itemsPerPage { count }
        fun currentPage(page: Int) = currentPage { page }
        fun previousPage(text: String) = previousPage { text }
        fun nextPage(text: String) = nextPage { text }
        fun onPageTurn(handler: PageTurnHandler) = onPageTurn { handler }

        fun itemsPerPage(init: PageListDialogBuilder.() -> Int): PageListDialogBuilder {
            dialog.itemsPerPage = init(this)
            return this
        }

        fun currentPage(init: PageListDialogBuilder.() -> Int): PageListDialogBuilder {
            dialog.currentPage = init(this)
            return this
        }

        fun previousPage(init: PageListDialogBuilder.() -> String): PageListDialogBuilder {
            dialog.prevPageItemText = init(this)
            return this
        }

        fun nextPage(init: PageListDialogBuilder.() -> String): PageListDialogBuilder {
            dialog.nextPageItemText = init(this)
            return this
        }

        fun onPageTurn(init: PageListDialogBuilder.() -> PageTurnHandler): PageListDialogBuilder {
            dialog.pageTurnHandler = init(this)
            return this
        }

        init {
            dialog = PageListDialog(parentEventManager)
        }

    }

    var itemsPerPage = 10
    var currentPage: Int = 0

    var prevPageItemText = "<< Prev Page <<"
    var nextPageItemText = ">> Next Page >>"

    var pageTurnHandler: PageTurnHandler? = null
    val enabledItems: List<ListDialogItem>
        get() = items.filter { it.isEnabled }

    val maxPage: Int
        get() = (items.size - 1) / itemsPerPage

    override fun show(player: Player) = show(player, listString)

    val listString: String
        get() {
            var listStr = ""
            displayedItems.clear()

            if (enabledItems.size >= itemsPerPage + 1) {
                displayedItems.add(object : ListDialogItem(Color.GRAY.embeddingString + prevPageItemText) {
                    override fun onItemSelect(player: Player) {
                        var page = currentPage - 1
                        if (page < 0) page = maxPage
                        show(player, page)
                    }
                })
            }

            val offset = itemsPerPage * currentPage
            for (i in 0 until itemsPerPage) {
                val index = offset + i
                if (enabledItems.size <= index) break

                val item = enabledItems[offset + i]
                displayedItems.add(item)
            }

            if (displayedItems.size >= itemsPerPage + 1)
                displayedItems.add(object : ListDialogItem(Color.GRAY.embeddingString + nextPageItemText) {
                    override fun onItemSelect(player: Player) {
                        var page = currentPage + 1
                        if (page > maxPage) page = 0
                        show(player, page)
                    }
                })

            for (item in displayedItems) {
                listStr += item.itemText + "\n"
            }
            return listStr
        }

    fun show(player: Player, page: Int) {
        if (currentPage != page) {
            currentPage = page
            if (currentPage > maxPage)
                currentPage = maxPage
            else if (currentPage < 0) currentPage = 0

            onPageTurn(player)
        }

        show(player)
    }

    fun onPageTurn(player: Player) = pageTurnHandler?.onPageTurn(this, player)

    @FunctionalInterface
    interface PageTurnHandler {
        fun onPageTurn(dialog: PageListDialog, player: Player)
    }

    companion object {

        @JvmStatic
        @JvmOverloads
        fun create(eventManager: EventManager = Shoebill.get().eventManager) = PageListDialogBuilder(eventManager)

        fun PageTurnHandler(handler: (PageListDialog, Player) -> Unit) = object : PageTurnHandler {
            override fun onPageTurn(dialog: PageListDialog, player: Player) {
                handler(dialog, player)
            }
        }
    }
}
