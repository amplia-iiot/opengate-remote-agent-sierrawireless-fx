package es.amplia.product.devices.sierrawireless.connection;

public interface WebSocketConnectionListener extends ConnectionListener {

	public void onOpen();
	
	public void onError(Throwable arg0);
	
	public void onClose(int arg0, String arg1);
}
