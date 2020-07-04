package org.stormrealms.stormstats.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "rpg_character")
public class RPGCharacter {
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "player_id")
	private RPGPlayer rpgPlayer;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "character_id")
	private long id;

	@OneToOne
	@MapsId
	private RPGCharacter character;

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
