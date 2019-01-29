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

	public int x1;
	public int y1;
	public int x2;
	public int y2;

	public LineData(int _x1, int _y1, int _x2, int _y2) {
		this.x1 = _x1;
		this.y1 = _y1;

		this.x2 = _x2;
		this.y2 = _y2;
	}

}
