package org.stormrealms.stormspigot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.server.Packet;
import net.minecraft.server.PacketListenerPlayIn;

public class PacketSubscriptionManager {
    //private static Mutex mapMutex = new Mutex();

    private static final PacketSubscriptionManager instance = new PacketSubscriptionManager();

    public static PacketSubscriptionManager getInstance() {
        return instance;
    }

    private static Map<
        Class<? extends Packet<PacketListenerPlayIn>>,
        List<PacketSubscription<PacketHook<?>>>> subscriptions = new HashMap<>();

    /**
     * Creates a subscription to a specific packet inheriting {@link Packet}{@literal <PacketListenerPlayIn>}.
     * @param <T>           The type of packet to subscribe to.
     * @param packetClass   The {@link Class} that represents the type of packet to subscribe to.
     * @return              A subscription to receive packets of the requested type.
     */
    public <T extends Packet<PacketListenerPlayIn>> PacketSubscription<PacketHook<?>> subscribe(Class<? extends T> packetClass) {
        PacketSubscription<PacketHook<?>> subscription = new PacketSubscription<>();
        List<PacketSubscription<PacketHook<?>>> packetSubList = subscriptions.get(packetClass);

        if(packetSubList == null) {
            packetSubList = new ArrayList<>();
            subscriptions.put(packetClass, packetSubList);
        }

        packetSubList.add(subscription);
        return subscription;
    }

    /**
     * Fulfills subscriptions to this packet's type.
     * @param <T>               The type of packet to dispatch.
     * @param packet            The packet to dispatch.
     * @param dispatchInternal  A function that calls to the internal packet listener.
     */
    public <T extends Packet<PacketListenerPlayIn>> void dispatchPacket(T packet, Consumer<T> dispatchInternal) {
        @SuppressWarnings("unchecked")
        Class<T> packetClass = (Class<T>) packet.getClass();

        List<PacketSubscription<PacketHook<?>>> packetSubList = subscriptions.get(packetClass);

        if(packetSubList == null) dispatchInternal.accept(packet);

        Ref<T> packetRef = Ref.to(packet);
        packetSubList.removeIf(PacketSubscription::isCancelled);

        for(PacketSubscription<PacketHook<?>> sub : packetSubList) {
            sub.fulfill(new PacketHook<>(packetRef.deref(), packetRef::assign));
        }

        dispatchInternal.accept(packetRef.deref());
    }
}
