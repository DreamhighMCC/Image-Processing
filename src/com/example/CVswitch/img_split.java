package com.example.CVswitch;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;

public class img_split {
	private static final String TAG = "Switch Split";

	public int hist_white[];
	public int hist_red[];
	public int hist_yellow[];

	private int black_hmin = 0;
	private int black_hmax = 180;
	private int black_smin = 0;
	private int black_smax = 255;
	private int black_vmin = 0;
	private int black_vmax = 46;

	private int grey_hmin = 0;
	private int grey_hmax = 180;
	private int grey_smin = 0;
	private int grey_smax = 43;
	private int grey_vmin = 46;
	private int grey_vmax = 220;

	private int white_hmin = 0;
	private int white_hmax = 180;
	private int white_smin = 0;
	private int white_smax = 30;
	private int white_vmin = 221;
	private int white_vmax = 255;

	private int red_hmin1 = 0;
	private int red_hmax1 = 10;
	private int red_hmin2 = 156;
	private int red_hmax2 = 180;
	private int red_smin = 43;
	private int red_smax = 255;
	private int red_vmin = 46;
	private int red_vmax = 255;

	private int yellow_hmin = 11;
	private int yellow_hmax = 34;
	private int yellow_smin = 43;
	private int yellow_smax = 255;
	private int yellow_vmin = 46;
	private int yellow_vmax = 255;
	
	

	/*
	 * img_src:输入RGB原图像 width：原图片的宽 height：原图片的高 split_info:图片分割的信息保存 split_info
	 * = new int[50][5];// 所有分割图片的位置信息[开关行数,x,y,height,width]
	 */
	public void split_01(Mat img_src, int width, int height, int split_info[][]) {
		// split_info = new int[50][5];// 所有分割图片的位置信息[开关行数,x,y,height,width]
		int horizon_mark = 1;
		int num = 0;
		width = img_src.width();
		height = img_src.height();
		Log.i("WH", "width" + width + ";height" + height);

		Mat img_gray = new Mat(height, width, CvType.CV_8UC1);
		List<Mat> images = new ArrayList<Mat>();
		Core.split(img_src, images);// RGB通道分离
		// 将图片的蓝色调视图作为图片的灰度图
		img_gray = images.get(2);// 获得第二通道分量

		// get the canny edge
		Mat canny_dst = new Mat();
		Imgproc.Canny(img_gray, canny_dst, 50, 150);

		img_gray.convertTo(img_gray, CvType.CV_32SC1);
		canny_dst.convertTo(canny_dst, CvType.CV_32SC1);

		// //canny算子边缘图水平投影
		// Mat onemat=new Mat(1,width,CvType.CV_32SC1);
		// onemat.ones(1, width, CvType.CV_32SC1);
		// Mat resultmat=new Mat(1, height, CvType.CV_64FC1);
		// for (int i = 0; i < height; i++) {
		// int data[] = new int[width];
		// canny_dst.get(i, 0, data);
		// //Log.i(TAG,"success"+data[100]+"zz"+onemat.width());
		// Mat temp=new Mat(1,width,CvType.CV_32SC1);
		// temp.put(0, 0, data);
		// Log.i(TAG,"success："+temp.width()+" ss："+temp.dot(onemat));
		// double data_t[]=new double[1];
		// data_t[0]=temp.dot(onemat);
		// resultmat.put(i, 0, data_t);
		// }

		int[] colheight_C = new int[height];// 记录水平投影数值
		// canny算子边缘图水平投影
		int tem;
		for (int i = 0; i < height; i++) {
			tem = 0;
			for (int j = 0; j < width; j++) {
				int data[] = new int[1];
				canny_dst.get(i, j, data);
				if (data[0] == 255) {
					++tem;
				}
			}
			colheight_C[i] = tem;
			Log.i(TAG, "canny=" + i + ":" + tem);
		}

		// 查找水平分割的位置
		int[] info_horizon = new int[13];// 保存水平分割的位置信息
		int i_h, max_hjg, pre, cur;
		i_h = 1;
		max_hjg = 0;
		pre = 0;
		cur = 0;
		for (int i = 0; i < height; i++) {
			if (colheight_C[i] < 10) {
				cur = i;
				if (cur - pre > 10) {
					info_horizon[i_h] = i + 3;
					Log.i("horizon", "horizon " + i_h + ":" + i);
					if (i_h > 1 && info_horizon[i_h] - info_horizon[i_h - 1] > max_hjg) {
						max_hjg = info_horizon[i_h] - info_horizon[i_h - 1];
					}
					i_h++;
				}
				pre = cur;
			}
		}
		info_horizon[0] = i_h;// 水平分割点的个数
		Log.i("jiange", "jiange " + max_hjg + ":" + i_h);

		// 进行垂直分割并记录分割结果
		for (int i = 1; i <= info_horizon[0]; i++) {
			if (i == 1) {
				if (info_horizon[i] > 2 * max_hjg / 3) {
					int x = 0;
					int y = 0;
					int width_patch = width;
					int height_patch = info_horizon[i];

					// 获取水平分割图片
					int data_patch[] = new int[width_patch * height_patch];
					img_gray.get(y, x, data_patch);
					Mat patch = new Mat(height_patch, width_patch, CvType.CV_32SC1);
					patch.put(0, 0, data_patch);
					num = split_vertical(patch, width_patch, height_patch, split_info, horizon_mark, y, num);
					horizon_mark++;
				}
			} else if (i == info_horizon[0]) {
				if (height - info_horizon[i - 1] > 2 * max_hjg / 3) {
					int x = 0;
					int y = info_horizon[i - 1];
					int width_patch = width;
					int height_patch = height - info_horizon[i - 1];

					// 获取水平分割图片
					int data_patch[] = new int[width_patch * height_patch];
					img_gray.get(y, x, data_patch);
					Mat patch = new Mat(height_patch, width_patch, CvType.CV_32SC1);
					patch.put(0, 0, data_patch);
					num = split_vertical(patch, width_patch, height_patch, split_info, horizon_mark, y, num);
					horizon_mark++;

				}
			} else {
				int x = 0;
				int y = info_horizon[i - 1];
				int width_patch = width;
				int height_patch = info_horizon[i] - info_horizon[i - 1];

				// 获取水平分割图片
				int data_patch[] = new int[width_patch * height_patch];
				img_gray.get(y, x, data_patch);
				Mat patch = new Mat(height_patch, width_patch, CvType.CV_32SC1);
				patch.put(0, 0, data_patch);
				num = split_vertical(patch, width_patch, height_patch, split_info, horizon_mark, y, num);
				horizon_mark++;
			}

		}

	}

	public void split_02(Mat img_src, int width, int height, int split_info[][]) {
		// split_info = new int[50][5];// 所有分割图片的位置信息[开关行数,x,y,height,width]
		int horizon_mark = 1;
		int num = 0;
		int mark1 = 0;
		int mark2 = 0;
		// 将图片的H通道作为图片的灰度图
		Mat img_hsv = new Mat();
		Imgproc.cvtColor(img_src, img_hsv, Imgproc.COLOR_RGB2HSV);// RGB装换为HSV
		// 图片三通道分离
		List<Mat> images_hsv = new ArrayList<Mat>();
		Core.split(img_hsv, images_hsv);// hsv通道分离
		Mat img_grayhsv = images_hsv.get(1);

		// 将图片的蓝色调视图作为图片的灰度图
		// List<Mat> images = new ArrayList<Mat>();
		// Core.split(img_src, images);// RGB通道分离
		// Mat img_gray = images.get(2);// 获得第3通道分量

		Imgproc.medianBlur(img_grayhsv, img_grayhsv, 5);

		// 获取二值图
		Mat img_bw = new Mat();
		Imgproc.threshold(img_grayhsv, img_bw, 90, 255, Imgproc.THRESH_BINARY);

		img_grayhsv.convertTo(img_grayhsv, CvType.CV_32SC1);
		// img_gray.convertTo(img_gray, CvType.CV_32SC1);
		img_bw.convertTo(img_bw, CvType.CV_32SC1);

		int[] colheight_C = new int[height];// 记录水平投影数值
		// 水平投影向量
		int tem;
		for (int i = 0; i < height; i++) {
			tem = 0;
			int[] raw = new int[width];
			img_bw.get(i, 0, raw);
			for (int j = 0; j < width; j++) {
				tem = tem + raw[j];
			}
			colheight_C[i] = tem / 255;
			Log.i(TAG, "bw_img=" + i + ":" + colheight_C[i]);
		}

		// 滑动窗口检测分割点（投影值最小点）
		int window = height / 4;// 检测最小值的窗口大小
		int step = window / 3;// 检测最小值的滑动步长

		// 整个区域的最大值与最小值
		Mat rheight = new Mat(1, height, CvType.CV_32SC1);
		rheight.put(0, 0, colheight_C);
		MinMaxLocResult result = new MinMaxLocResult();
		result = Core.minMaxLoc(rheight);
		double minVal = result.minVal;
		double maxVal = result.maxVal;
		Log.i(TAG, "min:" + minVal + "max" + maxVal);
		int thresh = (int) (maxVal - (maxVal - minVal) * 4 / 5);

		// 根据最大值查询分割点
		int[] info_horizon1 = new int[8];// 保存水平分割的位置信息
		double pre_maxV = 0.0, cur_maxV = 0.0;// 前一最大值，当前最大值
		int pre_Idx = 0, cur_Idx = 0, i_v = 1;// 前一最大值位置，当前最大值位置
		for (int s = 0; s < height; s = s + step) {
			int data[] = new int[window];
			rheight.get(0, s, data);
			Mat split_Window = new Mat(1, window, CvType.CV_32SC1);
			split_Window.put(0, 0, data);
			MinMaxLocResult result_temp = new MinMaxLocResult();
			result_temp = Core.minMaxLoc(split_Window);
			double maxV = result_temp.maxVal;
			if (maxV > thresh) {
				cur_maxV = maxV;
				cur_Idx = (int) (result_temp.maxLoc.x + s);
				if (i_v == 1) {
					info_horizon1[i_v] = cur_Idx;
					i_v++;
				} else {
					if (cur_Idx - pre_Idx < step) {
						if (cur_maxV < pre_maxV) {
							info_horizon1[i_v - 1] = cur_Idx;
						}
					} else {
						info_horizon1[i_v] = cur_Idx;
						i_v++;
					}
				}
				pre_Idx = cur_Idx;
				pre_maxV = cur_maxV;
			}
		}

		info_horizon1[0] = i_v-1;
		Log.i(TAG, "info1:" + i_v);
		for (int i = 1; i <= info_horizon1[0]; i++) {
			Log.i(TAG, "info_horizon1:" + info_horizon1[i]);
		}

		// 根据最小值查询分割点
		int[] info_horizon2 = new int[8];// 保存水平分割的位置信息
		double pre_minV = 0.0, cur_minV = 0.0;// 前一最小值，当前最小值
		pre_Idx = 0;
		cur_Idx = 0;
		i_v = 1;// 前一最大值位置，当前最大值位置
		for (int s = 0; s < height; s = s + step) {
			int data[] = new int[window];
			rheight.get(0, s, data);
			Mat split_Window = new Mat(1, window, CvType.CV_32SC1);
			split_Window.put(0, 0, data);
			MinMaxLocResult result_temp = new MinMaxLocResult();
			result_temp = Core.minMaxLoc(split_Window);
			double minV = result_temp.minVal;
			double maxV = result_temp.maxVal;

			if (minV < minV + ((maxVal - minVal) / 5) && (maxV - minV) > (maxVal - minVal) / 2) {
				cur_minV = minV;
				cur_Idx = (int) (result_temp.minLoc.x + s);
				if (i_v == 1) {
					info_horizon2[i_v] = cur_Idx;
					i_v++;
				} else {
					if (cur_Idx - pre_Idx < step) {
						if (cur_minV < pre_minV) {
							info_horizon2[i_v - 1] = cur_Idx;
						}
					} else {
						info_horizon2[i_v] = cur_Idx;
						i_v++;
					}
				}
				pre_Idx = cur_Idx;
				pre_minV = cur_minV;
			}

		}
		info_horizon2[0] = i_v - 1;
		Log.i(TAG, "info2:" + i_v);
		for (int i = 1; i <= info_horizon2[0]; i++) {
			Log.i(TAG, "info_horizon2:" + info_horizon2[i]);
		}
		int[] info_horizon = new int[8];
		i_v = 1;
		for(int i=1;i<=info_horizon1[0];i++){
			for(int j=1;j<=info_horizon2[0];j++){
				if(Math.abs(info_horizon1[i]-info_horizon2[j])<step){
					info_horizon[i_v]=(info_horizon1[i]+info_horizon2[j])/2;
					i_v++;
				}
			}
		}
		info_horizon[0] = i_v - 1;
		Log.i(TAG, "info:" + i_v);
		for (int i = 1; i <= info_horizon[0]; i++) {
			Log.i(TAG, "info_horizon:" + info_horizon[i]);
		}
		// 进行垂直分割并记录分割结果
		for (int i = 1; i <= info_horizon[0]; i++) {
			if (i < info_horizon[0]) {
				if (i == 1 && info_horizon[i] > 60) {
					int x = 0;
					int y = 0;
					int width_patch = width;
					int height_patch = info_horizon[i] - x;
					// 获取水平分割图片
					int data_patch[] = new int[width_patch * height_patch];
					img_bw.get(x, y, data_patch);
					Mat patch = new Mat(height_patch, width_patch, CvType.CV_32SC1);
					patch.put(0, 0, data_patch);
					int result_num[] = split_vertical3(patch, width_patch, height_patch, split_info, horizon_mark, x, num,mark1,mark2);
					horizon_mark++;
					num = result_num[0];
					mark1 = result_num[1];
					mark2 = result_num[2];
				}
				int x = info_horizon[i];
				int y = 0;
				int width_patch = width;
				int height_patch = info_horizon[i + 1] - x;
				// 获取水平分割图片
				int data_patch[] = new int[width_patch * height_patch];
				img_bw.get(x, y, data_patch);
				Mat patch = new Mat(height_patch, width_patch, CvType.CV_32SC1);
				patch.put(0, 0, data_patch);
				int result_num[] = split_vertical3(patch, width_patch, height_patch, split_info, horizon_mark, x, num,mark1,mark2);
				horizon_mark++;
				num = result_num[0];
				mark1 = result_num[1];
				mark2 = result_num[2];
			} else {
				if (height - info_horizon[i] > (info_horizon[i] - info_horizon[i - 1]) * 2 / 3) {
					int x = info_horizon[i];
					int y = 0;
					int width_patch = width;
					int height_patch = height - x;
					// 获取水平分割图片
					int data_patch[] = new int[width_patch * height_patch];
					img_bw.get(x, y, data_patch);
					Mat patch = new Mat(height_patch, width_patch, CvType.CV_32SC1);
					patch.put(0, 0, data_patch);
					int result_num[] = split_vertical3(patch, width_patch, height_patch, split_info, horizon_mark, x, num,mark1,mark2);
					horizon_mark++;
					num = result_num[0];
					mark1 = result_num[1];
					mark2 = result_num[2];
				}
			}
		}

	}

	public int split_vertical(Mat src, int width, int height, int split_info[][], int horizon_mark, int x, int mrow) {
		float tem = 0;
		float rowwidth_C[] = new float[width];// 记录垂直投影的值
		double minVal = 0.0;
		double maxVal = 0.0;
		// 垂直投影
		for (int i = 0; i < width; i++) {
			tem = 0;
			for (int j = 0; j < height; j++) {
				int data[] = new int[1];
				src.get(j, i, data);
				tem = tem + data[0];
			}
			rowwidth_C[i] = tem / height;
			if (rowwidth_C[i] > maxVal) {
				maxVal = rowwidth_C[i];
			}
			if (rowwidth_C[i] < minVal) {
				minVal = rowwidth_C[i];
			}
		}
		Log.i(TAG, "mins:" + minVal + "maxs" + maxVal);

		// 滑动窗口检测分割点（投影值最小点）
		int window = width / 9;// 检测最小值的窗口大小
		int step = window / 2;// 检测最小值的滑动步长
		int info_vertical[] = new int[15];// 保存垂直分割位置信息

		// 整个区域的最大值与最小值
		Mat rwidth = new Mat(1, width, CvType.CV_32FC1);
		rwidth.put(0, 0, rowwidth_C);
		MinMaxLocResult result = new MinMaxLocResult();
		result = Core.minMaxLoc(rwidth);
		minVal = result.minVal;
		maxVal = result.maxVal;
		Log.i(TAG, "min:" + minVal + "max" + maxVal);

		double pre_minV = 0.0, cur_minV = 0.0;// 前一最小值，当前最小值
		int pre_Idx = 0, cur_Idx = 0, i_v = 1;// 前一最小值位置，当前最小值位置
		for (int s = 0; s < width - window; s = s + step) {
			float data[] = new float[window];
			rwidth.get(0, s, data);
			Mat split_Window = new Mat(1, window, CvType.CV_32FC1);
			split_Window.put(0, 0, data);
			MinMaxLocResult result_temp = new MinMaxLocResult();
			result_temp = Core.minMaxLoc(split_Window);
			double minV = result_temp.minVal;
			double maxV = result_temp.maxVal;

			if ((maxV - minV) > ((maxVal - minVal) / 3)) {
				cur_minV = minV;
				cur_Idx = (int) (result_temp.minLoc.x + s);
				if (i_v == 1) {
					info_vertical[i_v] = cur_Idx;
					i_v++;
				} else {
					if (cur_Idx - pre_Idx < (step / 2)) {
						if (cur_minV < pre_minV) {
							info_vertical[i_v - 1] = cur_Idx;
						}
					} else {
						info_vertical[i_v] = cur_Idx;
						i_v++;
					}
				}
				pre_Idx = cur_Idx;
				pre_minV = cur_minV;
			}

		}
		info_vertical[0] = i_v - 1;
		for (int i = 1; i <= info_vertical[0]; i++) {
			if (i < info_vertical[0]) {
				split_info[mrow][0] = horizon_mark;
				split_info[mrow][1] = x;
				split_info[mrow][2] = info_vertical[i] - ((info_vertical[i + 1] - info_vertical[i]) / 2);
				split_info[mrow][3] = height;
				split_info[mrow][4] = (info_vertical[i + 1] - info_vertical[i]);
				mrow++;
			} else {
				split_info[mrow][0] = horizon_mark;
				split_info[mrow][1] = x;
				split_info[mrow][2] = info_vertical[i] - ((info_vertical[i] - info_vertical[i - 1]) / 2);
				split_info[mrow][3] = height;
				split_info[mrow][4] = (info_vertical[i] - info_vertical[i - 1]);
				mrow++;
			}
			Log.i(TAG, "location:" + i + ":" + info_vertical[i]);
		}
		return mrow;
	}

	public int split_vertical2(Mat src, int width, int height, int split_info[][], int horizon_mark, int x, int mrow) {
		float tem = 0;
		float rowwidth_C[] = new float[width];// 记录垂直投影的值
		double minVal = 0.0;
		double maxVal = 0.0;
		// 垂直投影
		for (int i = 0; i < width; i++) {
			tem = 0;
			for (int j = 0; j < height; j++) {
				int data[] = new int[1];
				src.get(j, i, data);
				tem = tem + data[0];
			}
			rowwidth_C[i] = tem / height;
		}
		// 滑动窗口检测分割点（投影值最小点）
		int window = width / 9;// 检测最小值的窗口大小
		int step = window / 2;// 检测最小值的滑动步长
		int info_vertical[] = new int[15];// 保存垂直分割位置信息

		// 整个区域的最大值与最小值
		Mat rwidth = new Mat(1, width, CvType.CV_32FC1);
		rwidth.put(0, 0, rowwidth_C);
		MinMaxLocResult result = new MinMaxLocResult();
		result = Core.minMaxLoc(rwidth);
		minVal = result.minVal;
		maxVal = result.maxVal;
		Log.i(TAG, "min:" + minVal + "max" + maxVal);

		double pre_minV = 0.0, cur_minV = 0.0;// 前一最小值，当前最小值
		int pre_Idx = 0, cur_Idx = 0, i_v = 1;// 前一最小值位置，当前最小值位置
		for (int s = 0; s < width - window; s = s + step) {
			float data[] = new float[window];
			rwidth.get(0, s, data);
			Mat split_Window = new Mat(1, window, CvType.CV_32FC1);
			split_Window.put(0, 0, data);
			MinMaxLocResult result_temp = new MinMaxLocResult();
			result_temp = Core.minMaxLoc(split_Window);
			double minV = result_temp.minVal;
			double maxV = result_temp.maxVal;

			if ((maxV - minV) > ((maxVal - minVal) / 3)) {
				cur_minV = minV;
				cur_Idx = (int) (result_temp.minLoc.x + s);
				if (i_v == 1 && cur_Idx > step / 2) {
					info_vertical[i_v] = cur_Idx;
					i_v++;
				} else {
					if (cur_Idx - pre_Idx < (step)) {
						if (cur_minV < pre_minV) {
							info_vertical[i_v - 1] = cur_Idx;
						}
					} else {
						info_vertical[i_v] = cur_Idx;
						i_v++;
					}
				}
				pre_Idx = cur_Idx;
				pre_minV = cur_minV;
			}

		}
		info_vertical[0] = i_v - 1;
		if (info_vertical[0] == 9) {
			for (int i = 1; i <= info_vertical[0]; i++) {
				if (i < info_vertical[0]) {
					// if(info_vertical[i]<step)
					split_info[mrow][0] = horizon_mark;
					split_info[mrow][1] = x;
					split_info[mrow][2] = info_vertical[i] - ((info_vertical[i + 1] - info_vertical[i]) * 2 / 3);
					split_info[mrow][3] = height;
					if (width - (info_vertical[i]
							- ((info_vertical[i] - info_vertical[i - 1]) * 2 / 3)) > (info_vertical[i]
									- info_vertical[i - 1])) {
						split_info[mrow][4] = (info_vertical[i] - info_vertical[i - 1]);
					} else {
						split_info[mrow][4] = width - 1
								- (info_vertical[i] - ((info_vertical[i] - info_vertical[i - 1]) * 2 / 3));
					}
					mrow++;
				} else {
					split_info[mrow][0] = horizon_mark;
					split_info[mrow][1] = x;
					split_info[mrow][2] = info_vertical[i] - ((info_vertical[i] - info_vertical[i - 1]) * 2 / 3);
					split_info[mrow][3] = height;
					if (width - (info_vertical[i]
							- ((info_vertical[i] - info_vertical[i - 1]) * 2 / 3)) > (info_vertical[i]
									- info_vertical[i - 1])) {
						split_info[mrow][4] = (info_vertical[i] - info_vertical[i - 1]);
					} else {
						split_info[mrow][4] = width - 1
								- (info_vertical[i] - ((info_vertical[i] - info_vertical[i - 1]) * 2 / 3));
					}
					mrow++;
				}
				Log.i(TAG, "location:" + i + ":" + info_vertical[i]);
			}
		} else {
			for (int i = 0; i < 9; i++) {
				split_info[mrow][0] = horizon_mark;
				split_info[mrow][1] = x;
				split_info[mrow][2] = split_info[mrow - 9][2];
				split_info[mrow][3] = height;
				split_info[mrow][4] = split_info[mrow][4];
				mrow++;
			}

		}
		return mrow;
	}

	public void split_vertical3(Mat src, int width, int height, int info_vertical[]) {
		float tem = 0;
		float rowwidth_C[] = new float[width];// 记录垂直投影的值
		double minVal = 0.0;
		double maxVal = 0.0;
		// 垂直投影
		for (int i = 0; i < width; i++) {
			tem = 0;
			for (int j = 0; j < height; j++) {
				int data[] = new int[1];
				src.get(j, i, data);
				tem = tem + data[0];
			}
			rowwidth_C[i] = tem / height;
		}
		// 滑动窗口检测分割点（投影值最小点）
		int window = width / 9;// 检测最小值的窗口大小
		int step = window / 2;// 检测最小值的滑动步长
		// int info_vertical[] = new int[15];// 保存垂直分割位置信息

		// 整个区域的最大值与最小值
		Mat rwidth = new Mat(1, width, CvType.CV_32FC1);
		rwidth.put(0, 0, rowwidth_C);
		MinMaxLocResult result = new MinMaxLocResult();
		result = Core.minMaxLoc(rwidth);
		minVal = result.minVal;
		maxVal = result.maxVal;
		Log.i(TAG, "min:" + minVal + "max" + maxVal);

		double pre_minV = 0.0, cur_minV = 0.0;// 前一最小值，当前最小值
		int pre_Idx = 0, cur_Idx = 0, i_v = 1;// 前一最小值位置，当前最小值位置
		for (int s = 0; s < width - window; s = s + step) {
			float data[] = new float[window];
			rwidth.get(0, s, data);
			Mat split_Window = new Mat(1, window, CvType.CV_32FC1);
			split_Window.put(0, 0, data);
			MinMaxLocResult result_temp = new MinMaxLocResult();
			result_temp = Core.minMaxLoc(split_Window);
			double minV = result_temp.minVal;
			double maxV = result_temp.maxVal;

			if ((maxV - minV) > ((maxVal - minVal) / 3)) {
				cur_minV = minV;
				cur_Idx = (int) (result_temp.minLoc.x + s);
				if (i_v == 1 && cur_Idx > step / 2) {
					info_vertical[i_v] = cur_Idx;
					i_v++;
				} else {
					if (cur_Idx - pre_Idx < (step)) {
						if (cur_minV < pre_minV) {
							info_vertical[i_v - 1] = cur_Idx;
						}
					} else {
						info_vertical[i_v] = cur_Idx;
						i_v++;
					}
				}
				pre_Idx = cur_Idx;
				pre_minV = cur_minV;
			}

		}
		info_vertical[0] = i_v - 1;
	}

	public void split_vertical4(Mat src, int width, int height, int info_vertical[]) {
		Mat img_bw = new Mat();
		Imgproc.threshold(src, img_bw, 90, 255, Imgproc.THRESH_BINARY);
		float tem = 0;
		float rowwidth_C[] = new float[width];// 记录垂直投影的值
		double minVal = 0.0;
		double maxVal = 0.0;
		// 垂直投影
		for (int i = 0; i < width; i++) {
			tem = 0;
			for (int j = 0; j < height; j++) {
				int data[] = new int[1];
				src.get(j, i, data);
				tem = tem + data[0];
			}
			rowwidth_C[i] = tem / height;
		}
		// 滑动窗口检测分割点（投影值最小点）
		int window = width / 9;// 检测最小值的窗口大小
		int step = window / 2;// 检测最小值的滑动步长
		// int info_vertical[] = new int[15];// 保存垂直分割位置信息

		// 整个区域的最大值与最小值
		Mat rwidth = new Mat(1, width, CvType.CV_32FC1);
		rwidth.put(0, 0, rowwidth_C);
		MinMaxLocResult result = new MinMaxLocResult();
		result = Core.minMaxLoc(rwidth);
		minVal = result.minVal;
		maxVal = result.maxVal;
		Log.i(TAG, "min:" + minVal + "max" + maxVal);

		double pre_minV = 0.0, cur_minV = 0.0;// 前一最小值，当前最小值
		int pre_Idx = 0, cur_Idx = 0, i_v = 1;// 前一最小值位置，当前最小值位置
		for (int s = 0; s < width - window; s = s + step) {
			float data[] = new float[window];
			rwidth.get(0, s, data);
			Mat split_Window = new Mat(1, window, CvType.CV_32FC1);
			split_Window.put(0, 0, data);
			MinMaxLocResult result_temp = new MinMaxLocResult();
			result_temp = Core.minMaxLoc(split_Window);
			double minV = result_temp.minVal;
			double maxV = result_temp.maxVal;

			if ((maxV - minV) > ((maxVal - minVal) / 3)) {
				cur_minV = minV;
				cur_Idx = (int) (result_temp.minLoc.x + s);
				if (i_v == 1 && cur_Idx > step / 2) {
					info_vertical[i_v] = cur_Idx;
					i_v++;
				} else {
					if (cur_Idx - pre_Idx < (step)) {
						if (cur_minV < pre_minV) {
							info_vertical[i_v - 1] = cur_Idx;
						}
					} else {
						info_vertical[i_v] = cur_Idx;
						i_v++;
					}
				}
				pre_Idx = cur_Idx;
				pre_minV = cur_minV;
			}

		}
		info_vertical[0] = i_v - 1;
	}

	public int[] split_vertical3(Mat src, int width, int height, int split_info[][], int horizon_mark, int x, int mrow,int mark1,int mark2) {
		float tem = 0;
		float rowwidth_C[] = new float[width];// 记录垂直投影的值
		// 垂直投影
		for (int i = 0; i < width; i++) {
			tem = 0;
			for (int j = 0; j < height; j++) {
				int data[] = new int[1];
				src.get(j, i, data);
				if (data[0] == 255) {
					tem++;
				}
			}
			rowwidth_C[i] = tem;
		}
		int start = 0;
		int end = width;
		for (int i = 0; i < width / 10; i++) {
			if (rowwidth_C[i] > 15 && rowwidth_C[i+1] - rowwidth_C[i] > 0&& i>mark1-8) {
				start = i;
				mark1  = start;
				break;
			}
		}
		if(mark2==0){
			mark2 = width-8;
		}
		for (int i = width-1; i > width*9 / 10; i--) {
			if (rowwidth_C[i] > 15 && rowwidth_C[i] - rowwidth_C[i -1] > 0 && i<mark2+8) {
				end = i;
				mark2 = end;
				break;
			}
		}
		int jiange = (end - start) / 9;
		for (int i = 0; i < 9; i++) {
			split_info[mrow][0] = horizon_mark;
			split_info[mrow][1] = x;
			split_info[mrow][2] = start + jiange * i;
			split_info[mrow][3] = height;
			split_info[mrow][4] = jiange;
			mrow++;
		}
		int[] result = new int[3];
		result[0] = mrow;result[1]=mark1;result[2]=mark2;
		return result;
	}

	public void split_hist(Mat src) {

		int height = src.height();
		int width = src.width();

		List<Mat> images_RGB = new ArrayList<Mat>();
		Core.split(src, images_RGB);// RGB通道分离
		// 将图片的蓝色调视图作为图片的灰度图
		Mat imgR = images_RGB.get(0);// 获得第二通道分量
		Mat imgG = images_RGB.get(1);// 获得第二通道分量
		Mat imgB = images_RGB.get(2);// 获得第二通道分量

		Mat img_hsv = new Mat();
		Imgproc.cvtColor(src, img_hsv, Imgproc.COLOR_RGB2HSV);

		List<Mat> images_HSV = new ArrayList<Mat>();
		Core.split(img_hsv, images_HSV);// HSV通道分离
		// 获取每个通道分量
		Mat imgH = images_HSV.get(0);// 获得第二通道分量
		Mat imgS = images_HSV.get(1);// 获得第二通道分量
		Mat imgV = images_HSV.get(2);// 获得第二通道分量
		imgH.convertTo(imgH, CvType.CV_32SC1);
		imgS.convertTo(imgS, CvType.CV_32SC1);
		imgV.convertTo(imgV, CvType.CV_32SC1);
		int[] Hdata = new int[height * width];
		imgH.get(0, 0, Hdata);
		int[] Sdata = new int[height * width];
		imgS.get(0, 0, Sdata);
		int[] Vdata = new int[height * width];
		imgV.get(0, 0, Vdata);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (j == 0) {
					hist_red[i] = 0;
					hist_white[i] = 0;
					hist_yellow[i] = 0;
				}
				int[] data_h = new int[1];
				imgH.get(j, i, data_h);
				int[] data_s = new int[1];
				imgS.get(j, i, data_s);
				int[] data_v = new int[1];
				imgV.get(j, i, data_v);
				if ((data_h[0] > red_hmin1 && data_h[0] < red_hmax1) || (data_h[0] > red_hmin2 && data_h[0] < red_hmax2)
						&& (data_s[0] > red_smin && data_s[0] < red_smax)
						&& (data_v[0] > red_vmin && data_v[0] < red_vmax)) {
					hist_red[i] = hist_red[i] + 1;
				} else if ((data_h[0] > white_hmin && data_h[0] < white_hmax)
						&& (data_s[0] > white_smin && data_s[0] < white_smax)
						&& (data_v[0] > white_vmin && data_v[0] < white_vmax)) {
					hist_white[i] = hist_white[i] + 1;
				} else if ((data_h[0] > yellow_hmin && data_h[0] < yellow_hmax)
						&& (data_s[0] > yellow_smin && data_s[0] < yellow_smax)
						&& (data_v[0] > yellow_vmin && data_v[0] < yellow_vmax)) {
					hist_yellow[i] = hist_yellow[i] + 1;
				}
			}
			Log.i("message", "red" + hist_red[i] + " white:" + hist_white[i] + " yellow:" + hist_yellow[i]);
		}

		src.convertTo(src, CvType.CV_32FC3);
		Mat img_gray = new Mat(src.height(), src.width(), CvType.CV_32FC1);
		Imgproc.cvtColor(src, img_gray, Imgproc.COLOR_RGB2GRAY);
		float data[] = new float[height * width];
		img_gray.get(0, 0, data);
		img_gray.convertTo(img_gray, CvType.CV_64FC1);
		double[] data1 = new double[height * width];
		img_gray.get(0, 0, data1);

		hist_red = new int[width];
		hist_white = new int[width];
		hist_yellow = new int[width];
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++) {
				if (j == 0) {
					hist_red[i] = 0;
					hist_white[i] = 0;
					hist_yellow[i] = 0;
				}
				double[] datal = new double[1];
				img_gray.get(j, i, datal);
				datal[0] = datal[0] * 1000;
				if (datal[0] > 130000 && datal[0] < 134000)
					hist_red[i] = hist_red[i] + 1;
				else if (datal[0] > 220000)
					hist_white[i] = hist_white[i] + 1;
				else if (datal[0] > 195000 && datal[0] < 200000) {
					hist_yellow[i] = hist_yellow[i] + 1;
				}
				Log.i("message", "Config:");
			}
		Log.i("Massage", "data");
	}

	public int[] split_all(Bitmap bitmap) {
		Bitmap bitmap2 = Bitmap.createBitmap(bitmap, bitmap.getWidth() / 2, 0, bitmap.getWidth() / 10,
				bitmap.getHeight());
		Mat img_src = new Mat();
		Utils.bitmapToMat(bitmap2, img_src);
		if (img_src.channels() == 4) {
			Imgproc.cvtColor(img_src, img_src, Imgproc.COLOR_RGBA2RGB);
		}
		Mat img_hsv = new Mat();
		Imgproc.cvtColor(img_src, img_hsv, Imgproc.COLOR_RGB2HSV);// RGB装换为HSV
		// 图片三通道分离
		List<Mat> images_hsv = new ArrayList<Mat>();
		Core.split(img_hsv, images_hsv);// hsv通道分离
		Mat img_gray = images_hsv.get(1);
		Imgproc.medianBlur(img_gray, img_gray, 5);
		// 获取二值图
		Mat img_bw = new Mat();
		Imgproc.threshold(img_gray, img_bw, 100, 255, Imgproc.THRESH_BINARY);
		img_bw.convertTo(img_bw, CvType.CV_32SC1);

		Bitmap bitmap3 = Bitmap.createBitmap(bitmap, 0, bitmap.getHeight() / 2, bitmap.getWidth(),
				bitmap.getHeight() / 8);
		Mat img_src2 = new Mat();
		Utils.bitmapToMat(bitmap3, img_src2);
		if (img_src2.channels() == 4) {
			Imgproc.cvtColor(img_src2, img_src2, Imgproc.COLOR_RGBA2RGB);
		}
		Mat img_hsv2 = new Mat();
		Imgproc.cvtColor(img_src2, img_hsv2, Imgproc.COLOR_RGB2HSV);// RGB装换为HSV
		// 图片三通道分离
		List<Mat> images_hsv2 = new ArrayList<Mat>();
		Core.split(img_hsv2, images_hsv2);// hsv通道分离
		Mat img_gray2 = images_hsv2.get(1);
		Imgproc.medianBlur(img_gray2, img_gray2, 5);
		// 获取二值图
		Mat img_bw2 = new Mat();
		Imgproc.threshold(img_gray2, img_bw2, 100, 255, Imgproc.THRESH_BINARY);
		img_bw2.convertTo(img_bw2, CvType.CV_32SC1);

		// 水平投影向量
		int tem;
		int x = 0, y = 0, x_end = 0, y_end = 0;
		for (int i = 0; i < img_bw.height(); i++) {
			tem = 0;
			int[] raw = new int[img_bw.width()];
			img_bw.get(i, 0, raw);
			for (int j = 0; j < img_bw.width(); j++) {
				tem = tem + raw[j];
			}
			if (tem > 2) {
				x = i;
				break;
			}
		}
		for (int i = img_bw.height() - 1; i > 0; i--) {
			tem = 0;
			int[] raw = new int[img_bw.width()];
			img_bw.get(i, 0, raw);
			for (int j = 0; j < img_bw.width(); j++) {
				tem = tem + raw[j];
			}
			if (tem > 2) {
				x_end = i;
				break;
			}
		}
		int rowwidth_C[] = new int[img_bw2.width()];// 记录垂直投影的值
		// 垂直投影
		for (int i = 0; i < img_bw2.width(); i++) {
			tem = 0;
			for (int j = 0; j < img_bw2.height(); j++) {
				int data[] = new int[1];
				img_bw2.get(j, i, data);
				if (data[0] == 255) {
					tem++;
				}
			}
			rowwidth_C[i] = tem;
		}
		for (int i = 0; i < img_bw2.width(); i++) {
			if (rowwidth_C[i] > 2) {
				y = i;
				break;
			}
		}
		for (int i = img_bw2.width() - 1; i > 0; i--) {
			if (rowwidth_C[i] > 2) {
				y_end = i;
				break;
			}
		}
		// Bitmap result_bmp = Bitmap.createBitmap(bitmap, y, x, y_end-y,
		// x_end-x);
		// Mat result_img = new Mat();
		// Utils.bitmapToMat(result_bmp, result_img);
		// if (result_img.channels() == 4) {
		// Imgproc.cvtColor(result_img, result_img, Imgproc.COLOR_RGBA2RGB);
		// }
		int result[] = new int[4];
		result[0] = x;
		result[1] = y;
		result[2] = x_end - x;
		result[3] = y_end - y;
		return result;
	}
}
