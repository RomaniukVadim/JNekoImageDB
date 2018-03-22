package utils.messages;

import java.util.UUID;

public class Msg<T> {
	public enum Direction {
		SEND_TO_EXECUTOR, RETURN_TO_SENDER, BROADCAST
	}

	public enum Priority {
		HIGH, NORMAL, LOW
	}

	private Priority priority;
	private Thread fromThread;
	private T payload;

	private Direction messageDirection;
	private UUID fromUUID;
	private UUID toUUID;
	private UUID msgUUID;
	private Long timestamp;
	private Long messageID;
	private int messageCategory;
	private MessageReceiver<T> receiver;
	private boolean toUiThread = false;

	public Msg(T payload, int messageCategory, MessageReceiver<T> receiver) {
		this.setFromThread(Thread.currentThread());
		setTimestamp(System.currentTimeMillis());
		this.setPayload(payload);
		this.setPriority(Priority.NORMAL);
		this.messageCategory = messageCategory;
	}

	public Msg(T payload, int messageCategory, UUID fromUUID, UUID toUUID, MessageReceiver<T> receiver) {
		this.setFromThread(Thread.currentThread());
		setTimestamp(System.currentTimeMillis());
		this.setPayload(payload);
		this.setPriority(Priority.NORMAL);
		this.messageCategory = messageCategory;
		this.msgUUID = UUID.randomUUID();
		this.fromUUID = fromUUID;
		this.toUUID = toUUID;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Thread getFromThread() {
		return fromThread;
	}

	public void setFromThread(Thread fromThread) {
		this.fromThread = fromThread;
	}

	public T getPayload() {
		return payload;
	}

	public void setPayload(T payload) {
		this.payload = payload;
	}

	public Direction getMessageDirection() {
		return messageDirection;
	}

	public void setMessageDirection(Direction messageDirection) {
		this.messageDirection = messageDirection;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Long getMessageID() {
		return messageID;
	}

	public void setMessageID(Long messageID) {
		this.messageID = messageID;
	}

	public MessageReceiver<T> getReceiver() {
		return receiver;
	}

	public void setReceiver(MessageReceiver<T> receiver) {
		this.receiver = receiver;
	}

	public int getMessageCategory() {
		return messageCategory;
	}

	public void setMessageCategory(int messageCategory) {
		this.messageCategory = messageCategory;
	}

	public boolean isToUiThread() {
		return toUiThread;
	}

	public void setToUiThread(boolean toUiThread) {
		this.toUiThread = toUiThread;
	}

	public UUID getFromUUID() {
		return fromUUID;
	}

	public void setFromUUID(UUID fromUUID) {
		this.fromUUID = fromUUID;
	}

	public UUID getToUUID() {
		return toUUID;
	}

	public void setToUUID(UUID toUUID) {
		this.toUUID = toUUID;
	}

	public UUID getMsgUUID() {
		return msgUUID;
	}

	public void setMsgUUID(UUID msgUUID) {
		this.msgUUID = msgUUID;
	}
}
