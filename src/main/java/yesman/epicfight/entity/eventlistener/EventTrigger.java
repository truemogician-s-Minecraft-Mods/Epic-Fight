package yesman.epicfight.entity.eventlistener;

import java.util.UUID;

import com.google.common.base.Function;

public class EventTrigger<T extends PlayerEvent<?>> implements Comparable<EventTrigger<?>> {
	private UUID uuid;
	private Function<T, Boolean> function;
	private final int priority;
	
	public EventTrigger(UUID uuid, Function<T, Boolean> function, int priority) {
		this.uuid = uuid;
		this.function = function;
		this.priority = priority;
	}
	
	public boolean is(UUID uuid) {
		return this.uuid.equals(uuid);
	}
	
	public boolean trigger(T args) {
		return this.function.apply(args);
	}
	
	public int getPriority() {
		return this.priority;
	}
	
	@Override
	public int compareTo(EventTrigger<?> o) {
		if (this.uuid == o.uuid) {
			return 0;
		} else {
			return this.priority > o.priority ? 1 : -1;
		}
	}
	
	public static <T extends PlayerEvent<?>> EventTrigger<T> makeEvent(UUID uuid, Function<T, Boolean> function, int priority) {
		return new EventTrigger<T>(uuid, function, priority);
	}
}