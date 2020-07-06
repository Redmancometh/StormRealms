package org.stormrealms.stormstats.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "rpg_character")
public class RPGCharacter {

	@PrePersist
	public void prePersist() {
		this.level = 10;
	}

	@OneToOne(mappedBy = "character", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private ClassData data;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "character_id")
	private long id;
	@Column
	private String characterName;
	@Column
	private double x;
	@Column
	private double y;
	@Column
	private double z;
	@Column
	private String world;
	@Column
	private int health;
	@Column
	private double experience;
	@Column
	private int level;
	@Column
	private int str;
	@Column
	private int intel;
	@Column
	private int spi;
	@Column
	private int agi;

	public void setDefaults() {
		this.level = 1;
		this.str = 1;
		this.intel = 1;
		this.spi = 1;
		this.agi = 1;
		this.experience = 0;
		this.health = 0;
	}
}
