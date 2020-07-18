package org.stormrealms.stormshop.menus;

import javax.annotation.PostConstruct;

import org.bukkit.entity.Player;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.ClickType;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormmenus.util.TriConsumer;
import org.stormrealms.stormshop.configuration.pojo.Shop;
import org.stormrealms.stormshop.configuration.pojo.ShopItem;

public class IndividualShopMenu extends TypedMenu<Shop> {

	public IndividualShopMenu(Shop shop) {
		super(shop.getName(), 54);
		this.setSelected(shop);
	}

	@PostConstruct
	public void constructMenu() {
		this.getSelected().getItems().forEach((shopItem) -> {
			TriConsumer<ClickType, Shop, Player> click = (type, shop, p) -> {
				switch (type) {
				case LEFT:
					sellOne(p, shopItem);
					break;
				case RIGHT:
					break;
				case SHIFT_LEFT:
					sellStack(p, shopItem);
					break;
				case SHIFT_RIGHT:
					break;
				default:
					break;
				}
			};
			TypedMenuButton<Shop> itemButton = new TypedMenuButton((p, shop) -> shopItem.getItem().build(), click);
			setButton(shopItem.getItem().getIndex(), itemButton);
		});
	}

	private void sellStack(Player p, ShopItem shopItem) {
		
	}

	private void sellOne(Player p, ShopItem shopItem) {

	}

	@Override
	public boolean shouldReopen() {
		return false;
	}
}
