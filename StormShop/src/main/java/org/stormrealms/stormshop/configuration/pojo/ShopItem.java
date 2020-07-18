package org.stormrealms.stormshop.configuration.pojo;

import org.stormrealms.stormmenus.Icon;

import lombok.Data;

@Data
public class ShopItem {
	private int cost;
	private int sellFor;
	private Icon item;
}
