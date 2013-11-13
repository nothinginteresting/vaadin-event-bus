package net.nothinginteresting.vaadin.event.bus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import net.engio.mbassy.dispatch.HandlerInvocation;
import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.listener.Invoke;
import net.engio.mbassy.listener.MessageListenerMetadata;

public class MessageHandlerMetadata extends net.engio.mbassy.listener.MessageHandlerMetadata
{
	@SuppressWarnings("unused")
	private ViewHandler handlerConfig;

	public MessageHandlerMetadata(Method handler, IMessageFilter[] filter, ViewHandler handlerConfig, MessageListenerMetadata listenerConfig)
	{
		super(handler, filter, createHandlerStub(), listenerConfig);
	}

	private static Handler createHandlerStub()
	{
		return new Handler()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return Handler.class;
			}

			@Override
			public Filter[] filters()
			{
				return null;
			}

			@Override
			public Invoke delivery()
			{
				return Invoke.Synchronously;
			}

			@Override
			public int priority()
			{
				return 0;
			}

			@Override
			public boolean rejectSubtypes()
			{
				return true;
			}

			@Override
			public boolean enabled()
			{
				return true;
			}

			@Override
			public Class<? extends HandlerInvocation> invocation()
			{
				return ReflectiveHandlerInvocation.class;
			}
		};
	}
}
