//package com.example.CVswitch;
//
//import org.opencv.android.Utils;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import org.opencv.core.MatOfFloat;
//import org.opencv.core.MatOfPoint;
//import org.opencv.core.Size;
//import org.opencv.core.TermCriteria;
//import org.opencv.imgproc.Imgproc;
//import org.opencv.ml.Ml;
//import org.opencv.ml.SVM;
//import org.opencv.objdetect.HOGDescriptor;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import android.content.Context;
//import android.content.res.AssetManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.util.Log;
//
//public class Svmdemo {
//	SVM mSvm01;
//	SVM mSvm02;
//	img_feature mImg_feature;
//
//	/**
//	 * 获取图片地址列表
//	 * 
//	 * @param file
//	 * @return
//	 */
//	public ArrayList<String> imagePath(Context ctxDealFile, String path) {
//		ArrayList<String> pathFiles = new ArrayList<String>();
//		try {
//			// 获取指定目录下的所有文件名称
//			String str[] = ctxDealFile.getAssets().list(path);
//			String pathF;
//			if (str.length > 0) {
//				// 如果是目录
//				for (String string : str) {
//					pathF = path + "/" + string;
//					pathFiles.add(pathF);
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return pathFiles;
//	}
//
//	/**
//	 * 从Assets中读取图片
//	 * 
//	 * @param fileName
//	 * @return
//	 */
//	private Bitmap getImageFromAssetsFile(Context context, String fileName) {
//		Bitmap image = null;
//		AssetManager am = context.getResources().getAssets();
//		try {
//			InputStream is = am.open(fileName);
//			image = BitmapFactory.decodeStream(is);
//			is.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return image;
//
//	}
//
//	private void SvmTrain(Context context) {
//		ArrayList<String> pos_files = new ArrayList<String>();
//		ArrayList<String> neg_files = new ArrayList<String>();
//		pos_files = imagePath(context, "type01/train/pos01");
//		neg_files = imagePath(context, "type01/train/neg01");
//
//		int pos_num = pos_files.size();
//		int neg_num = neg_files.size();
//		Mat sampleFeatureMat = new Mat(pos_num + neg_num, 7500, CvType.CV_32SC1);
//		Mat sampleLabelMat = new Mat(pos_num + neg_num, 1, CvType.CV_32SC1);
//
//		// 读取正样本并提取特征
//		for (int i = 0; i < pos_files.size(); i++) {
//			Bitmap bitmap = getImageFromAssetsFile(context, pos_files.get(i));
//
//			Mat pos_srct = new Mat();
//			Mat pos_src = new Mat();
//			// 获取彩色图像所对应的像素数据
//			Utils.bitmapToMat(bitmap, pos_srct);
//			Size msize = new Size(75, 100);
//			Imgproc.resize(pos_srct, pos_src, msize);// 改变图片的大小为75*100
//
//			// HSV or RGB features
//			mImg_feature.HSV_split(pos_src);
//			Mat feature = new Mat();
//			feature = mImg_feature.H_feature;
//
//			/************************************************ 存储 **********************************************************/
//			int data_h[] = new int[7500];
//			feature.get(0, 0, data_h);
//			sampleFeatureMat.put(i, 0, data_h);
//
//			int data_l[] = new int[1];
//			data_l[0] = 1;
//			sampleLabelMat.put(i, 0, data_l);
//		}
//		// 读取负样本
//		for (int i = 0; i < neg_files.size(); i++) {
//			Bitmap bitmap = getImageFromAssetsFile(context, neg_files.get(i));
//
//			Mat neg_srct = new Mat();
//			Mat neg_src = new Mat();
//			// 获取彩色图像所对应的像素数据
//			Utils.bitmapToMat(bitmap, neg_srct);
//			Size msize = new Size(75, 100);
//			Imgproc.resize(neg_srct, neg_src, msize);// 改变图片的大小为75*100
//
//			// HSV or RGB features
//			mImg_feature.HSV_split(neg_src);
//			Mat feature = new Mat();
//			feature = mImg_feature.H_feature;
//
//			/************************************************ 存储 **********************************************************/
//			int data_h[] = new int[7500];
//			feature.get(0, 0, data_h);
//			sampleFeatureMat.put(i + pos_num, 0, data_h);
//
//			int data_l[] = new int[1];
//			data_l[0] = -1;
//			sampleLabelMat.put(i + pos_num, 0, data_l);
//		}
//
//		/*************************************** SVM训练 ***************************************************/
//		sampleFeatureMat.convertTo(sampleFeatureMat, CvType.CV_32FC1);
//
//		mSvm01 = SVM.create();
//		mSvm01.setType(SVM.C_SVC);
//		mSvm01.setKernel(SVM.LINEAR);
//		mSvm01.setC(1);
//		mSvm01.setCoef0(1.0);
//		mSvm01.setP(0);
//		mSvm01.setNu(0.5);
//		mSvm01.setTermCriteria(new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 1000, 0.01));
//		mSvm01.train(sampleFeatureMat, Ml.ROW_SAMPLE, sampleLabelMat);
//
//		Mat results = new Mat();
//		mSvm01.predict(sampleFeatureMat, results, 0);
//		float result_t[] = new float[pos_num + neg_num];
//		results.get(0, 0, result_t);
//		Log.i("Model Training", "Train success!");
//		// 获取支持向量
//		// Mat svmv = mSvm.getSupportVectors();
//		// float sv[] = new float[60 * 7500];
//		// svmv.get(0, 0, sv);
//		// svmv.reshape(1, 2);
//		// svmv.push_back(svmv);
//		// File datasetFile = new
//		// File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
//		// "model_hsv.xml");
//		// svm.save(datasetFile.getAbsolutePath());//此处72行报错
//
//	}
//
//	private void SvmTrain2(Context context) {
//		ArrayList<String> pos_files = new ArrayList<String>();
//		ArrayList<String> neg_files = new ArrayList<String>();
//		pos_files = imagePath(context, "type02/train/pos01");
//		neg_files = imagePath(context, "type02/train/neg01");
//
//		int pos_num = pos_files.size();
//		int neg_num = neg_files.size();
//		Mat sampleFeatureMat = new Mat(pos_num + neg_num, 14112, CvType.CV_32FC1);
//		Mat sampleLabelMat = new Mat(pos_num + neg_num, 1, CvType.CV_32SC1);
//
//		// 读取正样本并提取特征
//		for (int i = 0; i < pos_files.size(); i++) {
//			Bitmap bitmap = getImageFromAssetsFile(context, pos_files.get(i));
//
//			Mat pos_srct = new Mat();
//			Mat pos_src = new Mat();
//			// 获取彩色图像所对应的像素数据
//			Utils.bitmapToMat(bitmap, pos_srct);
//			Size msize = new Size(75, 100);
//			Imgproc.resize(pos_srct, pos_src, msize);// 改变图片的大小为75*100
//
//			if (pos_src.channels() == 4) {
//				Imgproc.cvtColor(pos_src, pos_src, Imgproc.COLOR_RGBA2RGB);
//			}
//			// int ch = pos_src.channels();
//			Size winsize = new Size(25, 50);
//			Size blocksize = new Size(10, 20);
//			Size blockStride = new Size(5, 10);
//			Size cellsize = new Size(10, 10);
//			Size winStride = new Size(8, 8);
//			Size padding = new Size(0, 0);
//			int nbins = 9;
//			HOGDescriptor hog = new HOGDescriptor(winsize, blocksize, blockStride, cellsize, nbins);
//			MatOfFloat descriptors = new MatOfFloat();
//			MatOfPoint locations = new MatOfPoint();
//			hog.compute(pos_src, descriptors, winStride, padding, locations);
//
//			/************************************************ 存储 **********************************************************/
//			float data_h[] = new float[14112];
//			descriptors.get(0, 0, data_h);
//			sampleFeatureMat.put(i, 0, data_h);
//
//			int data_l[] = new int[1];
//			data_l[0] = 1;
//			sampleLabelMat.put(i, 0, data_l);
//		}
//		// 读取负样本
//		for (int i = 0; i < neg_files.size(); i++) {
//			Bitmap bitmap = getImageFromAssetsFile(context, neg_files.get(i));
//
//			Mat neg_srct = new Mat();
//			Mat neg_src = new Mat();
//			// 获取彩色图像所对应的像素数据
//			Utils.bitmapToMat(bitmap, neg_srct);
//			Size msize = new Size(75, 100);
//			Imgproc.resize(neg_srct, neg_src, msize);// 改变图片的大小为75*100
//
//			if (neg_src.channels() == 4) {
//				Imgproc.cvtColor(neg_src, neg_src, Imgproc.COLOR_RGBA2RGB);
//			}
//			Size winsize = new Size(25, 50);
//			Size blocksize = new Size(10, 20);
//			Size blockStride = new Size(5, 10);
//			Size cellsize = new Size(10, 10);
//			Size winStride = new Size(8, 8);
//			Size padding = new Size(0, 0);
//			int nbins = 9;
//			HOGDescriptor hog = new HOGDescriptor(winsize, blocksize, blockStride, cellsize, nbins);
//			MatOfFloat descriptors = new MatOfFloat();
//			MatOfPoint locations = new MatOfPoint();
//			hog.compute(neg_src, descriptors, winStride, padding, locations);
//
//			/************************************************ 存储 **********************************************************/
//			float data_h[] = new float[14112];
//			descriptors.get(0, 0, data_h);
//			sampleFeatureMat.put(i + pos_num, 0, data_h);
//
//			int data_l[] = new int[1];
//			data_l[0] = -1;
//			sampleLabelMat.put(i + pos_num, 0, data_l);
//		}
//
//		/*************************************** SVM训练 ***************************************************/
//		sampleFeatureMat.convertTo(sampleFeatureMat, CvType.CV_32FC1);
//
//		mSvm02 = SVM.create();
//		mSvm02.setType(SVM.C_SVC);
//		mSvm02.setKernel(SVM.LINEAR);
//		mSvm02.setC(1);
//		mSvm02.setCoef0(1.0);
//		mSvm02.setP(0);
//		mSvm02.setNu(0.5);
//		mSvm02.setTermCriteria(new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 1000, 0.01));
//		mSvm02.train(sampleFeatureMat, Ml.ROW_SAMPLE, sampleLabelMat);
//		Mat results = new Mat();
//		mSvm02.predict(sampleFeatureMat, results, 0);
//		float result_t[] = new float[pos_num + neg_num];
//		results.get(0, 0, result_t);
//		Log.i("Model Training", "Train success!");
//		// 获取支持向量
//		// Mat svmv = mSvm.getSupportVectors();
//		// float sv[] = new float[60 * 7500];
//		// svmv.get(0, 0, sv);
//		// svmv.reshape(1, 2);
//		// svmv.push_back(svmv);
//		// File datasetFile = new
//		// File(Environment.getExternalStoragePublicDirectory(
//		// Environment.DIRECTORY_DOWNLOADS), "model_hsv.xml");
//		// svm.save(datasetFile.getAbsolutePath());//此处72行报错
//	}
//
//	private float[] SVMTest01(Context context) {
//		ArrayList<String> postest_files = new ArrayList<String>();
//		ArrayList<String> negtest_files = new ArrayList<String>();
//		postest_files = imagePath(context, "type01/test/pos01");
//		negtest_files = imagePath(context, "type01/test/neg01");
//
//		int postest_num = postest_files.size();
//		int negtest_num = negtest_files.size();
//		Mat sampletestFeatureMat = new Mat(postest_num + negtest_num, 7500, CvType.CV_32SC1);
//
//		// 读取正样本并提取特征
//		for (int i = 0; i < postest_files.size(); i++) {
//			Bitmap bitmap = getImageFromAssetsFile(context, postest_files.get(i));
//
//			Mat pos_srct = new Mat();
//			Mat pos_src = new Mat();
//			// 获取彩色图像所对应的像素数据
//			Utils.bitmapToMat(bitmap, pos_srct);
//			Size msize = new Size(75, 100);
//			Imgproc.resize(pos_srct, pos_src, msize);// 改变图片的大小为75*100
//
//			mImg_feature.HSV_split(pos_src);
//			Mat feature = new Mat();
//			feature = mImg_feature.H_feature;
//
//			/************************************************ 存储 **********************************************************/
//			int data_h[] = new int[7500];
//			feature.get(0, 0, data_h);
//			sampletestFeatureMat.put(i, 0, data_h);
//		}
//		// 读取负样本
//		for (int i = 0; i < negtest_files.size(); i++) {
//			Bitmap bitmap = getImageFromAssetsFile(context, negtest_files.get(i));
//
//			Mat neg_srct = new Mat();
//			Mat neg_src = new Mat();
//			// 获取彩色图像所对应的像素数据
//			Utils.bitmapToMat(bitmap, neg_srct);
//			Size msize = new Size(75, 100);
//			Imgproc.resize(neg_srct, neg_src, msize);// 改变图片的大小为75*100
//
//			mImg_feature.HSV_split(neg_src);
//			Mat feature = new Mat();
//			feature = mImg_feature.H_feature;
//
//			/************************************************ 存储 **********************************************************/
//			int data_h[] = new int[7500];
//			feature.get(0, 0, data_h);
//			sampletestFeatureMat.put(i + postest_num, 0, data_h);
//		}
//
//		/*************************************** SVM测试 ***************************************************/
//		sampletestFeatureMat.convertTo(sampletestFeatureMat, CvType.CV_32FC1);
//
//		Mat testresults = new Mat();
//		mSvm01.predict(sampletestFeatureMat, testresults, 0);
//		float result_test[] = new float[postest_num + negtest_num];
//		testresults.get(0, 0, result_test);
//		Log.i("Model testing", "test success!");
//		return result_test;
//	}
//
//	private float[] SVMTest02(Context context) {
//		ArrayList<String> postest_files = new ArrayList<String>();
//		ArrayList<String> negtest_files = new ArrayList<String>();
//		postest_files = imagePath(context, "type02/test/pos01");
//		negtest_files = imagePath(context, "type02/test/neg01");
//
//		int postest_num = postest_files.size();
//		int negtest_num = negtest_files.size();
//		Mat sampletestFeatureMat = new Mat(postest_num + negtest_num, 14112, CvType.CV_32FC1);
//
//		// 读取正样本并提取特征
//		for (int i = 0; i < postest_files.size(); i++) {
//			Bitmap bitmap = getImageFromAssetsFile(context, postest_files.get(i));
//
//			Mat pos_srct = new Mat();
//			Mat pos_src = new Mat();
//			// 获取彩色图像所对应的像素数据
//			Utils.bitmapToMat(bitmap, pos_srct);
//			Size msize = new Size(75, 100);
//			Imgproc.resize(pos_srct, pos_src, msize);// 改变图片的大小为75*100
//
//			if (pos_src.channels() == 4) {
//				Imgproc.cvtColor(pos_src, pos_src, Imgproc.COLOR_RGBA2RGB);
//			}
//			// int ch = pos_src.channels();
//			Size winsize = new Size(25, 50);
//			Size blocksize = new Size(10, 20);
//			Size blockStride = new Size(5, 10);
//			Size cellsize = new Size(10, 10);
//			Size winStride = new Size(8, 8);
//			Size padding = new Size(0, 0);
//			int nbins = 9;
//			HOGDescriptor hog = new HOGDescriptor(winsize, blocksize, blockStride, cellsize, nbins);
//			MatOfFloat descriptors = new MatOfFloat();
//			MatOfPoint locations = new MatOfPoint();
//			hog.compute(pos_src, descriptors, winStride, padding, locations);
//
//			/************************************************ 存储  **********************************************************/
//			float data_h[] = new float[14112];
//			descriptors.get(0, 0, data_h);
//			sampletestFeatureMat.put(i, 0, data_h);
//		}
//		// 读取负样本
//		for (int i = 0; i < negtest_files.size(); i++) {
//			Bitmap bitmap = getImageFromAssetsFile(context, negtest_files.get(i));
//
//			Mat neg_srct = new Mat();
//			Mat neg_src = new Mat();
//			// 获取彩色图像所对应的像素数据
//			Utils.bitmapToMat(bitmap, neg_srct);
//			Size msize = new Size(75, 100);
//			Imgproc.resize(neg_srct, neg_src, msize);// 改变图片的大小为75*100
//
//			if (neg_src.channels() == 4) {
//				Imgproc.cvtColor(neg_src, neg_src, Imgproc.COLOR_RGBA2RGB);
//			}
//			// int ch = pos_src.channels();
//			Size winsize = new Size(25, 50);
//			Size blocksize = new Size(10, 20);
//			Size blockStride = new Size(5, 10);
//			Size cellsize = new Size(10, 10);
//			Size winStride = new Size(8, 8);
//			Size padding = new Size(0, 0);
//			int nbins = 9;
//			HOGDescriptor hog = new HOGDescriptor(winsize, blocksize, blockStride, cellsize, nbins);
//			MatOfFloat descriptors = new MatOfFloat();
//			MatOfPoint locations = new MatOfPoint();
//			hog.compute(neg_src, descriptors, winStride, padding, locations);
//
//			/************************************************ 存储  **********************************************************/
//			float data_h[] = new float[14112];
//			descriptors.get(0, 0, data_h);
//			sampletestFeatureMat.put(i + postest_num, 0, data_h);
//		}
//
//		/*************************************** SVM测试  ***************************************************/
//		sampletestFeatureMat.convertTo(sampletestFeatureMat, CvType.CV_32FC1);
//
//		Mat testresults = new Mat();
//		mSvm02.predict(sampletestFeatureMat, testresults, 0);
//		float result_test[] = new float[postest_num + negtest_num];
//		testresults.get(0, 0, result_test);
//		Log.i("Model Testing", "test success!");
//		return result_test;
//
//	}
//}
