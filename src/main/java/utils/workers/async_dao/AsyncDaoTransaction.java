package utils.workers.async_dao;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class AsyncDaoTransaction<T> {
	private final UUID uuid = UUID.randomUUID();
	private final HashSet<T> objects = new HashSet<>();
	private final AsyncDaoTransactionType type;

	public AsyncDaoTransaction(AsyncDaoTransactionType type) {
		this.type = type;
	}

	public AsyncDaoTransaction(AsyncDaoTransactionType type, T ... objects) {
		this.type = type;
		this.getObjects().addAll(Arrays.asList(objects));
	}

	public void push(T object) {
		getObjects().add(object);
	}

	public UUID getUuid() {
		return uuid;
	}

	public AsyncDaoTransactionType getType() {
		return type;
	}

	public HashSet<T> getObjects() {
		return objects;
	}
}
