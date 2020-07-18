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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.Data;

@Entity
@Data
@Table(name = "rpg_character")
@Component
@Scope("prototype")
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
	private String race;
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
	private int sta;
	@Column
	private int intel;
	@Column
	private int spi;
	@Column
	private int agi;

	public boolean isCharacterComplete() {
		// System.out.println("RACE IS NULL: " + (this.getRace() == null));
		// System.out.println("NAME IS NULL: " + (this.getCharacterName() == null));
		// System.out.println("CLASSDATA IS NULL: " + (this.getData() == null));
		// System.out.println("IS CHAR COMPLETE?");
		if (this.getRace() != null && this.getCharacterName() != null && this.getData() != null
				&& this.getData().getClassName() != null) {
			System.out.println("CHARACTER COMPLETE");
			return true;
		}
		System.out.println("CHARACTER IS NOT COMPLETE");
		return false;
	}

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
