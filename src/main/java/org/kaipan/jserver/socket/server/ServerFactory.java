package org.kaipan.jserver.socket.server;

import java.lang.reflect.Constructor;

import org.kaipan.jserver.socket.core.Config;
import org.kaipan.jserver.socket.core.Server;

/**
 * server factory
 *     create wanted server for u
 * 
 * @author will<pan.kai@icloud.com>
 */
public class ServerFactory
{
	public static Server create(String name, Config config, String filename) 
	{
		try {
			Class<?> ServerClass = Class.forName(name);
			
			Class<?>[] classes = new Class[] {
				Config.class,
				String.class,
				String.class
			};
			
			Constructor<?> constructor = ServerClass.getConstructor(classes);
			
			Object[] arguments = new Object[] {
				config, filename, null
			};
			
			return (Server) constructor.newInstance(arguments);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to instantiate " + name);
		}
	}
	
	public static Server create(String name, Config config, String filename, String path) 
	{
		try {
			Class<?> ServerClass = Class.forName(name);
			
			Class<?>[] classes = new Class[] {
				Config.class,
				String.class,
				String.class
			};
			
			Constructor<?> constructor = ServerClass.getConstructor(classes);
			
			Object[] arguments = new Object[] {
				config, filename, path
			};
			
			return (Server) constructor.newInstance(arguments);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to instantiate " + name);
		}
	}
}
