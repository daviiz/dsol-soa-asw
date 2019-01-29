package asw.soa.data;

/**
 * 用于绘制通信线的数据传递
 * 
 * @author daiwenzhi
 *
 */
public class LineData implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5171097272647081846L;

	public int x1 = 0;
	public int y1 = 0;
	public int x2 = 0;
	public int y2 = 0;

	public LineData(int _x1, int _y1, int _x2, int _y2) {
		this.x1 = _x1;
		this.y1 = _y1;
		this.x2 = _x2;
		this.y2 = _y2;
	}
	
	public void updateData(int _x1, int _y1, int _x2, int _y2) {
		this.x1 = _x1;
		this.y1 = _y1;
		this.x2 = _x2;
		this.y2 = _y2;
	}
	public void reset() {
		this.x1 = 0;
		this.y1 = 0;
		this.x2 = 0;
		this.y2 = 0;
	}

	public void updateData(double x, double y, double x3, double y3) {
		// TODO Auto-generated method stub
		this.x1 = (int)x;
		this.y1 = (int)y;
		this.x2 = (int)x3;
		this.y2 = (int)y3;
	}

}
