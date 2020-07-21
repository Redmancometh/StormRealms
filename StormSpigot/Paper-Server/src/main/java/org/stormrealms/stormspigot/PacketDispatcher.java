package org.stormrealms.stormspigot;

import java.util.function.Consumer;

import net.minecraft.server.Packet;
import net.minecraft.server.PacketListenerPlayIn;
import net.minecraft.server.PlayerConnection;

public interface PacketDispatcher {
	public <T extends Packet<PacketListenerPlayIn>> void dispatchPacket(T packet, PlayerConnection playerConnection,
			Consumer<T> dispatchInternal);
}