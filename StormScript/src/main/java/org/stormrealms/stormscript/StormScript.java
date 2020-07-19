package org.stormrealms.stormscript;

import javax.annotation.PostConstruct;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormSpringPlugin;

@Component
public class StormScript extends StormSpringPlugin {
    @PostConstruct
    public void enable() {
        // TODO(Yevano)
    }

    @Override
    public Class<?> getConfigurationClass() {
        return null;
    }

    @Override
    public String[] getPackages() {
        return new String[] { "org.stormrealms.stormscript", "org.stormrealms.stormscript.configuration" };
    }

    @Override
    public void setContext(AnnotationConfigApplicationContext context) {
        super.context = context;
    }

    @Override
    public ConfigurableApplicationContext getContext() {
        return super.context;
    }
    
}