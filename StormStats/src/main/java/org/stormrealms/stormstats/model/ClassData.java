package org.stormrealms.stormstats.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Component
@Scope("prototype")
public class ClassData {
	@Id
	@Column(name = "character_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long characterId;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@MapsId
	private RPGCharacter character;

	@Column
	private String className;

}
