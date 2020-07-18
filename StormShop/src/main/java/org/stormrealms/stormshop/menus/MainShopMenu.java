package org.stormrealms.stormshop.menus;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormmenus.absraction.Menu;
import org.stormrealms.stormmenus.menus.MenuButton;
import org.stormrealms.stormshop.configuration.pojo.ShopConfig;

public class MainShopMenu extends Menu {
	private ConfigManager<ShopConfig> shopConfig;

	public MainShopMenu(@Autowired ConfigManager<ShopConfig> shopConfig) {
		super(shopConfig.getConfig().getMenuName(), shopConfig.getConfig().getTemplate(), 54);
		this.shopConfig = shopConfig;
	}

	@PostConstruct
	public void initMenu() {
		this.shopConfig.getConfig().getShops().forEach((shop) -> {
			MenuButton button = new MenuButton((p) -> shop.getShopIcon().build(), (type, player) -> {
				IndividualShopMenu shopMenu = new IndividualShopMenu(shop);
				shopMenu.open(player, shop);
			});
			setButton(shop.getShopIcon().getIndex(), button);
		});
	}
}
