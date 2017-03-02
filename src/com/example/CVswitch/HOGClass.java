package com.example.CVswitch;

import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

public class HOGClass {
	
	public MatOfFloat hogfeature(Mat src){
		
		Size msize = new Size(75, 100);
		Imgproc.resize(src, src, msize);// 改变图片的大小为75*100

		if(src.channels()==4){
			Imgproc.cvtColor(src, src, Imgproc.COLOR_RGBA2RGB);
		}
		Size winsize = new Size(25,50);
		Size blocksize = new Size(10,20);
		Size blockStride = new Size(5,10);
		Size cellsize=new Size(10,10);
		Size winStride = new Size(8,8);
		Size padding = new Size(0,0);
		int nbins=9;
		HOGDescriptor hog=new HOGDescriptor(winsize, blocksize, blockStride, cellsize, nbins );			
		MatOfFloat descriptors= new MatOfFloat();
		MatOfPoint locations=new MatOfPoint();	
		hog.compute(src, descriptors, winStride, padding, locations);
		return descriptors;
		
	}

}
