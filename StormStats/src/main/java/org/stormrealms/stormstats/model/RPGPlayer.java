package org.stormrealms.stormstats.model;

import java.lang.ref.WeakReference;
import java.util.HashSet;
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
import javax.persistence.Transient;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.hibernate.annotations.Type;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.Defaultable;

import lombok.Data;

@Data
@Entity
@Table(name = "rpg_player")
@Component
@Scope("prototype")
public class RPGPlayer implements Defaultable<UUID> {
	@Type(type = "uuid-char")
	@Column(name = "player_id")
	@Id
	private UUID playerId;
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "player_id")
	private Set<RPGCharacter> characters = new HashSet();
	@Transient
	private RPGCharacter constructingChar;
	@Transient
	private RPGCharacter chosenCharacter;
	@Transient
	private WeakReference<Player> playerRef;

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
		this.playerId = playerId;
		this.characters = new HashSet();
	}

}
