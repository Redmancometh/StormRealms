package org.stormrealms.stormloot.configuration.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WeaponRoot extends ItemRoot {
	private int lowDmg;
	private int highDmg;
}
