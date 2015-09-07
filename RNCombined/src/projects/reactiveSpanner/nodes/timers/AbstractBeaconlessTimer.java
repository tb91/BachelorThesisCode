package projects.reactiveSpanner.nodes.timers;

import java.util.UUID;

import sinalgo.nodes.timers.Timer;
import sinalgo.tools.logging.Logging;

public abstract class AbstractBeaconlessTimer extends Timer
{
	protected static Logging logger = Logging.getLogger();
	/**
	 * The specific ID this message is assigned to.
	 */
	protected final UUID tcID;
	protected static double t_max;
	protected static double r;
	
	protected boolean isQuiet = false;

	protected AbstractBeaconlessTimer(final UUID tcID)
	{
		this.tcID = tcID;
	}
	/**
	 * Cancel this timer but do not remove it. Instead we set <b>isQuiet</b> to true
	 */
	public void cancel() {
		isQuiet = true;
	}
	
	public UUID getID()
	{
		return this.tcID;
	}
}