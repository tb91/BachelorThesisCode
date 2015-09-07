package projects.reactiveSpanner.exceptions;

public class NoAllocatedStartNodeException extends NoAllocatedNodeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 461264951289928835L;

	public NoAllocatedStartNodeException()
	{
		super("The allocation of the start node is necessary for the selected option.");
	}
	
	public NoAllocatedStartNodeException(String s)
	{
		super(s);
	}
}