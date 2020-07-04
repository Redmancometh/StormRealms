package org.stormrealms.stormstats.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ClassData {
	@Id
	@Column(name = "character_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long characterId;

	@OneToOne
	@MapsId
	private RPGCharacter character;

	@Column
	private String className;

}
