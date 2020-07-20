package org.stormrealms.stormstats.model;

import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.outfacing.RPGStat;

import lombok.Data;

@Entity
@Data
@Table(name = "rpg_character")
@Component
@Scope("prototype")
public class RPGCharacter {

	@OneToOne(mappedBy = "character", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private ClassData data;
	@Id
	@Column(name = "character_id")
	@Type(type = "uuid-char")
	private UUID id;
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
	private double health;
	@Column
	private double maxHealth;
	@Column
	private double experience;
	@Column
	private int level;
	@Column
	@ElementCollection(fetch = FetchType.EAGER, targetClass = Integer.class)
	private Map<RPGStat, Integer> stats;

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

	@PostConstruct
	public void assignID() {
		this.id = UUID.randomUUID();
	}

	public void setDefaults() {
		this.level = 1;
		this.experience = 0;
		this.health = 0;
	}
}
