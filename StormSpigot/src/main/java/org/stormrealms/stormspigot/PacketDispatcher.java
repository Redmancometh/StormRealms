package org.stormrealms.stormspigot;

import java.util.function.Consumer;

import net.minecraft.server.Packet;
import net.minecraft.server.PacketListenerPlayIn;
import net.minecraft.server.PlayerConnection;

public interface PacketDispatcher {
    public void dispatchPacket(
        Packet<PacketListenerPlayIn> packet,
        PlayerConnection playerConnection,
        Consumer<? extends Packet<PacketListenerPlayIn>> internalDispatchFunction);
}
