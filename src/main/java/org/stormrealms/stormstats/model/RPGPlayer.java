package org.stormrealms.stormstats.model;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.hibernate.annotations.Type;
import com.redmancometh.redcore.Defaultable;

import lombok.Data;

@Data
@Entity
@Table
public class RPGPlayer implements Defaultable<UUID> {
	@Id
	@Type(type = "uuid-char")
	@Column(nullable = true)
	private UUID playerId;
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
	@Column
	private String chosenClass;
	@ElementCollection(fetch = FetchType.EAGER, targetClass = java.lang.Integer.class)
	private Map<UUID, Integer> questMap;
	
	@Transient
	private WeakReference<Player> playerRef;

	public Player getPlayer() {
		if (playerRef != null && playerRef.get() != null)
			return playerRef.get();
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
