package org.stormrealms.nms;

import java.util.function.Consumer;

import net.minecraft.server.Packet;
import net.minecraft.server.PacketListenerPlayIn;

public class PacketHook<T extends Packet<PacketListenerPlayIn>> {
    private Packet<PacketListenerPlayIn> packet;
    private Consumer<? extends T> internalDispatchFunction;

    public PacketHook(Packet<PacketListenerPlayIn> packet, Consumer<? extends T> dispatchInternal) {
        this.packet = packet;
        this.internalDispatchFunction = dispatchInternal;
    }

    public Packet<PacketListenerPlayIn> getPacket() {
        return packet;
    }

    public Consumer<? extends T> getInternalDispatchFunction() {
        return internalDispatchFunction;
    }
}
