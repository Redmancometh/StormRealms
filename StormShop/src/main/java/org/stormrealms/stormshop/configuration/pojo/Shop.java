package org.stormrealms.stormshop.configuration.pojo;

import java.util.List;

import org.stormrealms.stormmenus.Icon;
import org.stormrealms.stormmenus.MenuTemplate;

import lombok.Data;

@Data
public class Shop {
	private List<ShopItem> items;
	private MenuTemplate template;
	private String name;
	private Icon shopIcon;
}
