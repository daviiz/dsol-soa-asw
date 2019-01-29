package asw.soa.om;

import java.awt.Color;
import java.rmi.RemoteException;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.naming.NamingException;
import javax.vecmath.Point3d;

import asw.soa.data.EntityMSG;
import asw.soa.data.ModelData;
import asw.soa.main.SimUtil;
import asw.soa.view.Visual2dRender;
import asw.soa.view.Visual2dService;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface.TimeDouble;
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
 */
public class Fleet extends EventProducer implements EventListenerInterface,Locatable {

	private static final long serialVersionUID = 5337683693470946049L;

	public static final EventType FLEET_LOCATION_UPDATE_EVENT = new EventType("FLEET_LOCATION_UPDATE_EVENT");

	/** the simulator. */
	private DEVSSimulatorInterface.TimeDouble simulator = null;

	/** the start time. */
	private double startTime = Double.NaN;

	/** the stop time. */
	private double stopTime = Double.NaN;

	/** the stream -- ugly but works. */
	private static StreamInterface stream = new MersenneTwister();

	public Decoy _decoy1;

	public Decoy _decoy2;

	public int decoyCouts = 2;

	/**
	 * 探测到的威胁实体信息
	 */
	public EntityMSG lastThreat = null;

	private volatile boolean isDead = false;

	/**
	 * asw策略设置：1：施放鱼雷并逃逸；2:逃逸
	 */
	private int aswPolicy = 1; 
	
	private ModelData _vdata = new ModelData();
	
//	public Fleet(String name, double x, double y, final DEVSSimulatorInterface.TimeDouble simulator)
//			throws RemoteException, SimRuntimeException {
//		
//		_vdata.origin = new CartesianPoint(x, y, 0);
//		_vdata.destination = new CartesianPoint(x, y, 0);
//		this.simulator = simulator;
//		this._vdata.name = name;
//		_vdata.detectRange = 200;
//		_vdata.belong = 1;
//		
//
//		_decoy1 = new Decoy(name + "_decoy1", x, y, simulator);
//		_decoy2 = new Decoy(name + "_decoy2", x, y, simulator);
//
//		this.next();
//	}

	public Fleet(ModelData _data, final DEVSSimulatorInterface.TimeDouble  simulator) throws RemoteException, SimRuntimeException {
		this.simulator = simulator;
		this._vdata = _data;
		
		ModelData d1Data = new ModelData("Decoy_1_"+this._vdata.name);
		d1Data.origin = d1Data.destination = this._vdata.origin;
		_decoy1 = new Decoy(d1Data, simulator);
		ModelData d2Data = new ModelData("Decoy_2_"+this._vdata.name);
		d2Data.origin = d2Data.destination = this._vdata.origin;
		_decoy2 = new Decoy(d2Data, simulator);
		
		this.next();
	}

	/**
	 * next movement.
	 * 
	 * @throws RemoteException     on network failure
	 * @throws SimRuntimeException on simulation failure
	 */
	private synchronized void next() throws RemoteException, SimRuntimeException {
		
		this._vdata.origin = this._vdata.destination;
		
		if (isDead) {
			this._vdata.destination = new CartesianPoint(this._vdata.destination.x, this._vdata.destination.y, 0);
		} else if (lastThreat == null) {
			this._vdata.destination = new CartesianPoint(this._vdata.destination.x + 2, this._vdata.destination.y + 2, 0);
		} else {
			this._vdata.destination = SimUtil.nextPoint(this._vdata.origin.x, this._vdata.origin.y, lastThreat.x, lastThreat.y, 2.0, false);
		}

		this.startTime = this.simulator.getSimulatorTime();
		this.stopTime = this.startTime + Math.abs(new DistNormal(stream, 9, 1.8).draw());
		
		//System.out.println(Math.abs(new DistNormal(stream, 9, 1.8).draw()));

		this.simulator.scheduleEventAbs(this.stopTime, this, this, "next", null);
		super.fireTimedEvent(FLEET_LOCATION_UPDATE_EVENT,
				new EntityMSG(_vdata.name, _vdata.belong, _vdata.status, this._vdata.origin.x, this._vdata.origin.y),
				this.simulator.getSimTime().plus(2.0));

	}

	@Override
	public DirectedPoint getLocation() throws RemoteException {
		double fraction = (this.simulator.getSimulatorTime() - this.startTime) / (this.stopTime - this.startTime);
		double x = this._vdata.origin.x + (this._vdata.destination.x - this._vdata.origin.x) * fraction;
		double y = this._vdata.origin.y + (this._vdata.destination.y - this._vdata.origin.y) * fraction;
		return new DirectedPoint(x, y, 0, 0.0, 0.0, this._vdata.theta);
	}

	@Override
	public synchronized void notify(final EventInterface event) throws RemoteException {
		if (!isDead) {
			if (event.getType().equals(FLEET_LOCATION_UPDATE_EVENT)) {
				EntityMSG tmp = (EntityMSG) event.getContent();
				//System.out.println(name + " received msg: " + tmp.name + " current location:x=" + tmp.x + ", y=" + tmp.y);

				// fireTimedEvent(Fleet.FLEET_LOCATION_UPDATE_EVENT, (LOC)event.getContent(),
				// this.simulator.getSimulatorTime());

			} else if (event.getType().equals(Torpedo.TORPEDO_LOCATION_MSG)) {
				EntityMSG tmp = (EntityMSG) event.getContent();
				//System.out.println(name + " received msg: " + tmp.name + " current location:x=" + tmp.x + ", y=" + tmp.y);
				double dis = SimUtil.calcLength(this._vdata.origin.x, this._vdata.origin.y, tmp.x, tmp.y);

				if (dis < _vdata.detectRange) {
					if (aswPolicy == 1) {
						if (decoyCouts == 2) {
							try {
								_decoy1.setLocation(this._vdata.origin);
								this.simulator.scheduleEventRel(20.0, this, _decoy1, "fire", new Object[] { tmp });
								decoyCouts--;
								
							} catch (SimRuntimeException e) {
								SimLogger.always().error(e);
							}
						} else if (decoyCouts == 1) {
							try {
								_decoy2.setLocation(this._vdata.origin);
								this.simulator.scheduleEventRel(120.0, this, _decoy2, "fire", new Object[] { tmp });
								decoyCouts--;
							} catch (SimRuntimeException e) {
								SimLogger.always().error(e);
							}
						}
					}
					lastThreat = tmp;
					if (dis < SimUtil.hit_distance) {
						//visualComponent.setColor(Color.BLACK);
						_vdata.color = Color.BLACK;
						Visual2dService.getInstance().update(this._vdata);
						isDead = true;
						_vdata.status = false;
					}
				}

			}
		}
	}
	@Override
	public Bounds getBounds() throws RemoteException {
		return new BoundingSphere(new Point3d(0, 0, 0), _vdata.RADIUS);
	}

}
