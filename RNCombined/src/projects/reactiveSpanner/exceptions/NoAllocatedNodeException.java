package projects.reactiveSpanner.exceptions;

public class NoAllocatedNodeException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 461264951289928835L;

	public NoAllocatedNodeException()
	{
		super("The allocation of a node is necessary for the selected option.");
	}
	
	public NoAllocatedNodeException(String s)
	{
		super(s);
	}
}
