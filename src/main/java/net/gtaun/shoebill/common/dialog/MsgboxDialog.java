/**
 * Copyright (C) 2012 MK124
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.gtaun.shoebill.common.dialog;

import net.gtaun.shoebill.constant.DialogStyle;
import net.gtaun.shoebill.event.dialog.DialogResponseEvent;
import net.gtaun.shoebill.object.Player;
import net.gtaun.util.event.EventManager;

/**
 * 
 * @author MK124
 */
public abstract class MsgboxDialog extends AbstractDialog
{
	@SuppressWarnings("unchecked")
	public static abstract class AbstractMsgboxDialogBuilder
	<DialogType extends MsgboxDialog, DialogBuilderType extends AbstractMsgboxDialogBuilder<DialogType, DialogBuilderType>>
	extends AbstractDialogBuilder<DialogType, DialogBuilderType>
	{
		protected AbstractMsgboxDialogBuilder(DialogType dialog)
		{
			super(dialog);
		}
		
		public DialogBuilderType message(DialogTextSupplier messageSupplier)
		{
			dialog.setMessage(messageSupplier);
			return (DialogBuilderType) this;
		}
		
		public DialogBuilderType message(String message)
		{
			dialog.setMessage(message);
			return (DialogBuilderType) this;
		}
		
		public DialogBuilderType onClickOk(ClickOkHandler handler)
		{
			dialog.setOnClickOkHandler(handler);
			return (DialogBuilderType) this;
		}
	}
	
	public static class MsgboxDialogBuilder extends AbstractMsgboxDialogBuilder<MsgboxDialog, MsgboxDialogBuilder>
	{
		protected MsgboxDialogBuilder(Player player, EventManager rootEventManager)
		{
			super(new MsgboxDialog(player, rootEventManager)
			{
			});
		}
	}
	
	public static MsgboxDialogBuilder create(Player player, EventManager rootEventManager)
	{
		return new MsgboxDialogBuilder(player, rootEventManager);
	}
	
	@FunctionalInterface
	public interface ClickOkHandler
	{
		void onClickOk(MsgboxDialog dialog);
	}
	
	
	private DialogTextSupplier messageSupplier = DialogTextSupplier.EMPTY_MESSAGE_SUPPLIER;
	private ClickOkHandler clickOkHandler = null;
	
	
	protected MsgboxDialog(Player player, EventManager rootEventManager)
	{
		super(DialogStyle.MSGBOX, player, rootEventManager);
	}
	
	public void setMessage(DialogTextSupplier messageSupplier)
	{
		this.messageSupplier = messageSupplier;
	}
	
	public void setMessage(String message)
	{
		this.messageSupplier = (d) -> message;
	}
	
	public void setOnClickOkHandler(ClickOkHandler onClickOkHandler)
	{
		this.clickOkHandler = onClickOkHandler;
	}
	
	@Override
	public void show()
	{
		show(messageSupplier.get(this));
	}
	
	@Override
	final void onClickOk(DialogResponseEvent event)
	{
		onClickOk();
	}
	
	protected void onClickOk()
	{
		if (clickOkHandler != null) clickOkHandler.onClickOk(this);
	}
}