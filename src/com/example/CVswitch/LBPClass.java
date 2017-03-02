package com.example.CVswitch;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class LBPClass {

	public int table[] = new int[256];
	public Mat LbpImg;// Lbp图像
	public Mat Lbp59Mat; // 返回的mat是一个59维的矩阵 32FC1;

	public int getHopCount(int i) {
		int a[] = new int[8];
		int cnt = 0;
		int k = 7;
		for(;i>0;i--){
			a[k] = i & 1;
			i = i >> 1;
			--k;
		}
//		while (i) {
//			a[k] = i & 1;
//			i = i >> 1;
//			--k;
//		}
		for (k = 0; k < 7; k++) {
			if (a[k] != a[k + 1]) {
				++cnt;
			}
		}
		if (a[0] != a[7]) {
			++cnt;
		}
		return cnt;
	}

	// 降维数组 由256->59
	public void lbp59table(int table[])
	{
//		memset(table, 0, 256);
		int temp = 1;
		for (int i = 0; i < 256; i++)
		{
			if (getHopCount(i) <= 2)    // 跳变次数<=2 的为非0值  
			{
				table[i] = temp;
				temp++;
			}
		}
	}

	public void uniformLBP(Mat image, Mat LbpImg, Mat Lbp59Mat, char table[])
	{
		image.convertTo(image, CvType.CV_32SC1);
		Lbp59Mat = new Mat(1, 59, CvType.CV_32FC1);
		float saveResult[]=new float[59];
		LbpImg.create(image.size(), image.type());
		for (int x = 1; x < image.height() - 1; x++)
		{
			for (int y = 1; x < image.width() - 1; x++)
			{
				int neighbor[] = new int[8];
				int data[]=new int[1];
				image.get(x - 1, y - 1, data);
				neighbor[0]=data[0];
				
				image.get(x - 1, y, data);
				neighbor[1]=data[0];
				
				image.get(x - 1, y + 1, data);
				neighbor[2]=data[0];
				
				image.get(x, y + 1, data);
				neighbor[3]=data[0];
				
				image.get(x + 1, y + 1, data);
				neighbor[4]=data[0];
				
				image.get(x + 1, y, data);
				neighbor[5]=data[0];
				
				image.get(x + 1, y - 1, data);
				neighbor[6]=data[0];
				
				image.get(x, y-1, data);
				neighbor[7]=data[0];
				
				image.get(x, y, data);
				int center = data[0];
				
				int temp = 0;
				for (int k = 0; k < 8; k++)
				{
					int val=0;
					if(neighbor[k] >= center){
						val=1;
					}
					temp += val* (1 << k);  // 计算LBP的值  
				}
				int value = table[temp];
				LbpImg.put(x, y, table[temp]);//  降为59维空间 
				//LbpImg.at<uchar>(y, x) = table[temp];    				
				for (int k = 0; k < 59; k++)
				{
					if (value == k)
					{
						saveResult[k]++;
					}
				}

			}
		}
		Lbp59Mat.put(0, 0, saveResult);
//		for (int i = 0; i < 59; i++)
//		{
//			float v59 = saveResult[i];
//			Lbp59Mat.put(0, i, v59);
//			//Lbp59Mat.at<float>(0, i) = v59;
//		}
	}

}
