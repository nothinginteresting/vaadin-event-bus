package net.nothinginteresting.vaadin.event.bus;

import net.engio.mbassy.IPublicationErrorHandler;
import net.engio.mbassy.PublicationError;
import net.engio.mbassy.bus.BusConfiguration;
import net.engio.mbassy.bus.MBassador;

public class EventBus
{
	private final MBassador<Object> bus;

	public EventBus()
	{
		bus = createBus();
	}

	private MBassador<Object> createBus()
	{
		BusConfiguration conf = BusConfiguration.Default();
		conf.setMetadataReader(new EventBusMetadataReader());
		MBassador<Object> bus = new MBassador<Object>(conf);
		bus.addErrorHandler(new IPublicationErrorHandler()
		{
			@Override
			public void handleError(PublicationError error)
			{
				throw new RuntimeException(error.getCause());
			}
		});
		return bus;
	}

	public void subscribe(Object object)
	{
		bus.subscribe(object);
	}

	public void send(Object event)
	{
		bus.publish(event);
	}
}
