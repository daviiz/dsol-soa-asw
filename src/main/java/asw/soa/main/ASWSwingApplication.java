package asw.soa.main;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.simtime.SimTimeDouble;
import nl.tudelft.simulation.dsol.simulators.DEVSRealTimeClock;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.swing.animation.D2.AnimationPanel;
import nl.tudelft.simulation.dsol.swing.gui.DSOLApplication;
import nl.tudelft.simulation.dsol.swing.gui.DSOLPanel;
/**
 * 
 * @author daiwenzhi
 *
 */
public class ASWSwingApplication extends DSOLApplication {
	/**
	 * @param title String; the title
	 * @param panel DSOLPanel&lt;Double,Double,SimTimeDouble&gt;; the panel
	 */
	public ASWSwingApplication(final String title, final DSOLPanel<Double, Double, SimTimeDouble> panel) {
		super(title, panel);
	}

	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * @param args String[]; arguments, expected to be empty
	 * @throws SimRuntimeException on error
	 * @throws RemoteException     on error
	 * @throws NamingException     on error
	 */
	public static void main(final String[] args) throws SimRuntimeException, RemoteException, NamingException {
		DEVSRealTimeClock.TimeDouble simulator = new DEVSRealTimeClock.TimeDouble(0.1);
		ASWModel model = new ASWModel(simulator);
		Replication.TimeDouble<DEVSSimulatorInterface.TimeDouble> replication = Replication.TimeDouble.create("rep1",
				0.0, 0.0, 1000000.0, model);
		DSOLPanel<Double, Double, SimTimeDouble> panel = new DSOLPanel<Double, Double, SimTimeDouble>(model, simulator);
		panel.getTabbedPane().add("animation",
				new AnimationPanel(new Rectangle2D.Double(-100, -100, 400, 400), new Dimension(200, 200), simulator));
		panel.getTabbedPane().setSelectedIndex(1);
		simulator.initialize(replication, ReplicationMode.TERMINATING);
		new ASWSwingApplication("Anti-Submarine-Warfare", panel);
	}

}
