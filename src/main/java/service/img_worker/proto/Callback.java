package service.img_worker.proto;

public interface Callback<T> {
	void onEvent(T t);
}
