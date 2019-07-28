package org.stormrealms.stormstats.model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;

import lombok.Data;

@Entity
@Data
public class ClassData {
	@Id
	private UUID playerId;
	@JoinColumn(name = "playerId")
	@MapsId
	@OneToOne
	private RPGPlayer player;
}
