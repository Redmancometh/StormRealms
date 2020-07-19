package org.stormrealms.stormcombat.combatsystem;

import org.stormrealms.stormcombat.events.WeaponAttackEvent;

public interface CombatProcessor {

	public void dodged(WeaponAttackEvent e);

	public void parried(WeaponAttackEvent e);

	public void missed(WeaponAttackEvent e);

	public void hit(WeaponAttackEvent e);

	public void giveLoot(WeaponAttackEvent e);

}
