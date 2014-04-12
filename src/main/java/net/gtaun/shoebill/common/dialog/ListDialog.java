/**
 * Copyright (C) 2012-2014 MK124
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import net.gtaun.shoebill.common.dialog.ListDialogItem.ItemBooleanSupplier;
import net.gtaun.shoebill.common.dialog.ListDialogItem.ItemSelectHandler;
import net.gtaun.shoebill.common.dialog.ListDialogItem.ItemSelectSimpleHandler;
import net.gtaun.shoebill.common.dialog.ListDialogItem.ItemTextSupplier;
import net.gtaun.shoebill.constant.DialogStyle;
import net.gtaun.shoebill.event.dialog.DialogResponseEvent;
import net.gtaun.shoebill.object.Player;
import net.gtaun.util.event.EventManager;

/**
 * 
 * @author MK124
 */
public class ListDialog extends AbstractDialog
{
	@SuppressWarnings("unchecked")
	public static abstract class AbstractListDialogBuilder
	<DialogType extends ListDialog, DialogBuilderType extends AbstractListDialogBuilder<DialogType, DialogBuilderType>>
	extends AbstractDialogBuilder<DialogType, DialogBuilderType>
	{
		protected AbstractListDialogBuilder(DialogType dialog)
		{
			super(dialog);
		}
		
		public DialogBuilderType item(ListDialogItem item)
		{
			dialog.items.add(item);
			return (DialogBuilderType) this;
		}

		public DialogBuilderType item(String itemText, ItemSelectSimpleHandler handler)
		{
			dialog.items.add(new ListDialogItem(itemText, handler));
			return (DialogBuilderType) this;
		}
		
		public DialogBuilderType item(Supplier<String> textSupplier, ItemSelectSimpleHandler handler)
		{
			dialog.items.add(new ListDialogItem(textSupplier, handler));
			return (DialogBuilderType) this;
		}
		
		public <DataType> DialogBuilderType item(DataType data, String itemText, ItemSelectHandler<DataType> handler)
		{
			dialog.items.add(new ListDialogItem(data, itemText, handler));
			return (DialogBuilderType) this;
		}
		
		public <DataType> DialogBuilderType item(DataType data, ItemTextSupplier<DataType> textSupplier, ItemSelectHandler<DataType> handler)
		{
			dialog.items.add(new ListDialogItem(data, textSupplier, handler));
			return (DialogBuilderType) this;
		}
		
		public <DataType> DialogBuilderType item(DataType data, String itemText, ItemBooleanSupplier<DataType> enabledSupplier, ItemSelectHandler<DataType> handler)
		{
			dialog.items.add(new ListDialogItem(data, itemText, enabledSupplier, handler));
			return (DialogBuilderType) this;
		}
		
		public <DataType> DialogBuilderType item(DataType data, ItemTextSupplier<DataType> textSupplier, ItemBooleanSupplier<DataType> enabledSupplier, ItemSelectHandler<DataType> handler)
		{
			dialog.items.add(new ListDialogItem(data, textSupplier, enabledSupplier, handler));
			return (DialogBuilderType) this;
		}
		
		public DialogBuilderType onClickOk(ClickOkHandler handler)
		{
			dialog.setClickOkHandler(handler);
			return (DialogBuilderType) this;
		}
	}
	
	public static class ListDialogBuilder extends AbstractListDialogBuilder<ListDialog, ListDialogBuilder>
	{
		protected ListDialogBuilder(Player player, EventManager rootEventManager)
		{
			super(new ListDialog(player, rootEventManager));
		}
	}
	
	public static AbstractListDialogBuilder<?, ?> create(Player player, EventManager rootEventManager)
	{
		return new ListDialogBuilder(player, rootEventManager);
	}
	
	@FunctionalInterface
	public interface ClickOkHandler
	{
		void onClickOk(ListDialog dialog, ListDialogItem item);
	}
	
	
	protected final List<ListDialogItem> items;
	protected final List<ListDialogItem> displayedItems;
	
	private ClickOkHandler clickOkHandler = null;
	

	protected ListDialog(Player player, EventManager eventManager)
	{
		super(DialogStyle.LIST, player, eventManager);
		items = new ArrayList<ListDialogItem>()
		{
			private static final long serialVersionUID = 2260194681967627384L;

			@Override
			public void add(int index, ListDialogItem e)
			{
				e.currentDialog = ListDialog.this;
				super.add(index, e);
			}
			
			@Override
			public boolean add(ListDialogItem e)
			{
				e.currentDialog = ListDialog.this;
				return super.add(e);
			}
			
			@Override
			public ListDialogItem set(int index, ListDialogItem e)
			{
				e.currentDialog = ListDialog.this;
				return super.set(index, e);
			}
			
			@Override
			public boolean addAll(Collection<? extends ListDialogItem> c)
			{
				c.forEach((e) -> e.currentDialog = ListDialog.this);
				return super.addAll(c);
			}
			
			@Override
			public boolean addAll(int index, Collection<? extends ListDialogItem> c)
			{
				c.forEach((e) -> e.currentDialog = ListDialog.this);
				return super.addAll(index, c);
			}
		};
		displayedItems = new ArrayList<>();
	}
	
	public void setClickOkHandler(ClickOkHandler handler)
	{
		clickOkHandler = handler;
	}
	
	public List<ListDialogItem> getItems()
	{
		return items;
	}
	
	public List<ListDialogItem> getDisplayedItems()
	{
		return displayedItems;
	}
	
	@Override
	public void show()
	{
		String listStr = "";
		displayedItems.clear();
		
		for (ListDialogItem item : items)
		{
			if (item.isEnabled() == false) continue;
			
			listStr += item.getItemText() + "\n";
			displayedItems.add(item);
		}
		
		show(listStr);
	}
	
	@Override
	final void onClickOk(DialogResponseEvent event)
	{
		int itemId = event.getListitem();
		ListDialogItem item = displayedItems.get(itemId);

		onClickOk(item);
		item.onItemSelect();
	}
	
	protected void onClickOk(ListDialogItem item)
	{
		if (clickOkHandler != null) clickOkHandler.onClickOk(this, item);
	}
}
