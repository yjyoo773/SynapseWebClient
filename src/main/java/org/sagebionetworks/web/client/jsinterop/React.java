package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class React {
	public static native <P extends ReactComponentProps> ReactNode createElement(ReactComponentType<P> component, P props);

	public static native <P extends ReactComponentProps> ReactNode createElement(ReactComponentType<P> component, P props, ReactNode child);

	public static native <P extends ReactComponentProps> ReactNode createElement(ReactComponentType<P> component, P props, ReactNode[] children);

	/**
	 * Wraps a component in SynapseContextProvider. Nearly all Synapse React Client components must be wrapped in this context, so this utility
	 * simplifies creating the wrapper.
	 *
	 * For setting props, use {@link org.sagebionetworks.web.client.context.SynapseContextPropsProvider}
	 * @param component
	 * @param props
	 * @param wrapperProps
	 * @param <P>
	 * @return
	 */
	@JsOverlay
	public static <P extends ReactComponentProps> ReactNode createElementWithSynapseContext(ReactComponentType<P> component, P props, SynapseContextProviderProps wrapperProps) {
		ReactNode componentElement = createElement(component, props);
		return createElement(SRC.SynapseContext.SynapseContextProvider, wrapperProps, componentElement);
	}
}
