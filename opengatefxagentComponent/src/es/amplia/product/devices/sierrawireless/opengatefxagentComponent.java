package es.amplia.product.devices.sierrawireless;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Properties;
import java.util.logging.Logger;

import io.legato.Component;
import io.legato.Level;
import io.legato.Result;
import io.legato.api.Data;
import io.legato.api.Data.ConnectionStateHandler;
import io.legato.api.Data.RequestObjRef;
import io.legato.api.Data.Technology;

public class opengatefxagentComponent implements Component {
	private Logger logger;
	private Data data;
	private Agent agent;
	
	public class ConnectionHandler implements ConnectionStateHandler {

		public void handle(String intfName, boolean isConnected) {
			logger.log(Level.INFO, "Interface " + intfName + ", connected: " + isConnected);

			if (isConnected) {
				// Starting Agent
				try {
					InetAddress inetAddress = null;
					NetworkInterface ni = NetworkInterface.getByName(intfName);
					for (InterfaceAddress addr : ni.getInterfaceAddresses()) {
						if (addr.getAddress().getAddress().length == 4) { // Is IPv4
							logger.log(Level.INFO, "Connecting Http Connection through IP address: " + addr.getAddress());
							inetAddress = addr.getAddress();
							break;
						}
					}
					
					InetAddress inetEth0Address = null;
					NetworkInterface niEth0 = NetworkInterface.getByName("eth0");
					for (InterfaceAddress addr : niEth0.getInterfaceAddresses()) {
						if (addr.getAddress().getAddress().length == 4) { // Is IPv4
							logger.log(Level.INFO, "Connecting IOT Http Connction through IP address: " + addr.getAddress());
							inetEth0Address = addr.getAddress();
							break;
						}
					}
					Properties prop = new Properties();
					prop.setProperty("database.synchronization.polling", "10000");
					
					prop.setProperty("connection.common.deviceId", "");
					prop.setProperty("connection.common.apiKey", "41d0f0e8-1de0-434c-8a73-62a610dd60e3");
					
					prop.setProperty("connection.http.remote.address", "api.opengate.es/south");
					prop.setProperty("connection.http.remote.port", "0");
					prop.setProperty("connection.http.remote.uri", "/v70/devices");
					
					prop.setProperty("iot.connection.http.remote.address", "192.168.3.3");
					prop.setProperty("iot.connection.http.remote.port", "5000");
					
					prop.setProperty("connection.websocket.enable", "true");
					prop.setProperty("connection.websocket.remote.address", "api.opengate.es/south/ws");
					prop.setProperty("connection.websocket.remote.port", "0");
					prop.setProperty("connection.websocket.remote.uri", "/v70/sessions");
					
					prop.setProperty("connection.at.command.port", "/dev/ttyAT");
					prop.setProperty("connection.at.command.baudrate", "115200");
					
					agent = new Agent(prop, inetAddress, inetEth0Address);
					agent.setLogger(logger);
					agent.start();
	
				} catch (Exception e) {
					logger.log(Level.ERR, "Error executing agent!!");
					e.printStackTrace();
				}
			}

		}
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	public void componentInit() {
		logger.log (Level.INFO, "Starting agent...");
		System.setProperty("java.net.preferIPv4Stack", "true");
		
		// Technology election
		this.data.AddConnectionStateHandler(new ConnectionHandler());

		Result techSetResult = this.data.SetTechnologyRank(1, Technology.CELLULAR);
		if (!techSetResult.equals(Result.OK))
			logger.log(Level.ERR, "Error setting technology: " + techSetResult);
		logger.log(Level.INFO, "Result setting technology: " + techSetResult);

		RequestObjRef reqResult = this.data.Request();
		if (reqResult.getRef() < 0)
			logger.log(Level.ERR, "Error requesting connection: " + reqResult);
		logger.log(Level.INFO, "Result requesting connection: " + reqResult.getRef());
	}
	
	public void setData(Data data) {
		logger.log(Level.INFO, "Setting DATA");
		this.data = data;
	}
}
