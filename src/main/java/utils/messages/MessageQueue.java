package utils.messages;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Platform;

public class MessageQueue {
	private static final int processorsCount = Runtime.getRuntime().availableProcessors();

	private static final CopyOnWriteArrayList<MessageDeliveryThread> threads = new CopyOnWriteArrayList<>();
	private static final LinkedBlockingQueue<Msg> messages = new LinkedBlockingQueue<>();
	private static final ConcurrentHashMap<UUID, CopyOnWriteArraySet<MessageReceiver>> subscribers = new ConcurrentHashMap<>();
	private static final ExecutorService executor = Executors.newFixedThreadPool(processorsCount <= 4 ? 4 : processorsCount);

	public static void init() {
		for (int i=0; i<processorsCount; i++) {
			final MessageDeliveryThread mdt = new MessageDeliveryThread(messages, subscribers);
			final Thread thread = new Thread(mdt);
			threads.add(mdt);
			executor.submit(thread);
		}
	}

	public static void dispose() {
		executor.shutdownNow();
	}

	public <T, V extends Class> Msg<T> createReply(Msg oldMsg, T payload) {
		final Msg<T> msg = new Msg(payload, oldMsg.getMessageCategory(), oldMsg.getReceiver());
		msg.setFromUUID(oldMsg.getToUUID());
		msg.setToUUID(oldMsg.getFromUUID());
		msg.setMessageID(oldMsg.getMessageID());
		msg.setMessageCategory(oldMsg.getMessageCategory());
		msg.setToUiThread(oldMsg.isToUiThread());
		return msg;
	}

	public static <T> void reply(Msg<T> m) {
		if (Objects.isNull(m)) return;
		m.setMessageDirection(Msg.Direction.RETURN_TO_SENDER);
		messages.add(m);
	}

	public static <T> void send(Msg<T> m) {
		if (Objects.isNull(m)) return;
		m.setMessageDirection(Msg.Direction.SEND_TO_EXECUTOR);
		m.setToUiThread(Platform.isFxApplicationThread());
		messages.add(m);
	}

	public static <T> void subscribe(UUID mUUID, MessageReceiver<T> mr) {
		if (Objects.isNull(mUUID) || Objects.isNull(mr)) return;

		if (subscribers.contains(mUUID)) {
			if (!subscribers.get(mUUID).contains(mUUID)) subscribers.get(mUUID).add(mr);
		} else {
			final CopyOnWriteArraySet<MessageReceiver> mrs = new CopyOnWriteArraySet<>();
			mrs.add(mr);
			subscribers.put(mUUID, mrs);
		}
	}
}
