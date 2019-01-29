package asw.soa.main;

import nl.tudelft.simulation.language.d3.CartesianPoint;

/**
 * 
 * @author daiwenzhi
 *
 */
public class SimUtil {

	/**
	 * 打击距离 距离小于此值是视为 命中目标
	 */
	public static int hit_distance = 10;

	/**
	 * 
	 * @param src_x
	 * @param src_y
	 * @param des_x
	 * @param des_y
	 * @return
	 */
	public static double calcLength(double src_x, double src_y, double des_x, double des_y) {
		double tmp = 0;
		tmp = Math.sqrt((des_y - src_y) * (des_y - src_y) + (des_x - src_x) * (des_x - src_x));
		return tmp;
	}

	/**
	 * 
	 * @param src_x
	 * @param src_y
	 * @param des_x
	 * @param des_y
	 * @param _speed
	 * @param isFllow
	 * @return
	 */
	public static CartesianPoint nextPoint(double src_x, double src_y, double des_x, double des_y, double _speed,
			boolean isFllow) {
		double c = calcLength(src_x, src_y, des_x, des_y);
		double b = calcLength(src_x, src_y, src_x, des_y);
		double a = calcLength(des_x, des_y, src_x, des_y);
		double _delta_x = a / c * _speed;
		double _delta_y = b / c * _speed;

		if (des_x < src_x) {
			_delta_x = _delta_x * (-1);
		}
		if (des_y < src_y) {
			_delta_y = _delta_y * (-1);
		}
		if (isFllow)
			return new CartesianPoint(src_x + _delta_x, src_y + _delta_y, 0);
		else
			return new CartesianPoint(src_x - _delta_x, src_y - _delta_y, 0);
	}
}
