package net.nothinginteresting.vaadin.event.bus;

import com.vaadin.ui.HasComponents;

public interface EventBusOwner extends HasComponents
{
	EventBus getEventBus();
}
