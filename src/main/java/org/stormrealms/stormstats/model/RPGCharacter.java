package org.stormrealms.stormstats.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "rpg_character")
public class RPGCharacter {

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

}
