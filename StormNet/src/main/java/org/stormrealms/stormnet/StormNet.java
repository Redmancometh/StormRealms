package org.stormrealms.stormnet;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormSpringPlugin;
import org.stormrealms.stormnet.configuration.StormNetConfiguration;
import org.stormrealms.stormspigot.PlayerConnectionOverride;

@Component
public class StormNet extends StormSpringPlugin {
    @Autowired private PacketSubscriptionManager packetSubscriptionManager;

    @PostConstruct
    public void enable() {
        System.out.println("Enabling StormNet");
        super.enable();
        PlayerConnectionOverride.setPacketDispatcher(packetSubscriptionManager);
    }

    @Override
    public Class<?> getConfigurationClass() {
        return StormNetConfiguration.class;
    }

    @Override
    public ConfigurableApplicationContext getContext() {
        return super.context;
    }

    @Override
    public void setContext(AnnotationConfigApplicationContext context) {
        this.context = context;
    }

    @Override
    public String[] getPackages() {
        return new String[] { "org.stormrealms.stormnet", "org.stormrealms.stormnet.configuration" };
    }
}