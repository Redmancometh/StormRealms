package org.stormrealms.stormloot.configuration.pojo;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This just looks a lot nicer than a ConfigManager<List<WeaponRoot>>
 * 
 * @author Redmancometh
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class WeaponRoots {
	private List<ItemRoot> roots;
}
