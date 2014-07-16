package ar.com.dcsys.firmware.websocket;

import javax.websocket.server.ServerEndpointConfig;

import ar.com.dcsys.firmware.App;

public class WebsocketConfigurator extends ServerEndpointConfig.Configurator {

	@Override
	public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
		
		if (!endpointClass.isAssignableFrom(EchoEndpoint.class)) {
			throw new InstantiationException(endpointClass.getName() + " wrong endpoint");
		}
		
		EchoEndpoint e = App.getWeldContainer().instance().select(EchoEndpoint.class).get();
		
		return (T)e;
	}
	
}
