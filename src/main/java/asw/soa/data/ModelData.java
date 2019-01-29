package asw.soa.data;

import java.awt.Color;

import nl.tudelft.simulation.language.d3.CartesianPoint;

/**
 * 
 * @author daiwenzhi
 *
 */
public class ModelData implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3177965369711352742L;

	/** the origin. */
	public CartesianPoint origin = new CartesianPoint(0, 0, 0);

	/** the destination. */
	public CartesianPoint destination = new CartesianPoint(0, 0, 0);
	
	/** the start time. */
	public double startTime = Double.NaN;

	/** the stop time. */
	public double stopTime = Double.NaN;

	/** the radius of the ball. */
	public static final double RADIUS = 5.0;

	/** the angle of the ball. */
	public double theta = 0.0;

	/** the name of the model. */
	public String name = "default";

	public int belong = 1;

	public boolean status = true;
	
	public int speed = 0;

	/**
	 * 通信数据链
	 */
	public int x1 = 0;
	public int y1 = 0;
	public int x2 = 0;
	public int y2 = 0;

	public Color color = Color.RED;

	public int detectRange = 100;

	public LineData lineData = new LineData(0, 0, 0, 0);

	private Integer id;

	public ModelData(Color color, int detectRange, int x1, int y1, int x2, int y2) {
		this.color = color;
		this.detectRange = detectRange;
		this.x1 = x1;
		this.y1 = y1;

		this.x2 = x2;
		this.y2 = y2;
	}

	public ModelData() {

	}

	public ModelData(String name) {
		this.name = name;
		if (this.name.startsWith("Fleet")) {
			this.color = Color.RED;
			this.detectRange = 200;
			this.belong = 1;
			this.speed = 2;
		} else if (this.name.startsWith("Sub")) {
			this.color = Color.BLUE;
			this.detectRange = 400;
			this.belong = -1;
			this.speed = 1;

		} else if (this.name.startsWith("Decoy")) {
			this.color = Color.PINK;
			this.detectRange = 100;
			this.belong = 1;
			this.speed = 2;

		} else if (this.name.startsWith("Torpedo")) {
			this.color = Color.CYAN;
			this.detectRange = 150;
			this.belong = -1;
			this.speed = 4;
		}
	}

	public String toString() {
		return this.name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

}
