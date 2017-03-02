package com.example.CVswitch;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class img_feature {
	public Mat H_feature; 
	public Mat S_feature; 
	public Mat V_feature;
	public Mat R_feature; 
	public Mat G_feature; 
	public Mat B_feature;
	public double height_s = 100;
	public double width_s = 75;
	/*
	 * HSV����
	 */
	public void HSV_split(Mat src){	
		//�ı�ͼƬ�Ĵ�СΪ75*100
		Mat src1=new Mat();
		Size msize = new Size(width_s,height_s);
		Imgproc.resize(src, src1, msize);
		
		Mat img_hsv=new Mat();
		Imgproc.cvtColor(src1,img_hsv,Imgproc.COLOR_RGB2HSV);//RGBװ��ΪHSV
		//ͼƬ��ͨ������
		List<Mat> images = new ArrayList<Mat>();
		Core.split(img_hsv, images);//hsvͨ������
		Mat hMat = images.get(0);
		Mat sMat = images.get(1);
		Mat vMat = images.get(2);
		hMat.convertTo(hMat, CvType.CV_32SC1);
		sMat.convertTo(sMat, CvType.CV_32SC1);
		vMat.convertTo(vMat, CvType.CV_32SC1);
		int data[]=new int[7500];
		
		hMat.get(0, 0, data);
		H_feature = new Mat(1, 7500,CvType.CV_32SC1);
		H_feature.put(0, 0, data);
		
		sMat.get(0, 0, data);
		S_feature = new Mat(1, 7500,CvType.CV_32SC1);
		S_feature.put(0, 0, data);
		
		vMat.get(0, 0, data);
		V_feature = new Mat(1, 7500,CvType.CV_32SC1);
		V_feature.put(0, 0, data);
		
	}
	/*
	 * RGB����
	 */
	public void RGB_split(Mat src){	
		//�ı�ͼƬ�Ĵ�СΪ75*100
		Mat src1=new Mat();
		Size msize = new Size(width_s,height_s);
		Imgproc.resize(src, src1, msize);

		//ͼƬRGB��ͨ������
		List<Mat> images = new ArrayList<Mat>();
		Core.split(src1, images);
		Mat RMat = images.get(0);
		Mat GMat = images.get(1);
		Mat BMat = images.get(2);
		RMat.convertTo(RMat, CvType.CV_32SC1);
		GMat.convertTo(GMat, CvType.CV_32SC1);
		BMat.convertTo(BMat, CvType.CV_32SC1);
		int data[]=new int[7500];
		
		RMat.get(0, 0, data);
		R_feature = new Mat(1, 7500,CvType.CV_32SC1);
		R_feature.put(0, 0, data);
		
		GMat.get(0, 0, data);
		G_feature = new Mat(1, 7500,CvType.CV_32SC1);
		G_feature.put(0, 0, data);
		
		BMat.get(0, 0, data);
		B_feature = new Mat(1, 7500,CvType.CV_32SC1);
		B_feature.put(0, 0, data);
		
	}

}
