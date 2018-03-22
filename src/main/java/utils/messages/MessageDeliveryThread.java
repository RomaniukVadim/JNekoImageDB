package utils.messages;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;

public class MessageDeliveryThread implements Runnable {
	private final LinkedBlockingQueue<Msg> messages;
	private final ConcurrentHashMap<UUID, CopyOnWriteArraySet<MessageReceiver>> subscribers;
	private final AtomicBoolean active = new AtomicBoolean(false);

	public MessageDeliveryThread(
			LinkedBlockingQueue<Msg> messages,
			ConcurrentHashMap<UUID, CopyOnWriteArraySet<MessageReceiver>> subscribers) {
		this.messages = messages;
		this.subscribers = subscribers;
	}

	@Override public void run() {
		while(true) {
			try {
				active.set(false);
				final Msg message = messages.poll(9999, TimeUnit.DAYS);
				active.set(true);
				final UUID uuidTo = message.getToUUID();

				switch (message.getMessageDirection()) {
				case SEND_TO_EXECUTOR:
					final CopyOnWriteArraySet<MessageReceiver> receivers = subscribers.get(uuidTo);
					if (Objects.nonNull(receivers)) {
						if (message.isToUiThread()) {
							Platform.runLater(() -> receivers.forEach(m -> m.onMessageReceive(message)));
						} else {
							receivers.forEach(m -> m.onMessageReceive(message));
						}
					}
					break;
				case RETURN_TO_SENDER:
					if (Objects.isNull(message.getReceiver())) break;
					if (message.isToUiThread()) {
						Platform.runLater(() -> message.getReceiver().onMessageReceive(message));
					} else {
						message.getReceiver().onMessageReceive(message);
					}
					break;
				case BROADCAST:
					// TODO: add broadcast messages
					break;
				}
			} catch (InterruptedException e) {
				return;
			} catch (Exception e) {
				System.err.println("ERROR: " + e.getClass().getSimpleName() + " || " + e.getMessage());
			}
		}
	}

	public boolean isActive() {
		return active.get();
	}
}
