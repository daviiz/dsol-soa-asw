package asw.soa.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import asw.soa.data.LineData;
import asw.soa.data.ModelData;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * 
 * @author daiwenzhi
 *
 */
class Visual2dRender extends Renderable2D<Locatable> {

	/**
	 * 用于渲染所用的所有模型数据
	 */
	private ModelData data = new ModelData();

	public ModelData getData() {
		return data;
	}

	public void setData(ModelData data) {
		this.data = data;
	}

	private Graphics2D graphics;
	private ImageObserver observer;

	/**
	 * constructs a new BallAnimation.
	 * 
	 * @param source    Locatable; the source
	 * @param simulator SimulatorInterface.TimeDouble; the simulator
	 * @throws NamingException on registration error
	 * @throws RemoteException on remote animation error
	 */
	public Visual2dRender(final Locatable source, final SimulatorInterface.TimeDouble simulator, Color _color,
			int _detectRange, LineData _target) throws RemoteException, NamingException {
		super(source, simulator);
		this.data.color = _color;
		this.data.detectRange = _detectRange;
		this.data.lineData = _target;
	}

	public Visual2dRender(final Locatable source, final SimulatorInterface.TimeDouble simulator, ModelData _data)
			throws RemoteException, NamingException {
		super(source, simulator);
		this.data = _data;
		this.data.color = data.color;
		this.data.detectRange = data.detectRange;
		this.data.lineData = new LineData(data.x1, data.y1, data.x2, data.y2);
	}

	/** {@inheritDoc} */
	@Override
	public void paint(final Graphics2D graphics, final ImageObserver observer) {
		graphics.setColor(this.data.color);
		graphics.fillOval(-(int) data.RADIUS, -(int) data.RADIUS, (int) (data.RADIUS * 2.0), (int) (data.RADIUS * 2.0));
		Font f = new Font("Consolas", Font.BOLD, 6);
		graphics.setFont(f);
		graphics.setColor(Color.GRAY);
		graphics.drawString(this.data.name, (float) (data.RADIUS * -1.0), (float) (data.RADIUS * 1.0));
		if (data.detectRange > 0) {
			graphics.setColor(this.data.color);
			graphics.drawOval(-data.detectRange, -data.detectRange, data.detectRange * 2, data.detectRange * 2);
		}

		if (this.data.lineData != null) {
			graphics.setColor(this.data.color);
			int x = 0, y = 0;
			x = this.data.lineData.x1 > this.data.lineData.x2
					? (-1) * (Math.abs(this.data.lineData.x1 - this.data.lineData.x2))
					: (Math.abs(this.data.lineData.x1 - this.data.lineData.x2));
			y = this.data.lineData.y1 > this.data.lineData.y2
					? (Math.abs(this.data.lineData.y1 - this.data.lineData.y2))
					: (-1) * (Math.abs(this.data.lineData.y1 - this.data.lineData.y2));
			;
			graphics.drawLine(0, 0, x, y);
		}

	}

	public void paint() {
		if (graphics != null && observer != null)
			this.paint(graphics, observer);
	}

	/**
	 * @return Returns the color.
	 */
	public Color getColor() {
		return this.data.color;
	}

	/**
	 * @param color Color; The color to set.
	 */
	public void setColor(final Color color) {
		this.data.color = color;
	}

	public void update(ModelData data2) {
		this.data = data2;
	}
}
