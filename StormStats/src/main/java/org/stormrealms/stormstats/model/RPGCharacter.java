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
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Table(name = "rpg_character")
@Component
@Scope("prototype")
@ToString(exclude = { "rpgPlayer" })
public class RPGCharacter {

	@PrePersist
	public void prePersist() {
		this.level = 10;
	}

	@OneToOne(mappedBy = "character", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private ClassData data;
	@ManyToOne
	@JoinColumn(name = "player_id")
	private RPGPlayer rpgPlayer;
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

	public boolean isCharacterComplete() {
		if (this.getRace() != null && this.getCharacterName() != null && this.getData() != null
				&& this.getData().getClassName() != null) {
			System.out.println("CHARACTER COMPLETE");
			return true;
		}
		System.out.println("CHARACTER IS NOT COMPLETE");
		return false;
	}

	public void setDefaults() {

	}
}
