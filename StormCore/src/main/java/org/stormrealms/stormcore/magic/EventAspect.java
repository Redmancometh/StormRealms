package org.stormrealms.stormcore.magic;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class EventAspect {
	/*
	 * @Autowired private List<EventListener> listeners;
	 * 
	 * @Pointcut(value = "execution(public * *(..))*") public void anyPublicMethod()
	 * { System.out.println("PUBLIC"); }
	 * 
	 * @After("anyPublicMethod() && @annotation(eventStart)") public void
	 * openDialog(JoinPoint pjp, BeginEvent eventStart) throws Throwable { Event e =
	 * (Event) pjp.getArgs()[0]; listeners.forEach((listener) -> { if
	 * (listener.getType() == e.getClass()) {
	 * 
	 * } }); }
	 */
}
