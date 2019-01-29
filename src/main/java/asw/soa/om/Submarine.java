package asw.soa.om;

import java.rmi.RemoteException;
import java.util.HashMap;

import asw.soa.data.EntityMSG;
import asw.soa.data.ModelData;
import asw.soa.main.SimUtil;
import asw.soa.view.Visual2dService;
import nl.tudelft.simulation.dsol.SimRuntimeException;
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

/**
 * 
 * @author daiwenzhi
 *
 */
public class Submarine extends EventProducer implements EventListenerInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5001962561864197742L;

	/** TOTAL_ORDERING_COST_EVENT is fired whenever ordering occurs. */
	public static final EventType SUBMARINE_LOCATION_UPDATE_EVENT = new EventType("SUBMARINE_LOCATION_UPDATE_EVENT");

	// private String name;

	private ModelData _mdata = new ModelData();

	/** the simulator. */
	private DEVSSimulatorInterface.TimeDouble simulator = null;

	/** the stream -- ugly but works. */
	private static StreamInterface stream = new MersenneTwister();

	public Torpedo _t1 = null;
	public Torpedo _t2 = null;

	private int weaponCounts = 0;

	private HashMap<String, String> LockedTarget = new HashMap<String, String>();

	public Submarine(ModelData data, final DEVSSimulatorInterface.TimeDouble simulator)
			throws RemoteException, SimRuntimeException {
		this._mdata = data;
		this.simulator = simulator;

		ModelData t1Data = new ModelData("Torpedo_1_" + this._mdata.name);
		t1Data.origin = t1Data.destination = this._mdata.origin;
		_t1 = new Torpedo(t1Data, this.simulator);

		ModelData t2Data = new ModelData("Torpedo_2_" + this._mdata.name);
		t2Data.origin = t2Data.destination = this._mdata.origin;
		_t2 = new Torpedo(t2Data, this.simulator);

		// _t1 = new Torpedo(this._mdata.name
		// +"_torpedo1",this._mdata.origin.x,this._mdata.origin.y,simulator);
		// _t2 = new Torpedo(this._mdata.name
		// +"_torpedo2",this._mdata.origin.x,this._mdata.origin.y,simulator);
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
		this._mdata.origin = this._mdata.destination;
		// this.destination = new CartesianPoint(-100 + stream.nextInt(0, 200), -100 +
		// stream.nextInt(0, 200), 0);
		this._mdata.destination = new CartesianPoint(this._mdata.destination.x + this._mdata.speed, this._mdata.destination.y + this._mdata.speed, 0);
		this._mdata.startTime = this.simulator.getSimulatorTime();
		this._mdata.stopTime = this._mdata.startTime + Math.abs(new DistNormal(stream, 9, 1.8).draw());
		this.simulator.scheduleEventAbs(this._mdata.stopTime, this, this, "next", null);
	}

	@Override
	public synchronized void notify(EventInterface event) throws RemoteException {
		if (event.getType().equals(Fleet.FLEET_LOCATION_UPDATE_EVENT)) {
			EntityMSG tmp = (EntityMSG) event.getContent();
			// System.out.println(name+" received msg: "+tmp.name+" current
			// location:x="+tmp.x+", y="+tmp.y);

			double dis = SimUtil.calcLength(this._mdata.origin.x, this._mdata.origin.y, tmp.x, tmp.y);
			if (dis < _mdata.detectRange) {
				// 设置通信线数据
				_mdata.lineData.updateData(this._mdata.origin.x, this._mdata.origin.y, tmp.x, tmp.y);
				// 施放鱼雷，对同一目标仅施放一个鱼雷
				if (!LockedTarget.containsKey(tmp.name)) {
					if (weaponCounts == 2) {
						try {
							_t1.setLocation(this._mdata.origin);
							this.simulator.scheduleEventRel(2.0, this, _t1, "fire", new Object[] { tmp });
							weaponCounts--;
							LockedTarget.put(tmp.name, tmp.name);
						} catch (SimRuntimeException e) {
							SimLogger.always().error(e);
						}
					} else if (weaponCounts == 1) {
						try {
							_t2.setLocation(this._mdata.origin);
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
				Visual2dService.getInstance().update(_mdata);
			} else {
				_mdata.lineData.reset();
			}
			Visual2dService.getInstance().update(_mdata);
		}
	}
}
