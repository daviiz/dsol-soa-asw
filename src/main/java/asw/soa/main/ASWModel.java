package asw.soa.main;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import asw.soa.data.ModelData;
import asw.soa.om.Decoy;
import asw.soa.om.Fleet;
import asw.soa.om.Submarine;
import asw.soa.om.Torpedo;
import asw.soa.view.Visual2dService;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.dsol.model.AbstractDSOLModel;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.language.d3.CartesianPoint;

/**
 * 
 * @author daiwenzhi
 *
 */
public class ASWModel extends AbstractDSOLModel.TimeDouble<DEVSSimulatorInterface.TimeDouble> {
	/** The default serial version UID for serializable classes. */
	private static final long serialVersionUID = 1L;

	private Fleet f1;
	private Fleet f2;
	private Submarine s1;

	/**
	 * constructs a new BallModel.
	 * 
	 * @param simulator the simulator
	 */
	public ASWModel(final DEVSSimulatorInterface.TimeDouble simulator) {
		super(simulator);
	}

	/** {@inheritDoc} */
	@Override
	public void constructModel() throws SimRuntimeException {
		try {
			ModelData f1Data = new ModelData("Fleet_1");
			f1Data.origin = f1Data.destination = new CartesianPoint(-200, -50, 0);
			f1 = new Fleet(f1Data, this.simulator);

			ModelData f2Data = new ModelData("Fleet_2");
			f2Data.origin = f2Data.destination = new CartesianPoint(-250, 0, 0);
			f2 = new Fleet(f2Data, this.simulator);

			ModelData s1Data = new ModelData("Sub_1");
			s1Data.origin = s1Data.destination = new CartesianPoint(200, 100, 0);
			s1 = new Submarine(s1Data, this.simulator);

			// 视图组件注册：
			try {
				Visual2dService.getInstance().register(f1Data.name,simulator,f1Data);
				Visual2dService.getInstance().register(f2Data.name, simulator, f2Data);
				Visual2dService.getInstance().register(s1Data.name, simulator,s1Data);
			} catch (NamingException e) {
				SimLogger.always().error(e);
			}
			// 模型发布显示组件消息：
			// renderService.render();

			// 发布方 .addListener 订阅方
			// 战舰1 消息发布
			f1.addListener(f1._decoy1, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			f1.addListener(f1._decoy2, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			f1.addListener(f2, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			f1.addListener(f2._decoy1, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			f1.addListener(f2._decoy2, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			f1.addListener(s1, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			f1.addListener(s1._t1, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			f1.addListener(s1._t2, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			// 战舰2 消息发布
			f2.addListener(f2._decoy1, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			f2.addListener(f2._decoy2, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			f2.addListener(f1, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			f2.addListener(f1._decoy1, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			f2.addListener(f1._decoy2, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			f2.addListener(s1, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			f2.addListener(s1._t1, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			f2.addListener(s1._t2, Fleet.FLEET_LOCATION_UPDATE_EVENT);
			// 潜艇1 消息发布
			s1.addListener(f2, Submarine.SUBMARINE_LOCATION_UPDATE_EVENT);
			s1.addListener(f2._decoy1, Submarine.SUBMARINE_LOCATION_UPDATE_EVENT);
			s1.addListener(f2._decoy2, Submarine.SUBMARINE_LOCATION_UPDATE_EVENT);
			s1.addListener(f1, Submarine.SUBMARINE_LOCATION_UPDATE_EVENT);
			s1.addListener(f1._decoy1, Submarine.SUBMARINE_LOCATION_UPDATE_EVENT);
			s1.addListener(f1._decoy2, Submarine.SUBMARINE_LOCATION_UPDATE_EVENT);
			s1.addListener(s1._t1, Submarine.SUBMARINE_LOCATION_UPDATE_EVENT);
			s1.addListener(s1._t2, Submarine.SUBMARINE_LOCATION_UPDATE_EVENT);
			// 潜艇附带鱼雷 消息发布
			s1._t1.addListener(f1, Torpedo.TORPEDO_LOCATION_MSG);
			s1._t1.addListener(f2, Torpedo.TORPEDO_LOCATION_MSG);
			s1._t1.addListener(f1._decoy1, Torpedo.TORPEDO_LOCATION_MSG);
			s1._t1.addListener(f1._decoy2, Torpedo.TORPEDO_LOCATION_MSG);
			s1._t1.addListener(f2._decoy1, Torpedo.TORPEDO_LOCATION_MSG);
			s1._t1.addListener(f2._decoy2, Torpedo.TORPEDO_LOCATION_MSG);

			s1._t2.addListener(f1, Torpedo.TORPEDO_LOCATION_MSG);
			s1._t2.addListener(f2, Torpedo.TORPEDO_LOCATION_MSG);
			s1._t2.addListener(f1._decoy1, Torpedo.TORPEDO_LOCATION_MSG);
			s1._t2.addListener(f1._decoy2, Torpedo.TORPEDO_LOCATION_MSG);
			s1._t2.addListener(f2._decoy1, Torpedo.TORPEDO_LOCATION_MSG);
			s1._t2.addListener(f2._decoy2, Torpedo.TORPEDO_LOCATION_MSG);
			// 战舰1附带鱼雷诱饵 消息发布
			f1._decoy1.addListener(s1._t1, Decoy.DECOY_LOCATION_MSG);
			f1._decoy1.addListener(s1._t2, Decoy.DECOY_LOCATION_MSG);
			f1._decoy2.addListener(s1._t1, Decoy.DECOY_LOCATION_MSG);
			f1._decoy2.addListener(s1._t2, Decoy.DECOY_LOCATION_MSG);
			// 战舰1附带鱼雷诱饵 消息发布
			f2._decoy1.addListener(s1._t1, Decoy.DECOY_LOCATION_MSG);
			f2._decoy1.addListener(s1._t2, Decoy.DECOY_LOCATION_MSG);
			f2._decoy2.addListener(s1._t1, Decoy.DECOY_LOCATION_MSG);
			f2._decoy2.addListener(s1._t2, Decoy.DECOY_LOCATION_MSG);

		} catch (RemoteException exception) {
			SimLogger.always().error(exception);
		}
	}

}
