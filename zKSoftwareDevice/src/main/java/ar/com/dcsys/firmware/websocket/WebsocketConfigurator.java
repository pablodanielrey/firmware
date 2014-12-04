package ar.com.dcsys.firmware.websocket;

import javax.websocket.server.ServerEndpointConfig;

import ar.com.dcsys.firmware.App;

public class WebsocketConfigurator extends ServerEndpointConfig.Configurator {
	
	@Override
	public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
		
		if (!endpointClass.isAssignableFrom(CommandsEndpoint.class)) {
			throw new InstantiationException(endpointClass.getName() + " wrong endpoint");
		}
		
		CommandsEndpoint e = App.getWeldContainer().instance().select(CommandsEndpoint.class).get();
		
		return (T)e;
	}

}
