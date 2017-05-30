package es.amplia.product.devices.sierrawireless.connection;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import io.legato.Level;

public class WebSocketConnection {

	private String remoteAddress;
	private int remotePort;
	private String remoteBaseUri;
	private String deviceId;
	private String apiKey;
	
	private URI uri;
	private WebSocketClient wsc;
	private WebSocketConnectionListener listener;
	
	private List<HttpCookie> cookies;	
	private ClientUpgradeRequest request;
	
	private Session session;
	private Logger logger;
	
	public WebSocketConnection(String remoteAddress, int remotePort, String remoteBaseUri, String deviceId, String apiKey) throws Exception {
		this(remoteAddress, remotePort, remoteBaseUri, deviceId, apiKey, null);
	}
	public WebSocketConnection(String remoteAddress, int remotePort, String remoteBaseUri, String deviceId, String apiKey, InetAddress localAddress) throws Exception {
		super();
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		this.remoteBaseUri = remoteBaseUri.startsWith("/")? remoteBaseUri:"/" + remoteBaseUri;
		this.remoteBaseUri = remoteBaseUri.endsWith("/")? this.remoteBaseUri:this.remoteBaseUri + "/";
		this.deviceId = deviceId;
		this.apiKey = apiKey;
		
		uri = new URI("ws://" + this.remoteAddress + ( (this.remotePort!=0)?(":" + this.remotePort):"" ) + this.remoteBaseUri + this.deviceId + "?X-ApiKey=" + this.apiKey);
		
		cookies = new ArrayList<HttpCookie>();
		request = new ClientUpgradeRequest();
		
		HttpCookie cookie = new HttpCookie("X-ApiKey", apiKey);
		cookies.add(cookie);
		request.setCookies(cookies);
		request.setHeader("X-ApiKey", apiKey);
		
		wsc = new WebSocketClient();
		if (localAddress != null) wsc.setBindAddress(new InetSocketAddress(localAddress, 0));
		wsc.start();
	}
	
	public void connect() throws IOException {
		this.logger.log(Level.INFO, "Connecting to URI: " + this.uri);
		wsc.connect(new WebSocketListener() {
			
			@Override
			public void onWebSocketError(Throwable arg0) {
				if (listener != null) listener.onError(arg0);
			}
			
			@Override
			public void onWebSocketConnect(Session arg0) {
				session = arg0;
				if (listener != null) listener.onOpen();
			}
			
			@Override
			public void onWebSocketClose(int arg0, String arg1) {
				session.close(arg0, arg1);
				if (listener != null) listener.onClose(arg0, arg1);
			}
			
			@Override
			public void onWebSocketText(String arg0) {
				if (listener != null) listener.onMessage(arg0);
			}
			
			@Override
			public void onWebSocketBinary(byte[] arg0, int arg1, int arg2) {
				logger.log(Level.INFO, "Binary data received: " + arg0 + ", " + arg1 + ", " + arg2);
			}
		}, this.uri, this.request);
	}
	
	public void disconnect() throws Exception {
		wsc.stop();
	}
	
	public void setListener (WebSocketConnectionListener listener) {
		this.listener = listener;
	}
	
	public void sendMessage (String msg) throws IOException {
		if (session != null) session.getRemote().sendString(msg);
	}
	
	public void setLogger (Logger logger) {
		this.logger = logger;
	}
	
}
