package org.stormrealms.stormnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.server.Packet;
import net.minecraft.server.PacketListenerPlayIn;
import net.minecraft.server.PlayerConnection;

import org.springframework.stereotype.Component;
import org.stormrealms.stormspigot.PacketDispatcher;

@Component
public class PacketSubscriptionManager implements PacketDispatcher {
    //private static Mutex mapMutex = new Mutex();

    private Map<
        Class<? extends Packet<PacketListenerPlayIn>>,
        List<PacketSubscription<PacketHook<? extends Packet<PacketListenerPlayIn>>>>> subscriptions = new HashMap<>();

    /**
     * Creates a subscription to a specific packet inheriting {@link Packet}{@literal <PacketListenerPlayIn>}.
     * @param <T>           The type of packet to subscribe to.
     * @param packetClass   The {@link Class} that represents the type of packet to subscribe to.
     * @return              A subscription to receive packets of the requested type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Packet<PacketListenerPlayIn>> PacketSubscription<PacketHook<T>> subscribe(Class<T> packetClass) {
        PacketSubscription<PacketHook<T>> subscription = new PacketSubscription<>();
        List<PacketSubscription<PacketHook<? extends Packet<PacketListenerPlayIn>>>> packetSubList = subscriptions.get(packetClass);

        if(packetSubList == null) {
            packetSubList = new ArrayList<>();
            subscriptions.put(packetClass, packetSubList);
        }

        packetSubList.add((PacketSubscription<PacketHook<? extends Packet<PacketListenerPlayIn>>>) (Object) subscription);
        return subscription;
    }

    /**
     * Fulfills subscriptions to this packet's type.
     * @param <T>               The type of packet to dispatch.
     * @param packet            The packet to dispatch.
     * @param dispatchInternal  A function that calls to the internal packet listener.
     * @param player            The player that sent the packet.
     */
    @Override
    public <T extends Packet<PacketListenerPlayIn>> void dispatchPacket(T packet, PlayerConnection playerConnection, Consumer<T> dispatchInternal) {
        @SuppressWarnings("unchecked")
        Class<T> packetClass = (Class<T>) packet.getClass();

        List<PacketSubscription<PacketHook<?>>> packetSubList = subscriptions.get(packetClass);

        if(packetSubList == null) {
            dispatchInternal.accept(packet);
            return;
        }

        Ref<T> packetRef = Ref.to(packet);
        packetSubList.removeIf(PacketSubscription::isCancelled);

        for(PacketSubscription<PacketHook<?>> sub : packetSubList) {
            sub.fulfill(new PacketHook<>(packetRef.deref(), playerConnection, packetRef::assign));
            if(packetRef.deref() == null) return;
        }

        dispatchInternal.accept(packetRef.deref());
    }
}
