package projects.defaultProject.models.distributionModels;

import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.models.DistributionModel;
import sinalgo.nodes.Position;

/**
 * Distributes the nodes on a sphere.
 */
public class Sphere extends DistributionModel {
	private double radius = 0.0;
	private double radius_max=0.0;
	private double radiusStep=0.0;
	private double oneStep = 0.0;
	private int number = 0;
	private boolean first=true;
	
	/* (non-Javadoc)
	 * @see sinalgo.models.DistributionModel#initialize()
	 */
	@Override
	public void initialize() {
		String parameter = this.getParamString();
		if(parameter.equals("")){
			try{
				radius_max= Configuration.getDoubleParameter("UDG/rMax");
			}catch (CorruptConfigurationEntryException e){
				e.printStackTrace();
			}
		}
		else{
			radius = Double.parseDouble(parameter);
		}
		radius=5;
		radiusStep = (30-radius) / numberOfNodes;
		oneStep = 360.0 / numberOfNodes;
		System.out.println(radius_max);
	}
	
	@Override
	public Position getNextPosition() {
		if(first){
			first=false;
			return new Position(Configuration.dimX/2.0, Configuration.dimY/2.0,0);
		}
		Position pos = new Position();
		pos.xCoord = (Configuration.dimX / 2.0) + ((radius) * Math.cos(Math.toRadians((number * oneStep))));
		pos.yCoord = (Configuration.dimY / 2.0) + ((radius) * Math.sin(Math.toRadians((number * oneStep))));
		radius+=radiusStep;
		//System.out.println(radius);
		number++;
		
		return pos;
	}
}
