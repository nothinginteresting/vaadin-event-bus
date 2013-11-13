package net.nothinginteresting.vaadin.event.bus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import net.engio.mbassy.common.IPredicate;
import net.engio.mbassy.common.ReflectionUtils;
import net.engio.mbassy.listener.Enveloped;
import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.listener.MessageListenerMetadata;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.subscription.MessageEnvelope;

public class EventBusMetadataReader extends MetadataReader
{
	//  This predicate is used to find all message listeners (methods annotated with @ViewHandler)
	private static final IPredicate<Method> AllMessageHandlers = new IPredicate<Method>()
	{
		@Override
		public boolean apply(Method target)
		{
			boolean result = target.getAnnotation(ViewHandler.class) != null;
			//			if (result)
			//			{
			//				System.out.println("EventBusMetadataReader: collect handler "
			//				        + target.getDeclaringClass().getName() + "." + target.getName());
			//			}

			return result;
		}
	};

	// get all listeners defined by the given class (includes
	// listeners defined in super classes)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public MessageListenerMetadata getMessageListener(Class target)
	{

		//System.out.println("getMessageListener for " + target);

		MessageListenerMetadata listenerMetadata = new MessageListenerMetadata(target);
		// get all handlers (this will include all (inherited) methods directly annotated using @Handler)
		List<Method> allHandlers = ReflectionUtils.getMethods(AllMessageHandlers, target);

		//		// retain only those that are at the bottom of their respective class hierarchy (deepest overriding method)
		//		List<Method> bottomMostHandlers = new LinkedList<Method>();
		//		for (Method handler : allHandlers)
		//		{
		//			if (!ReflectionUtils.containsOverridingMethod(allHandlers, handler))
		//			{
		//				bottomMostHandlers.add(handler);
		//			}
		//		}

		// for each handler there will be no overriding method that specifies @Handler annotation
		// but an overriding method does inherit the listener configuration of the overwritten method
		for (int i = allHandlers.size(); i > 0; i--)
		{
			Method handler = allHandlers.get(i - 1);

			//			System.out.println("EventBusMetadataReader: check handler "
			//			        + handler.getDeclaringClass().getName() + "." + handler.getName());

			ViewHandler handlerConfig = handler.getAnnotation(ViewHandler.class);
			if (!isValidMessageHandler(handler))
			{
				continue; // disabled or invalid listeners are ignored
			}
			Method overriddenHandler = ReflectionUtils.getOverridingMethod(handler, target);

			if (overriddenHandler != null)
			{
				if (!Modifier.isPrivate(overriddenHandler.getModifiers()))
				{
					// if a handler is overwritten it inherits the configuration of its parent method
					handler = overriddenHandler;
					//					System.out.println("Found overriddenHandler "
					//					        + overriddenHandler.getDeclaringClass() + "."
					//					        + overriddenHandler.getName() + " for "
					//					        + handler.getDeclaringClass().getName() + "." + handler.getName());
				}
				else
				{
					//					System.out.println("Skip overriddenHandler "
					//					        + overriddenHandler.getDeclaringClass() + "."
					//					        + overriddenHandler.getName() + " for private "
					//					        + handler.getDeclaringClass().getName() + "." + handler.getName());
				}
			}

			IMessageFilter[] filter = new IMessageFilter[] { new EventBusFilter() };
			MessageHandlerMetadata handlerMetadata =
			    new MessageHandlerMetadata(handler, filter, handlerConfig, listenerMetadata);
			listenerMetadata.addHandler(handlerMetadata);

			//			System.out.println("EventBusMetadataReader: register handler "
			//			        + handler.getDeclaringClass().getName() + "." + handler.getName());

		}
		return listenerMetadata.addHandlers(super.getMessageListener(target).getHandlers());
	}

	private boolean isValidMessageHandler(Method handler)
	{
		if (handler == null || handler.getAnnotation(ViewHandler.class) == null)
		{
			return false;
		}
		Enveloped envelope = handler.getAnnotation(Enveloped.class);
		if (envelope != null
		        && !MessageEnvelope.class.isAssignableFrom(handler.getParameterTypes()[0]))
		{
			//System.out.println("Message envelope configured but no subclass of MessageEnvelope found as parameter");
			return false;
		}
		if (envelope != null && envelope.messages().length == 0)
		{
			//System.out.println("Message envelope configured but message types defined for handler");
			return false;
		}
		return true;
	}

}
