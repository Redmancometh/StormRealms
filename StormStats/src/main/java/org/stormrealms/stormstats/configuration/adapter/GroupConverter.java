package org.stormrealms.stormstats.configuration.adapter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormstats.configuration.pojo.Group;
import org.stormrealms.stormstats.configuration.pojo.GroupsConfiguration;

@Converter
public class GroupConverter implements AttributeConverter<Group, String> {

	@Override
	public String convertToDatabaseColumn(Group group) {
		return group.getKey();
	}

	@Override
	public Group convertToEntityAttribute(String groupKey) {
		GroupsConfiguration groups = StormCore.getInstance().getContext().getBean(GroupsConfiguration.class);
		return groups.getGroups().get(groupKey);
	}

}
