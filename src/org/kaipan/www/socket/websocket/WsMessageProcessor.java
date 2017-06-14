package org.kaipan.www.socket.websocket;

import java.util.Map;

import org.kaipan.www.socket.controller.IController;
import org.kaipan.www.socket.core.IMessageProcessor;
import org.kaipan.www.socket.core.Message;
import org.kaipan.www.socket.core.Socket;
import org.kaipan.www.socket.core.WriteProxy;

public class WsMessageProcessor implements IMessageProcessor
{
	private WsConfig config;
	
	public WsMessageProcessor(WsConfig config) 
	{
		this.config = config;
	}

	@Override
	public void process(Socket socket, Message message, WriteProxy writeProxy, Map<String, IController> map)
	{
		
	}
}
