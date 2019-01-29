package asw.soa.om;

import java.rmi.RemoteException;
import java.util.HashMap;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import asw.soa.data.EntityMSG;
import asw.soa.data.ModelData;
import asw.soa.main.SimUtil;
import asw.soa.view.Visual2dService;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.d3.CartesianPoint;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * 
 * @author daiwenzhi
 *
 */
public class Submarine extends EventProducer implements EventListenerInterface, Locatable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5001962561864197742L;

	/** TOTAL_ORDERING_COST_EVENT is fired whenever ordering occurs. */
	public static final EventType SUBMARINE_LOCATION_UPDATE_EVENT = new EventType("SUBMARINE_LOCATION_UPDATE_EVENT");

	// private String name;

	private ModelData _vdata = new ModelData();

	/** the simulator. */
	private DEVSSimulatorInterface.TimeDouble simulator = null;

	/** the start time. */
	private double startTime = Double.NaN;

	/** the stop time. */
	private double stopTime = Double.NaN;

	/** the stream -- ugly but works. */
	private static StreamInterface stream = new MersenneTwister();

	public Torpedo _t1 = null;
	public Torpedo _t2 = null;

	private int weaponCounts = 0;

	private HashMap<String, String> LockedTarget = new HashMap<String, String>();

	public Submarine(ModelData data, final DEVSSimulatorInterface.TimeDouble simulator)
			throws RemoteException, SimRuntimeException {
		this._vdata = data;
		this.simulator = simulator;

		ModelData t1Data = new ModelData("Torpedo_1_" + this._vdata.name);
		t1Data.origin = t1Data.destination = this._vdata.origin;
		_t1 = new Torpedo(t1Data, this.simulator);

		ModelData t2Data = new ModelData("Torpedo_2_" + this._vdata.name);
		t2Data.origin = t2Data.destination = this._vdata.origin;
		_t2 = new Torpedo(t2Data, this.simulator);

		// _t1 = new Torpedo(this._vdata.name
		// +"_torpedo1",this._vdata.origin.x,this._vdata.origin.y,simulator);
		// _t2 = new Torpedo(this._vdata.name
		// +"_torpedo2",this._vdata.origin.x,this._vdata.origin.y,simulator);
		weaponCounts = 2;

		this.next();
	}

	/**
	 * next movement.
	 * 
	 * @throws RemoteException     on network failure
	 * @throws SimRuntimeException on simulation failure
	 */
	private void next() throws RemoteException, SimRuntimeException {
		this._vdata.origin = this._vdata.destination;
		// this.destination = new CartesianPoint(-100 + stream.nextInt(0, 200), -100 +
		// stream.nextInt(0, 200), 0);
		this._vdata.destination = new CartesianPoint(this._vdata.destination.x + 1, this._vdata.destination.y + 1, 0);
		this.startTime = this.simulator.getSimulatorTime();
		this.stopTime = this.startTime + Math.abs(new DistNormal(stream, 9, 1.8).draw());
		this.simulator.scheduleEventAbs(this.stopTime, this, this, "next", null);
	}

	@Override
	public DirectedPoint getLocation() throws RemoteException {
		double fraction = (this.simulator.getSimulatorTime() - this.startTime) / (this.stopTime - this.startTime);
		double x = this._vdata.origin.x + (this._vdata.destination.x - this._vdata.origin.x) * fraction;
		double y = this._vdata.origin.y + (this._vdata.destination.y - this._vdata.origin.y) * fraction;
		return new DirectedPoint(x, y, 0, 0.0, 0.0, 0);
	}

	@Override
	public synchronized void notify(EventInterface event) throws RemoteException {
		if (event.getType().equals(Fleet.FLEET_LOCATION_UPDATE_EVENT)) {
			EntityMSG tmp = (EntityMSG) event.getContent();
			// System.out.println(name+" received msg: "+tmp.name+" current
			// location:x="+tmp.x+", y="+tmp.y);

			double dis = SimUtil.calcLength(this._vdata.origin.x, this._vdata.origin.y, tmp.x, tmp.y);
			if (dis < _vdata.detectRange) {
				// 设置通信线数据
				_vdata.lineData.x1 = (int) this._vdata.origin.x;
				_vdata.lineData.y1 = (int) this._vdata.origin.y;
				_vdata.lineData.x2 = (int) tmp.x;
				_vdata.lineData.y2 = (int) tmp.y;
				// 施放鱼雷，对同一目标仅施放一个鱼雷
				if (!LockedTarget.containsKey(tmp.name)) {
					if (weaponCounts == 2) {
						try {
							_t1.setLocation(this._vdata.origin);
							this.simulator.scheduleEventRel(2.0, this, _t1, "fire", new Object[] { tmp });
							weaponCounts--;
							LockedTarget.put(tmp.name, tmp.name);
						} catch (SimRuntimeException e) {
							SimLogger.always().error(e);
						}
					} else if (weaponCounts == 1) {
						try {
							_t2.setLocation(this._vdata.origin);
							this.simulator.scheduleEventRel(2.0, this, _t2, "fire", new Object[] { tmp });
							LockedTarget.put(tmp.name, tmp.name);
							weaponCounts--;
						} catch (SimRuntimeException e) {
							SimLogger.always().error(e);
						}
					} else {
						// 逃逸
					}
				}
				Visual2dService.getInstance().update(_vdata);
			} else {
				_vdata.lineData.x1 = 0;
				_vdata.lineData.y1 = 0;
				_vdata.lineData.x2 = 0;
				_vdata.lineData.y2 = 0;
			}
			Visual2dService.getInstance().update(_vdata);
		}
	}

	@Override
	public Bounds getBounds() throws RemoteException {
		return new BoundingSphere(new Point3d(0, 0, 0), _vdata.RADIUS);
	}

}
