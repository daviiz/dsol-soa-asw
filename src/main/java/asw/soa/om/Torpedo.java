package asw.soa.om;

import java.awt.Color;
import java.rmi.RemoteException;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.naming.NamingException;
import javax.vecmath.Point3d;

import asw.soa.data.EntityMSG;
import asw.soa.data.LineData;
import asw.soa.data.ModelData;
import asw.soa.main.SimUtil;
import asw.soa.view.Visual2dRender;
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
public class Torpedo extends EventProducer implements EventListenerInterface,Locatable {

	private static final long serialVersionUID = -8295279255703776031L;

	public static final EventType TORPEDO_LOCATION_MSG = new EventType("TORPEDO_LOCATION_MSG");

	public boolean isFired = false;

	/** the start time. */
	private double startTime = Double.NaN;

	/** the stop time. */
	private double stopTime = Double.NaN;

	/** the stream -- ugly but works. */
	private static StreamInterface stream = new MersenneTwister();

	/** the simulator. */
	private DEVSSimulatorInterface.TimeDouble simulator = null;

	private EntityMSG lastTarget = null;

	private int next_x = -2 + stream.nextInt(0, 7);
	private int next_y = -3 + stream.nextInt(0, 7);

	private ModelData _mdata = new ModelData();
	
	private double lastDistance = 250;

	public Torpedo(String name, double x, double y, final DEVSSimulatorInterface.TimeDouble simulator) {
		this._mdata.name = name;
		this._mdata.detectRange = 100;
		_mdata.origin = new CartesianPoint(x, y, 0);
		_mdata.destination = new CartesianPoint(x, y, 0);
		this.simulator = simulator;
	}
	
	public Torpedo(ModelData data ,final DEVSSimulatorInterface.TimeDouble simulator) {
		this._mdata = data;
		this.simulator = simulator;
	}

	@Override
	public synchronized void notify(EventInterface event) throws RemoteException {

		if (isFired) {

			if (event.getType().equals(Fleet.FLEET_LOCATION_UPDATE_EVENT)
					|| event.getType().equals(Decoy.DECOY_LOCATION_MSG)) {
				EntityMSG tmp = (EntityMSG) event.getContent();
				double tmpL = SimUtil.calcLength(this._mdata.origin.x, this._mdata.origin.y, tmp.x, tmp.y);

				if (tmpL < _mdata.detectRange) {
					//在探测范围内 并且是生存状态的实体才显示通信线
					if(tmp.status == true) {
						_mdata.lineData.x1 = (int) this._mdata.origin.x;
						_mdata.lineData.y1 = (int) this._mdata.origin.y;
						_mdata.lineData.x2 = (int) tmp.x;
						_mdata.lineData.y2 = (int) tmp.y;
					}
					//在探测范围内 找到更近的 设置其为目标
					if (tmpL < lastDistance) {
						lastTarget = new EntityMSG(tmp);
						lastDistance = tmpL;
					}
					//如果自己的目标已经死亡 在探测范围内寻找目标 找到就重新设置目标
					if(this.lastTarget.status == false) {
						lastDistance = tmpL;
						lastTarget = new EntityMSG(tmp);
					}
				} else {
					_mdata.lineData.x1 = 0;
					_mdata.lineData.y1 = 0;
					_mdata.lineData.x2 = 0;
					_mdata.lineData.y2 = 0;
				}
			}
		}
	}

	/**
	 * 鱼雷施放
	 * 
	 * @param object
	 * @throws RemoteException
	 * @throws NamingException
	 * @throws SimRuntimeException
	 */
	public synchronized void fire(final EntityMSG object) throws RemoteException, NamingException, SimRuntimeException {
		isFired = true;
		lastTarget = object;
		//视图组件注册：
    	try {
    		Visual2dService.getInstance().addVisualComponent(this._mdata.name,new Visual2dRender(this, simulator, this._mdata));
		} catch (NamingException e) {
			SimLogger.always().error(e);
		}
		next();
	}

	/**
	 * next movement.
	 * 
	 * @throws RemoteException     on network failure
	 * @throws SimRuntimeException on simulation failure
	 */
	private synchronized void next() throws RemoteException, SimRuntimeException {
		this._mdata.origin = this._mdata.destination;
		
		if (lastTarget == null || lastTarget.status == false) {
			//this._mdata.destination = new CartesianPoint(this._mdata.destination.x + next_x, this._mdata.destination.y + next_y, 0);
		} else {
			this._mdata.destination = SimUtil.nextPoint(this._mdata.origin.x, this._mdata.origin.y, lastTarget.x, lastTarget.y, 4.0, true);
		}
		this.startTime = this.simulator.getSimulatorTime();
		//this.stopTime = this.startTime + Math.abs(new DistNormal(stream, 9, 1.8).draw());
		this.stopTime = this.startTime + Math.abs(new DistNormal(stream, 9, 1.8).draw());
		this.simulator.scheduleEventAbs(this.stopTime, this, this, "next", null);

		super.fireTimedEvent(TORPEDO_LOCATION_MSG, new EntityMSG(_mdata.name, _mdata.belong, _mdata.status, this._mdata.origin.x, this._mdata.origin.y),
				this.simulator.getSimTime().plus(2.0));

	}

	@Override
	public DirectedPoint getLocation() throws RemoteException {
		double fraction = (this.simulator.getSimulatorTime() - this.startTime) / (this.stopTime - this.startTime);
		double x = this._mdata.origin.x + (this._mdata.destination.x - this._mdata.origin.x) * fraction;
		double y = this._mdata.origin.y + (this._mdata.destination.y - this._mdata.origin.y) * fraction;
		return new DirectedPoint(x, y, 0, 0.0, 0.0, this._mdata.theta);
	}

	public void setLocation(CartesianPoint _origin) {
		this._mdata.origin = _origin;
		this._mdata.destination = _origin;
	}
	@Override
	public Bounds getBounds() throws RemoteException {
		return new BoundingSphere(new Point3d(0, 0, 0), _mdata.RADIUS);
	}

}
