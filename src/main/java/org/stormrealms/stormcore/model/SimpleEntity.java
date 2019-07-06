package org.stormrealms.stormcore.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SimpleEntity {
	@Id
	private UUID id;
	@Column
	private String name;
}
