package org.stormrealms.stormstats.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import org.hibernate.annotations.Type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ClassData {
	@Id
	@Column(name = "id")
	@Type(type = "uuid-char")
	private UUID playerId;
	@OneToOne(targetEntity = RPGPlayer.class)
	@MapsId("playerId")
	@JoinColumn(name = "playerId", referencedColumnName = "playerId")
	private RPGPlayer player;
	@Column
	@Setter
	@Getter
	private String chosenClass;

	@PrePersist
	public void print() {

	}

	public UUID getPlayerId() {
		return playerId;
	}

	public void setPlayerId(UUID playerId) {
		this.playerId = playerId;
	}

}
