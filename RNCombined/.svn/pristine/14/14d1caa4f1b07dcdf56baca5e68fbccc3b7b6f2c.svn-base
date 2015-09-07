package projects.reactiveSpanner;

import java.util.Set;

import sinalgo.nodes.Node;
import sinalgo.nodes.Position;


public class Disk2D
{

	public final Position center;
	public final double radius;
	public final Set<? extends Node> nodesInDisk;	//nodes within the disk that are closer than r to center position
	
	public Disk2D(final Position center, final double radius, final Set<? extends Node> nodesInDisk)
	{
		this.center = center;
		this.radius = radius;
		this.nodesInDisk = nodesInDisk;
	}
	@Override 
	public String toString()
	{
		return new StringBuilder().append("Center= ").append(center).append(", r=").append(radius).toString();
	}

}
