package net.nothinginteresting.vaadin.event.bus;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.engio.mbassy.IPublicationErrorHandler;
import net.engio.mbassy.PublicationError;
import net.engio.mbassy.dispatch.HandlerInvocation;
import net.engio.mbassy.subscription.SubscriptionContext;

import com.vaadin.event.ConnectorEvent;

public class ReflectiveHandlerInvocation extends HandlerInvocation
{

	public ReflectiveHandlerInvocation(SubscriptionContext context)
	{
		super(context);
	}

	protected void handlePublicationError(PublicationError error)
	{
		@SuppressWarnings("unchecked")
		Collection<IPublicationErrorHandler> handlers =
		    getContext().getOwningBus().getRegisteredErrorHandlers();
		for (IPublicationErrorHandler handler : handlers)
		{
			handler.handleError(error);
		}
	}

	private List<Object> getHandlerArgs(final Object message, final Object listener, Method handler)
	{
		List<Object> result = new ArrayList<Object>();
		result.add(message);
		if (handler.getGenericParameterTypes().length < 2)
		{
			return result;
		}

		for (int i = 1; i < handler.getParameterAnnotations().length; i++)
		{
			for (Annotation parameterAnnotation : handler.getParameterAnnotations()[i])
			{
				if (parameterAnnotation.annotationType() == Widget.class)
				{
					Widget widget = (Widget) parameterAnnotation;
					String widgetName = widget.value();
					//					if (widgetName.isEmpty())
					//					{
					//						Type t;
					//						TypeVariable<?> type =
					//						    (TypeVariable<?>) handler.getGenericParameterTypes()[i];
					//						widgetName = type.getName();
					//					}
					EventBusOwner owner = EventBusUtils.findEventBusOwner((ConnectorEvent) message);
					if (owner == null)
					{
						throw new RuntimeException("Can not find EventBusOwner for " + message);
					}
					result.add(EventBusUtils.findWidget(widgetName, owner));
					break;
				}
			}
		}
		return result;
	}

	protected void invokeHandler(final Object message, final Object listener, Method handler)
	{
		List<Object> args = getHandlerArgs(message, listener, handler);
		try
		{
			handler.invoke(listener, args.toArray());
		}
		catch (IllegalAccessException e)
		{
			handlePublicationError(new PublicationError(e, "Error during messageHandler notification. "
			        + "The class or method is not accessible", handler, listener, message));
		}
		catch (IllegalArgumentException e)
		{
			handlePublicationError(new PublicationError(e, "Error during messageHandler notification. "
			        + "Wrong arguments passed to method. Was: "
			        + message.getClass()
			        + "Expected: "
			        + handler.getParameterTypes()[0], handler, listener, message));
		}
		catch (InvocationTargetException e)
		{
			handlePublicationError(new PublicationError(e, "Error during messageHandler notification. "
			        + "Message handler threw exception", handler, listener, message));
		}
		catch (Throwable e)
		{
			handlePublicationError(new PublicationError(e, "Error during messageHandler notification. "
			        + "Unexpected exception", handler, listener, message));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void invoke(final Object listener, final Object message)
	{
		invokeHandler(message, listener, getContext().getHandlerMetadata().getHandler());
	}
}
