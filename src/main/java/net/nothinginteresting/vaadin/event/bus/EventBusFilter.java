package net.nothinginteresting.vaadin.event.bus;

import java.lang.reflect.Method;

import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.listener.MessageHandlerMetadata;

import com.vaadin.event.ConnectorEvent;
import com.vaadin.ui.Component;

public class EventBusFilter implements IMessageFilter
{
	@Override
	public boolean accepts(Object message, MessageHandlerMetadata metadata)
	{
		//		System.out.println("EventBusFilter.accepts: check "
		//		        + metadata.getHandler().getDeclaringClass().getName() + ". "
		//		        + metadata.getHandler().getName());

		ConnectorEvent event = (ConnectorEvent) message;
		Method handler = metadata.getHandler();
		String targetWidgetId = handler.getAnnotation(ViewHandler.class).value();
		if (targetWidgetId.isEmpty())
		{
			return isHandlerFromComponent(event, handler);
		}
		else
		{
			return targetWidgetId.equals(getComponentId(event));
		}
	}

	private Object getComponentId(ConnectorEvent event)
	{
		if (event.getSource() instanceof Component)
		{
			return ((Component) event.getSource()).getId();
		}
		return null;
	}

	private boolean isHandlerFromComponent(ConnectorEvent event, Method handler)
	{
		//		boolean result =
		//		    handler.getDeclaringClass().isAssignableFrom(event.getComponent().getClass());
		//		System.out.println("isHandlerFromComponent for " + handler.getName() + ": " + result);
		//		return result;
		return true;
	}
}
