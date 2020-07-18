package org.stormrealms.stormcore.util;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.stormrealms.stormcore.SpringPlugin;
import org.stormrealms.stormcore.StormCore;

public class SpringUtil {
	public static <T> T getPlugin(Class<T> clazz) {
		return StormCore.getInstance().getContext().getAutowireCapableBeanFactory().getBean(clazz);
	}

	public static <T extends SpringPlugin> ConfigurableApplicationContext getPluginContext(Class<T> clazz) {
		return StormCore.getInstance().getContext().getAutowireCapableBeanFactory().getBean(clazz).getContext();
	}

	/**
	 * Don't use this if you don't know what you're doing.
	 * 
	 * @param beanClass
	 * @param scope
	 */
	public static void addSpringBean(Class beanClass, String name, String scope) {
		GenericBeanDefinition myBeanDefinition = new GenericBeanDefinition();
		AutowireCapableBeanFactory factory = StormCore.getInstance().getContext().getAutowireCapableBeanFactory();
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;
		myBeanDefinition.setBeanClass(beanClass);
		myBeanDefinition.setScope(scope);
		registry.registerBeanDefinition(name, myBeanDefinition);
	}
}
