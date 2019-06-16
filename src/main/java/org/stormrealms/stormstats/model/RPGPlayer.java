package org.stormrealms.stormstats.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class RPGPlayer {
	@Column
	private double experience;
	@Column
	private int level;
	
}
