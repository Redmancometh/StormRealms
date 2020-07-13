package org.stormrealms.stormcore.magic;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeginEvent {
	public Class<? extends Event> eventClass() default PlayerDropItemEvent.class;
}