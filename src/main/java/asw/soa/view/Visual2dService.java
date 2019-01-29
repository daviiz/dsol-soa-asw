package asw.soa.view;

import java.rmi.RemoteException;
import java.util.HashMap;

import javax.naming.NamingException;

import asw.soa.data.ModelData;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface.TimeDouble;

public class Visual2dService {

	private static volatile Visual2dService instance;

	private Visual2dService() {
	}

	public static Visual2dService getInstance() {
		if (instance == null) {
			synchronized (Visual2dService.class) {
				if (instance == null) {
					instance = new Visual2dService();
				}
			}
		}
		return instance;
	}

	private HashMap<String, Visual2dRender> components = new HashMap<String, Visual2dRender>();
	

	public void addVisualComponent(String name, Visual2dRender c) {
		if (c == null || name == null || name.equals(""))
			return;
		components.put(name, c);
	}

	public synchronized void update(ModelData data) {
		for (HashMap.Entry<String, Visual2dRender> entry : components.entrySet()) {
			if (entry.getKey().equals(data.name)) {
				Visual2dRender v = (Visual2dRender) entry.getValue();
				v.update(data);
			}
		}
	}
	/**
	 * 注册2D视图组件
	 * @param name  --唯一标识，不允许重复
	 * @param loc   --Locatable接口的实现，用于获取位置信息，更新帧动画
	 * @param simulator --仿真器引用，用于调度
	 * @param _data     --模型数据
	 * @throws RemoteException
	 * @throws NamingException
	 */
	public void register(String name, Locatable loc, TimeDouble simulator, ModelData _data) throws RemoteException, NamingException {
		if (loc == null || name == null || name.equals("") ||simulator == null || _data == null)
			return;
		
		//查重
		for (HashMap.Entry<String, Visual2dRender> entry : components.entrySet()) {
			if (entry.getKey().equals(name)) {
				SimLogger.always().error(new Exception("no duplicate model name !"));
				return;
			}
		}
		components.put(name, new Visual2dRender(loc,simulator,_data));
	}
	/**
	 * 注册2D视图组件
	 * @param name  --唯一标识，不允许重复
	 * @param simulator --仿真器引用，用于调度
	 * @param _data     --模型数据
	 * @throws RemoteException
	 * @throws NamingException
	 */
	public void register(String name,TimeDouble simulator, ModelData _data) throws RemoteException, NamingException {
		if (name == null || name.equals("") ||simulator == null || _data == null)
			return;
		
		//查重
		for (HashMap.Entry<String, Visual2dRender> entry : components.entrySet()) {
			if (entry.getKey().equals(name)) {
				SimLogger.always().error(new Exception("Duplicate model name denyed!"));
				return;
			}
		}
		components.put(name, new Visual2dRender(new VisualComponent(_data, simulator),simulator,_data));
	}

}
