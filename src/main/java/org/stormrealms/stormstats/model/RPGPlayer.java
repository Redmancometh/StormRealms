package org.stormrealms.stormstats.model;

import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.hibernate.annotations.Type;
import org.stormrealms.stormcore.Defaultable;

import lombok.Data;

@Data
@Entity
@Table(name = "rpg_player")
public class RPGPlayer implements Defaultable<UUID> {
	@Type(type = "uuid-char")
	@Column(name = "player_id")
	@Id
	private UUID playerId;
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "player_id")
	private Set<RPGCharacter> characters;
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

	// @Transient
	// private WeakReference<Player> playerRef;

	public Player getPlayer() {
		// if (playerRef != null && playerRef.get() != null)
		// return playerRef.get();
		return Bukkit.getPlayer(playerId);
	}

	@Override
	public UUID getKey() {
		return playerId;
	}

	@Override
	public void setDefaults(UUID playerId) {
		this.level = 1;
		this.str = 1;
		this.intel = 1;
		this.spi = 1;
		this.agi = 1;
		this.playerId = playerId;
		this.experience = 0;
		this.health = 0;
	}

}
