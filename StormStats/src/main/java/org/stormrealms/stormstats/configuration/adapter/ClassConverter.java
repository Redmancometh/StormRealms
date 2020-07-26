package org.stormrealms.stormstats.configuration.adapter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormstats.configuration.pojo.ClassConfiguration;
import org.stormrealms.stormstats.configuration.pojo.ClassInformation;

@Converter
public class ClassConverter implements AttributeConverter<ClassInformation, String> {

	@Override
	public String convertToDatabaseColumn(ClassInformation info) {
		return info.getKey();
	}

	@Override
	public ClassInformation convertToEntityAttribute(String classKey) {
		ClassConfiguration classes = StormCore.getInstance().getContext().getBean(ClassConfiguration.class);
		return classes.getClassMap().get(classKey);
	}

}
