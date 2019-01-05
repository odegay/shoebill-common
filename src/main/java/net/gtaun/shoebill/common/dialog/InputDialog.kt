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
import net.gtaun.shoebill.constant.DialogStyle
import net.gtaun.shoebill.entities.Player
import net.gtaun.shoebill.event.dialog.DialogResponseEvent
import net.gtaun.util.event.EventManager

/**
 * @author MK124
 * @author Marvin Haschker
 */
@Suppress("CanBeParameter")
@AllOpen
class InputDialog
@JvmOverloads constructor(parentEventManager: EventManager, val passwordMode: Boolean = false) :
        AbstractDialog(if (passwordMode) DialogStyle.PASSWORD else DialogStyle.INPUT, parentEventManager) {

    @Suppress("unused")
    @AllOpen
    abstract class AbstractInputDialogBuilder<T : InputDialog, B : InputDialog.AbstractInputDialogBuilder<T, B>> :
            Builder<T, B>() {

        fun message(message: String) = message { message }
        fun messageSupplier(supplier: DialogTextSupplier): B {
            dialog.messageSupplier = supplier
            return this as B
        }

        fun onClickOk(handler: ClickOkHandler) = onClickOk { handler }

        fun message(init: B.() -> String): B {
            dialog.message = init(this as B)
            return this
        }

        fun messageSupplier(init: B.(T) -> String) =
                messageSupplier(DialogTextSupplier { init(this as B, dialog) })

        fun onClickOk(init: B.() -> ClickOkHandler): B {
            dialog.clickOkHandler = init(this as B)
            return this
        }
    }

    @AllOpen
    class InputDialogBuilder(parentEventManager: EventManager, passwordMode: Boolean = false) :
            AbstractInputDialogBuilder<InputDialog, InputDialogBuilder>() {
        init {
            dialog = InputDialog(parentEventManager, passwordMode)
        }
    }

    var messageSupplier = DialogTextSupplier { "None" }
    var clickOkHandler: ClickOkHandler? = null

    var message: String
        get() = messageSupplier[this]
        set(message) {
            this.messageSupplier = DialogTextSupplier { message }
        }

    fun setMessage(messageSupplier: DialogTextSupplier) {
        this.messageSupplier = messageSupplier
    }

    override fun show(player: Player) {
        //super.show(player)
        show(player, messageSupplier[this])
    }

    override fun onClickOk(event: DialogResponseEvent) = onClickOk(event.player, event.inputText)

    fun onClickOk(player: Player, inputText: String) {
        clickOkHandler?.onClickOk(this, player, inputText) ?: return Unit
    }

    @FunctionalInterface
    interface ClickOkHandler {
        fun onClickOk(dialog: InputDialog, player: Player, text: String)
    }

    companion object {

        @JvmStatic
        @JvmOverloads
        fun create(eventManager: EventManager = Shoebill.get().eventManager, passwordMode: Boolean = false) =
                InputDialogBuilder(eventManager, passwordMode)

        fun ClickOkHandler(handler: (InputDialog, Player, String) -> Unit) = object : ClickOkHandler {
            override fun onClickOk(dialog: InputDialog, player: Player, text: String) {
                handler(dialog, player, text)
            }
        }
    }
}
