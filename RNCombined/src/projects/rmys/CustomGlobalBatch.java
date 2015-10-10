package projects.rmys;

import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

/**
 * CustomGlobal class for batch mode.
 * This is a singleton class.
 * 
 * @author Mavs
 */
public class CustomGlobalBatch
{
	private static Logging logger = Logging.getLogger();
	
	private static double R = -1;
	static {
		try {
			R = Configuration.getDoubleParameter("UDG/rMax");

		} catch (CorruptConfigurationEntryException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}
	
	private static CustomGlobalBatch instance = null;
	public static CustomGlobalBatch getInstance() {
		if(instance == null) {
			logger.logln(LogL.INFO, "Create CustomGlobalBatch");
			instance = new CustomGlobalBatch();
		}
		return instance;
	}
	
	/** 
	 * Private constructor - this is a singleton implementation
	 */
	private CustomGlobalBatch() {
	}

	// for recording:
	private static int nodeDensity;
	

	
	public boolean hasTerminated()
	{
		return true;
	}
	
	public void preRun()
	{

	}

	public void postRound()
	{

	}

	public void onExit()
	{		
		System.out.println("exiting!");
	}

	
		
	
	
}
