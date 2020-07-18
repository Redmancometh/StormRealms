package org.stormrealms.stormshop.configuration.pojo;

import java.util.List;

import org.stormrealms.stormmenus.MenuTemplate;

import lombok.Data;

@Data
public class ShopConfig {
	private List<Shop> shops;
	private String menuName;
	private MenuTemplate template;
}
