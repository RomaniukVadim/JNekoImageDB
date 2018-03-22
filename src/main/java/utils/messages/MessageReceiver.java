package utils.messages;

public interface MessageReceiver<T> {
	void onMessageReceive(Msg<T> message);
}
