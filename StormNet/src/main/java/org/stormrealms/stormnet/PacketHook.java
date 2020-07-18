package org.stormrealms.stormnet;

import java.util.function.Consumer;

import net.minecraft.server.Packet;
import net.minecraft.server.PacketListenerPlayIn;
import net.minecraft.server.PlayerConnection;

public class PacketHook<T extends Packet<PacketListenerPlayIn>> {
    private Packet<PacketListenerPlayIn> packet;
    private Consumer<T> internalDispatchFunction;
    private PlayerConnection playerConnection;

    public PacketHook(Packet<PacketListenerPlayIn> packet, PlayerConnection playerConnection, Consumer<T> dispatchInternal) {
        this.packet = packet;
        this.internalDispatchFunction = dispatchInternal;
        this.playerConnection = playerConnection;
    }

    public Packet<PacketListenerPlayIn> getPacket() {
        return packet;
    }

    public PlayerConnection getPlayerConnection() {
        return playerConnection;
    }

    public Consumer<? extends T> getInternalDispatchFunction() {
        return internalDispatchFunction;
    }
}
