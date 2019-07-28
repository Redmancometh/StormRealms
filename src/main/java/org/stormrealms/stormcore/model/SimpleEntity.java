package org.stormrealms.stormcore.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class SimpleEntity {
	@Id
	private UUID id;
	@Column
	private String name;
}
