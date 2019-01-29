package asw.soa.view;

import java.util.HashMap;

import asw.soa.data.ModelData;

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

}
