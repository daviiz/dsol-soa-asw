package asw.soa.om;

import java.awt.Color;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import asw.soa.data.EntityMSG;
import asw.soa.data.ModelData;
import asw.soa.main.SimUtil;
import asw.soa.view.Visual2dService;
import nl.tudelft.simulation.dsol.SimRuntimeException;
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
 * 鱼雷诱饵模型
 * 
 * @author daiwenzhi
 *
 */
public class Decoy extends EventProducer implements EventListenerInterface{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1020956635649196808L;

	public static final EventType DECOY_LOCATION_MSG = new EventType("DECOY_LOCATION_MSG");

	private boolean isFired = false;

	private EntityMSG lastThreat = null;

	/** the stream -- ugly but works. */
	private static StreamInterface stream = new MersenneTwister();

	/** the simulator. */
	private DEVSSimulatorInterface.TimeDouble simulator = null;

	// private volatile boolean isDead = false;

	private ModelData _mdata = new ModelData();;

	public Decoy(ModelData data, final DEVSSimulatorInterface.TimeDouble simulator) {
		_mdata = data;
		this.simulator = simulator;

	}

	public Decoy(String string, double x, double y, final DEVSSimulatorInterface.TimeDouble simulator) {
		_mdata.name = string;
		_mdata.origin = _mdata.destination = new CartesianPoint(x, y, 0);
		this.simulator = simulator;
	}

	@Override
	public void notify(EventInterface event) throws RemoteException {
		if (isFired && (_mdata.status)) {
			if (event.getType().equals(Torpedo.TORPEDO_LOCATION_MSG)) {
				EntityMSG tmp = (EntityMSG) event.getContent();
				// System.out.println(name+" received msg: "+tmp.name+" current
				// location:x="+tmp.x+", y="+tmp.y);
				double dis = SimUtil.calcLength(this._mdata.origin.x, this._mdata.origin.y, tmp.x, tmp.y);
				if (dis < this._mdata.detectRange) {
					lastThreat = tmp;
					if (dis < SimUtil.hit_distance) {
						_mdata.color = Color.BLACK;
						// isDead = true;
						_mdata.status = false;
						Visual2dService.getInstance().update(_mdata);
					}
				}

			}
		}

	}

	/**
	 * 鱼雷诱饵施放
	 * 
	 * @param object
	 * @throws RemoteException
	 * @throws NamingException
	 * @throws SimRuntimeException
	 */
	public synchronized void fire(final EntityMSG object) throws RemoteException, NamingException, SimRuntimeException {
		isFired = true;
		lastThreat = object;
		// 视图组件注册：
		Visual2dService.getInstance().register(this._mdata.name, simulator,this._mdata);
		next();
	}

	/**
	 * next movement.
	 * 
	 * @throws RemoteException     on network failure
	 * @throws SimRuntimeException on simulation failure
	 * @throws NamingException
	 */
	private synchronized void next() throws RemoteException, SimRuntimeException, NamingException {

		this._mdata.origin = this._mdata.destination;
		// this.destination = new CartesianPoint(-100 + stream.nextInt(0, 200), -100 +
		// stream.nextInt(0, 200), 0);
		// this.destination = new CartesianPoint(this.destination.x+4,
		// this.destination.y+4, 0);
		if (!_mdata.status) {
			this._mdata.destination = new CartesianPoint(this._mdata.destination.x, this._mdata.destination.y, 0);
		} else if (lastThreat == null) {
			// this.destination = new CartesianPoint(this.destination.x, this.destination.y,
			// 0);
		} else {
			this._mdata.destination = SimUtil.nextPoint(this._mdata.origin.x, this._mdata.origin.y, lastThreat.x,
					lastThreat.y, this._mdata.speed, false);
		}
		this._mdata.startTime = this.simulator.getSimulatorTime();
		this._mdata.stopTime = this._mdata.startTime + Math.abs(new DistNormal(stream, 9, 1.8).draw());
		this.simulator.scheduleEventAbs(this._mdata.stopTime, this, this, "next", null);

		super.fireTimedEvent(DECOY_LOCATION_MSG,
				new EntityMSG(_mdata.name, _mdata.belong, _mdata.status, this._mdata.origin.x, this._mdata.origin.y),
				this.simulator.getSimTime().plus(2.0));

	}

	public void setLocation(CartesianPoint _origin) {
		this._mdata.origin = _origin;
		this._mdata.destination = _origin;
	}
}
