package org.stormrealms.stormscript.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({ "org.stormrealms.stormnet", "org.stormrealms.stormnet.configuration" })
public class StormScriptConfiguration { }