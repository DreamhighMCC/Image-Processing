package com.example.CVswitch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvSVM;
import org.opencv.objdetect.HOGDescriptor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class Pictureshow extends Activity implements OnClickListener {

	private Button type1;
	private Button Type2;
	private ImageView show_view;
	private int width;
	private int height;
	private CvSVM mSvm;
	public static Bitmap bmp_src;
	private Mat img_src;
	private img_split msplit = new img_split();
	private img_feature mImg_feature = new img_feature();
	private int split_info[][] = new int[50][5];// 所有分割图片的位置信息[开关行数,x,y,height,width]
	public static float result[][];

	private ImageView img1;
	private ImageView img2;
	private ImageView img3;
	private ImageView img4;
	private ImageView img5;
	private ImageView img6;
	private ImageView img7;
	private ImageView img8;
	private ImageView img9;
	
	
	private static final String TAG = "Switch RecognizeAcitivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pictureshow);
		type1 = (Button) findViewById(R.id.type1);
		Type2 = (Button) findViewById(R.id.type2);
		show_view = (ImageView) findViewById(R.id.show_view);
		
		Bitmap bmp = MainActivity.bmp;
//		int info[]=new int[4];
//		info= msplit.split_all(bitmap);
//		Bitmap bmp = Bitmap.createBitmap(bitmap, info[1], info[0], info[3], info[2]);
		bmp = zoomImage(bmp, 800, 450);
		int width_l = bmp.getWidth();
		int height_l = bmp.getHeight();
		show_view.setImageBitmap(bmp);
		
		Mat img_srcl = new Mat();
		// 获取彩色图像所对应的像素数据
		Utils.bitmapToMat(bmp, img_srcl);
		if (img_srcl.channels() == 4) {
			Imgproc.cvtColor(img_srcl, img_srcl, Imgproc.COLOR_RGBA2RGB);
		}
//		// 进行图片分割
//		msplit.split_02(img_srcl, width_l, height_l, split_info);
//		img1 = (ImageView) findViewById(R.id.img11);
//		Bitmap patch1 = Bitmap.createBitmap(bmp, split_info[0][2], split_info[0][1], split_info[0][4], split_info[0][3]);
//		img1.setImageBitmap(patch1);
//		img2 = (ImageView) findViewById(R.id.img12);
//		Bitmap patch2 = Bitmap.createBitmap(bmp, split_info[9][2], split_info[9][1], split_info[9][4], split_info[9][3]);
//		img2.setImageBitmap(patch2);
//		img3 = (ImageView) findViewById(R.id.img13);
//		Bitmap patch3 = Bitmap.createBitmap(bmp, split_info[18][2], split_info[18][1], split_info[18][4], split_info[18][3]);
//		img3.setImageBitmap(patch3);
//		img4 = (ImageView) findViewById(R.id.img14);
//		Bitmap patch4 = Bitmap.createBitmap(bmp, split_info[27][2], split_info[27][1], split_info[27][4], split_info[27][3]);
//		img4.setImageBitmap(patch4);
//		img5 = (ImageView) findViewById(R.id.img15);
//		Bitmap patch5 = Bitmap.createBitmap(bmp, split_info[36][2], split_info[36][1], split_info[36][4], split_info[36][3]);
//		img5.setImageBitmap(patch5);
//		img6 = (ImageView) findViewById(R.id.img16);
//		Bitmap patch6 = Bitmap.createBitmap(bmp, split_info[5][2], split_info[5][1], split_info[5][4], split_info[5][3]);
//		img6.setImageBitmap(patch6);
//		img7 = (ImageView) findViewById(R.id.img17);
//		Bitmap patch7 = Bitmap.createBitmap(bmp, split_info[6][2], split_info[6][1], split_info[6][4], split_info[6][3]);
//		img7.setImageBitmap(patch7);
//		img8 = (ImageView) findViewById(R.id.img18);
//		Bitmap patch8 = Bitmap.createBitmap(bmp, split_info[7][2], split_info[7][1], split_info[7][4], split_info[7][3]);
//		img8.setImageBitmap(patch8);
//		img9 = (ImageView) findViewById(R.id.img19);
//		Bitmap patch9 = Bitmap.createBitmap(bmp, split_info[8][2], split_info[8][1], split_info[8][4], split_info[8][3]);
//		img9.setImageBitmap(patch9);
		
		Mat img_hsv = new Mat();
		Imgproc.cvtColor(img_srcl, img_hsv, Imgproc.COLOR_RGB2HSV);// RGB装换为HSV
		// 图片三通道分离
		List<Mat> images_hsv = new ArrayList<Mat>();
		Core.split(img_hsv, images_hsv);// hsv通道分离
		Mat img_gray = images_hsv.get(1);
		Imgproc.medianBlur(img_gray, img_gray, 5);

		// 获取二值图
		Mat img_bw = new Mat();
		Imgproc.threshold(img_gray, img_bw, 90, 255, Imgproc.THRESH_BINARY);
		Bitmap bmp2 = Bitmap.createBitmap(img_bw.width(), img_bw.height(), Config.RGB_565);
		Utils.matToBitmap(img_bw, bmp2);
//		show_view.setImageBitmap(bmp2);
		Log.i("WH", "width" + width + ";height" + height);
		type1.setOnClickListener(this);
		Type2.setOnClickListener(this);
	}

	public void onClick(View v) {
		int id = v.getId();
		Intent intent = null;
		switch (id) {
		case R.id.type1:
			bmp_src = MainActivity.bmp;
			bmp_src = zoomImage(bmp_src, 800, 450);
			width = bmp_src.getWidth();
			height = bmp_src.getHeight();
			img_src = new Mat();
			// 获取彩色图像所对应的像素数据
			Utils.bitmapToMat(bmp_src, img_src);
			if (img_src.channels() == 4) {
				Imgproc.cvtColor(img_src, img_src, Imgproc.COLOR_RGBA2RGB);
			}
			// 进行图片分割
			msplit.split_01(img_src, width, height, split_info);

//			imgshow(bmp_src, split_info);
			// 识别开关
			result = recognitiontype1(bmp_src, split_info);
			intent = new Intent(Pictureshow.this, Resultshow.class);
			startActivity(intent);
			Log.i(TAG, "type1 result:" + result.toString());
			break;
		case R.id.type2:
			int info[]=new int[4];
			info= msplit.split_all(MainActivity.bmp);
			bmp_src = Bitmap.createBitmap(MainActivity.bmp, info[1], info[0], info[3], info[2]);
			bmp_src = zoomImage(bmp_src, 800, 450);
			width = bmp_src.getWidth();
			height = bmp_src.getHeight();
			img_src = new Mat();
			// 获取彩色图像所对应的像素数据
			Utils.bitmapToMat(bmp_src, img_src);
			if (img_src.channels() == 4) {
				Imgproc.cvtColor(img_src, img_src, Imgproc.COLOR_RGBA2RGB);
			}
			// 进行图片分割
			msplit.split_02(img_src, width, height, split_info);
//			imgshow(bmp_src, split_info);
			// 识别开关
			result = recognitiontype2(bmp_src, split_info);

			intent = new Intent(Pictureshow.this, Resultshow.class);
			startActivity(intent);
			Log.i(TAG, "type1 result:" + result.toString());
			break;
		default:
			break;
		}

	}

//	public void imgshow(Bitmap bmp, int split_info[][]) {
//		ImageView img1 = (ImageView) findViewById(R.id.img11);
//		Bitmap patch1 = Bitmap.createBitmap(bmp, split_info[0][2], split_info[0][1], split_info[0][4], split_info[0][3]);
//		img1.setImageBitmap(patch1);
//		ImageView img2 = (ImageView) findViewById(R.id.img12);
//		Bitmap patch2 = Bitmap.createBitmap(bmp, split_info[1][2], split_info[1][1], split_info[1][4], split_info[1][3]);
//		img2.setImageBitmap(patch2);
//		ImageView img3 = (ImageView) findViewById(R.id.img13);
//		Bitmap patch3 = Bitmap.createBitmap(bmp, split_info[2][2], split_info[2][1], split_info[2][4], split_info[2][3]);
//		img3.setImageBitmap(patch3);
//		ImageView img4 = (ImageView) findViewById(R.id.img14);
//		Bitmap patch4 = Bitmap.createBitmap(bmp, split_info[3][2], split_info[3][1], split_info[3][4], split_info[3][3]);
//		img4.setImageBitmap(patch4);
//		ImageView img5 = (ImageView) findViewById(R.id.img15);
//		Bitmap patch5 = Bitmap.createBitmap(bmp, split_info[4][2], split_info[4][1], split_info[4][4], split_info[4][3]);
//		img5.setImageBitmap(patch5);
//		ImageView img6 = (ImageView) findViewById(R.id.img16);
//		Bitmap patch6 = Bitmap.createBitmap(bmp, split_info[5][2], split_info[5][1], split_info[5][4], split_info[5][3]);
//		img6.setImageBitmap(patch6);
//		ImageView img7 = (ImageView) findViewById(R.id.img17);
//		Bitmap patch7 = Bitmap.createBitmap(bmp, split_info[6][2], split_info[6][1], split_info[6][4], split_info[6][3]);
//		img7.setImageBitmap(patch7);
//		ImageView img8 = (ImageView) findViewById(R.id.img18);
//		Bitmap patch8 = Bitmap.createBitmap(bmp, split_info[7][2], split_info[7][1], split_info[7][4], split_info[7][3]);
//		img8.setImageBitmap(patch8);
//		ImageView img9 = (ImageView) findViewById(R.id.img19);
//		Bitmap patch9 = Bitmap.createBitmap(bmp, split_info[8][2], split_info[8][1], split_info[8][4], split_info[8][3]);
//		img9.setImageBitmap(patch9);
//
//	}

	/***
	 * 图片的缩放方法
	 *
	 * @param bgimage
	 *            ：源图片资源
	 * @param newWidth
	 *            ：缩放后宽度
	 * @param newHeight
	 *            ：缩放后高度
	 * @return
	 */
	public static Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
		// 获取这个图片的宽和高
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width, (int) height, matrix, true);
		return bitmap;
	}

	private float[][] recognitiontype1(Bitmap bitmap, int info[][]) {
		// 加载训练模型
		try {
			// Copy the resource into a temp file so OpenCV can load it
			InputStream isSvm = getResources().openRawResource(R.raw.model1);// 输入文件名
			File svmDir = getDir("svm", Context.MODE_PRIVATE);
			File mSvmFile = new File(svmDir, "svm_data_recognition1.xml");
			FileOutputStream osSvm = new FileOutputStream(mSvmFile);

			byte[] bufferSvm = new byte[4096];
			int bytesReadSvm;
			while ((bytesReadSvm = isSvm.read(bufferSvm)) != -1) {
				osSvm.write(bufferSvm, 0, bytesReadSvm);
			}
			isSvm.close();
			osSvm.close();
			mSvm = new CvSVM();
			mSvm.load(mSvmFile.getAbsolutePath());
		} catch (Exception e) {
			Log.e("OpenCVActivity", "Error loading xml", e);
		}
		float[][] result = new float[50][2];
		for (int i = 0; info[i][0] > 0; i++) {
			Bitmap patch = Bitmap.createBitmap(bmp_src, info[i][2], info[i][1], info[i][4], info[i][3]);
			Mat pos_srct = new Mat();
			Mat pos_src = new Mat();
			// 获取彩色图像所对应的像素数据
			Utils.bitmapToMat(patch, pos_srct);
			Size msize = new Size(75, 100);
			Imgproc.resize(pos_srct, pos_src, msize);// 改变图片的大小为75*100

			// mImg_feature.HSV_split(pos_src);
			mImg_feature.RGB_split(pos_src);
			Mat feature = new Mat();
			feature = mImg_feature.G_feature;

			/************************************************ 存储 **********************************************************/
			feature.convertTo(feature, CvType.CV_32FC1);

			result[i][0] = info[i][0];
			result[i][1] = mSvm.predict(feature);
			Log.i(TAG, "test success!");
		}
		return result;

	}

	private float[][] recognitiontype2(Bitmap bitmap, int info[][]) {
		// 加载训练模型
		try {
			// Copy the resource into a temp file so OpenCV can load it
			InputStream isSvm = getResources().openRawResource(R.raw.model2);// 输入文件名
			File svmDir = getDir("svm", Context.MODE_PRIVATE);
			File mSvmFile = new File(svmDir, "svm_data_recognition2.xml");
			FileOutputStream osSvm = new FileOutputStream(mSvmFile);

			byte[] bufferSvm = new byte[4096];
			int bytesReadSvm;
			while ((bytesReadSvm = isSvm.read(bufferSvm)) != -1) {
				osSvm.write(bufferSvm, 0, bytesReadSvm);
			}
			isSvm.close();
			osSvm.close();
			mSvm = new CvSVM();
			mSvm.load(mSvmFile.getAbsolutePath());
		} catch (Exception e) {
			Log.e("OpenCVActivity", "Error loading xml", e);
		}
		float[][] result = new float[50][2];
		for (int i = 0; info[i][0] > 0; i++) {
			Bitmap patch = Bitmap.createBitmap(bmp_src, info[i][2], info[i][1], info[i][4], info[i][3]);
			Mat pos_srct = new Mat();
			Mat pos_src = new Mat();
			// 获取彩色图像所对应的像素数据
			Utils.bitmapToMat(patch, pos_srct);
			Size msize = new Size(75, 100);
			Imgproc.resize(pos_srct, pos_src, msize);// 改变图片的大小为75*100

			if (pos_src.channels() == 4) {
				Imgproc.cvtColor(pos_src, pos_src, Imgproc.COLOR_RGBA2RGB);
			}
			// int ch = pos_src.channels();
			Size winsize = new Size(25, 50);
			Size blocksize = new Size(10, 20);
			Size blockStride = new Size(5, 10);
			Size cellsize = new Size(10, 10);
			Size winStride = new Size(8, 8);
			Size padding = new Size(0, 0);
			int nbins = 9;
			HOGDescriptor hog = new HOGDescriptor(winsize, blocksize, blockStride, cellsize, nbins);
			MatOfFloat descriptors = new MatOfFloat();
			MatOfPoint locations = new MatOfPoint();
			hog.compute(pos_src, descriptors, winStride, padding, locations);

			/************************************************
			 * 存储
			 **********************************************************/
			float data_h[] = new float[14112];
			descriptors.get(0, 0, data_h);
			descriptors.convertTo(descriptors, CvType.CV_32FC1);

			// int t = mSvm.get_var_count();
			result[i][0] = info[i][0];
			result[i][1] = mSvm.predict(descriptors);
			Log.i(TAG, "test success!");
		}
		return result;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pictureshow, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
