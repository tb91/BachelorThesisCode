package projects.reactiveSpanner.exceptions;

public class IllegalSelectedNodeException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4994863606984185793L;
	
	
	public IllegalSelectedNodeException()
	{
		super("Selected node is invalid.");
	}
	
	public IllegalSelectedNodeException(String s)
	{
		super(s);
	}
}
