package asw.soa.view;

import java.rmi.RemoteException;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import asw.soa.data.ModelData;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

class VisualComponent  implements Locatable{
	
	private ModelData _mdata = new ModelData();
	private DEVSSimulatorInterface.TimeDouble simulator = null;
	
	VisualComponent(ModelData data, final DEVSSimulatorInterface.TimeDouble simulator){
		this._mdata = data;
		this.simulator = simulator;
	}
	
	@Override
	public DirectedPoint getLocation() throws RemoteException {
		double fraction = (this.simulator.getSimulatorTime() - this._mdata.startTime) / (this._mdata.stopTime - this._mdata.startTime);
		double x = this._mdata.origin.x + (this._mdata.destination.x - this._mdata.origin.x) * fraction;
		double y = this._mdata.origin.y + (this._mdata.destination.y - this._mdata.origin.y) * fraction;
		return new DirectedPoint(x, y, 0, 0.0, 0.0, this._mdata.theta);
	}

	@Override
	public Bounds getBounds() throws RemoteException {
		return new BoundingSphere(new Point3d(0, 0, 0), ModelData.RADIUS);
	}

}
