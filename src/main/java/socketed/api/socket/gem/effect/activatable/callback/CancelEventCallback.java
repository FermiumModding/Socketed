package socketed.api.socket.gem.effect.activatable.callback;

import net.minecraft.util.DamageSource;

import javax.annotation.Nullable;

public class CancelEventCallback implements IEffectCallback, ICancellableCallback {
	
	private boolean cancelled;
	@Nullable
	private final DamageSource source;
	
	public CancelEventCallback(boolean val) {
		this(val, null);
	}

	public CancelEventCallback(boolean val, @Nullable DamageSource source) {
		this.cancelled = val;
		this.source = source;
	}
	
	public void setCancelled(boolean val) {
		this.cancelled = val;
	}
	
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Nullable
	public DamageSource getSource() {
		return this.source;
	}
}