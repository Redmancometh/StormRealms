package org.stormrealms.stormstats.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.hibernate.annotations.Type;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormstats.configuration.adapter.GroupConverter;
import org.stormrealms.stormstats.configuration.adapter.RaceConverter;
import org.stormrealms.stormstats.configuration.pojo.Group;
import org.stormrealms.stormstats.configuration.pojo.Race;

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
	@Convert(converter = RaceConverter.class)
	private Race race;
	@Column
	@Convert(converter = GroupConverter.class)
	private List<Group> groups;
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
	@Column
	@ElementCollection(fetch = FetchType.EAGER, targetClass = String.class)
	private List<String> additionalPermissions;
	@Transient
	private Location location;
	@Transient
	private List<String> finalPerms;

	public boolean hasPermission(String permission) {
		return finalPerms.contains(permission);
	}

	public boolean isCharacterComplete() {
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
		this.location = new Location(Bukkit.getWorld(world), x, y, z);
	}

	public void setDefaults() {
		this.level = 1;
		this.experience = 0;
		this.health = 0;
	}
}
