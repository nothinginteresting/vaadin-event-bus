package net.nothinginteresting.vaadin.event.bus;

import java.lang.reflect.Field;
import java.util.Iterator;

import com.vaadin.event.ConnectorEvent;
import com.vaadin.server.ClientConnector.AttachEvent;
import com.vaadin.server.ClientConnector.AttachListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.Component.Listener;
import com.vaadin.ui.HasComponents;

public class EventBusUtils
{
	public static void setChildrenIds(HasComponents container)
	{
		Class<?> cur = container.getClass();
		while (!(cur.getName().equals(Object.class.getName())))
		{
			for (Field field : cur.getDeclaredFields())
			{
				try
				{
					field.setAccessible(true);
					if (field.get(container) != null)
					{
						if (!Component.class.isAssignableFrom(field.get(container).getClass()))
						{
							continue;
						}
						Component c = (Component) field.get(container);
						if (c.getId() == null)
						{
							c.setId(field.getName());
						}
					}
				}
				catch (Exception e)
				{
					throw new RuntimeException("Can not set id for " + field, e);
				}
			}
			cur = cur.getSuperclass();
		}
	}

	public static Component findWidget(String widgetName, HasComponents listener)
	{
		Component result = findWidgetImpl(widgetName, listener);
		if (result != null)
		{
			return result;
		}
		else
		{
			throw new RuntimeException("Can not find " + widgetName + " in " + listener);
		}
	}

	private static Component findWidgetImpl(String widgetName, HasComponents listener)
	{
		HasComponents container = listener;
		Iterator<Component> iterator = container.iterator();
		while (iterator.hasNext())
		{
			Component c = iterator.next();
			if (widgetName.equals(c.getId()))
			{
				return c;
			}
			if (c instanceof HasComponents)
			{
				Component result = findWidgetImpl(widgetName, (HasComponents) c);
				if (result != null)
				{
					return result;
				}
			}
		}
		return null;
	}

	public static void send(Object event, Component from)
	{
		EventBus bus = findEventBus(from);
		if (bus == null)
		{
			throw new RuntimeException("Can not find EventBus for " + from);
		}
		bus.send(event);
	}

	public static EventBus findEventBus(Component from)
	{
		//System.out.println("Find EventBus in " + from);
		if (from == null)
		{
			return null;
		}

		if (from instanceof EventBusOwner)
		{
			EventBusOwner owner = (EventBusOwner) from;
			return owner.getEventBus();
		}
		else
		{
			return findEventBus(from.getParent());
		}

		//		Class<?> curClass = from.getClass();
		//		while (!curClass.equals(Object.class))
		//		{
		//			for (Field field : curClass.getDeclaredFields())
		//			{
		//				try
		//				{
		//					field.setAccessible(true);
		//					if (field.get(from) != null)
		//					{
		//						Object value = field.get(from);
		//						if (value instanceof EventBus)
		//						{
		//							return (EventBus) value;
		//						}
		//					}
		//				}
		//				catch (Exception e)
		//				{
		//					throw new RuntimeException("Can not access field " + curClass.getName() + "."
		//					        + field.getName(), e);
		//				}
		//			}
		//			curClass = curClass.getSuperclass();
		//		}
		//
		//		return findEventBus(from.getParent());
	}

	@SuppressWarnings("serial")
	public static void initEventBusOwner(final EventBusOwner container, final EventBus bus)
	{
		container.addAttachListener(new AttachListener()
		{
			@Override
			public void attach(AttachEvent event)
			{
				EventBusUtils.setChildrenIds(container);
				bus.subscribe(container);
				bus.send(event);
			}
		});

		EventBusUtils.setEventProducers(container, bus);
	}

	private static void setEventProducers(Component component, final EventBus bus)
	{
		component.addListener(new Listener()
		{
			@Override
			public void componentEvent(Event event)
			{
				bus.send(event);
			}
		});
		setEventProducersForChildren(component, bus);
	}

	private static void setEventProducersForChildren(Component component, EventBus bus)
	{
		if (!(component instanceof HasComponents))
		{
			return;
		}
		HasComponents container = (HasComponents) component;
		Iterator<Component> iterator = container.iterator();
		while (iterator.hasNext())
		{
			Component child = iterator.next();
			setEventProducers(child, bus);
		}
	}

	public static EventBusOwner findEventBusOwner(ConnectorEvent event)
	{
		if (!(event.getSource() instanceof Component))
		{
			return null;
		}
		Component c = (Component) event.getSource();
		while (c != null)
		{
			if (c instanceof EventBusOwner)
			{
				return (EventBusOwner) c;
			}
			c = c.getParent();
		}
		return null;
	}
}
