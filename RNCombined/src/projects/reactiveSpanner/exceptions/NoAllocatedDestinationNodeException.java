package projects.reactiveSpanner.exceptions;

public class NoAllocatedDestinationNodeException extends NoAllocatedNodeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 461264951289928835L;

	public NoAllocatedDestinationNodeException()
	{
		super("The allocation of the destination node is necessary for the selected option.");
	}
	
	public NoAllocatedDestinationNodeException(String s)
	{
		super(s);
	}
}