package com.example.CVswitch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.Type;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvSVMParams;
import org.opencv.ml.Ml;
import org.opencv.objdetect.HOGDescriptor;

public class MainActivity extends Activity implements OnClickListener {

	private static final int TAKE_PICTURE = 0;
	private static final int CHOOSE_PICTURE = 1;
	private Button btncamara;
	private Button btnpicture;
	private ImageView imageView;
	public static Bitmap bmp;
	private Bitmap bmp_src;
	private int width;
	private int height;
	int split_info[][] = new int[50][5];// 所有分割图片的位置信息[开关行数,x,y,height,width]

	private img_feature mImg_feature = new img_feature();
	private CvSVM mSvm;
	private final String IMAGE_TYPE = "image/*";

	private static final String TAG = "Switch MainActivity";

	// OpenCV类库加载并初始化成功后的回调函数，在此我们不进行任何操作
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				// System.loadLibrary("opencv_java");
				Log.i(TAG, "成功加载");
				if (OpenCVLoader.initDebug()) {
					System.loadLibrary("opencv_java");// load other libraries
				}
				break;
			}
			default: {
				super.onManagerConnected(status);
				Log.i(TAG, "加载失败");
				break;
			}
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		// 通过OpenCV引擎服务加载并初始化OpenCV类库，所谓OpenCV引擎服务即是
		mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		// OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,
		// getApplicationContext(), mLoaderCallback);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btncamara = (Button) findViewById(R.id.btn_camara);
		btnpicture = (Button) findViewById(R.id.btn_picture);
		imageView = (ImageView) findViewById(R.id.image_view);
		Log.i(TAG, "initUI sucess...");

		// 将图像加载程序中并进行显示
		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.test1);
//		img_split img_splitt = new img_split();
//		img_splitt.split_hist(bmp);
		width = 800;
		height = 450;
		bmp_src = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		bmp_src = zoomImage(bmp, 800, 450);
		width = bmp_src.getWidth();
		height = bmp_src.getHeight();
		Log.i("WH", "width" + width + ";height" + height);
		imageView.setImageBitmap(bmp_src);

		btncamara.setOnClickListener(this);
		btnpicture.setOnClickListener(this);

	}

	@SuppressLint("ShowToast")
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.btn_camara:
			Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			Uri imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "image.jpg"));
			openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			startActivityForResult(openCameraIntent, TAKE_PICTURE);
			Log.i("success", "tiaozhuang");
			break;

		case R.id.btn_picture:
			Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
			getAlbum.setType(IMAGE_TYPE);
			startActivityForResult(getAlbum, CHOOSE_PICTURE);
			break;

		default:
			break;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) { // 此处的 RESULT_OK 是系统自定义得一个常量
			// Log.e(TAG,"ActivityResult resultCode error");
			return;
		}else{
			Intent intent=null;
			Bitmap newBitmap=null;
			switch (requestCode) {
			case TAKE_PICTURE:
				bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+"/image.jpg");
				newBitmap = ImageTools.zoomBitmap(bmp, 800,(int) bmp.getWidth()*bmp.getHeight()/800);
				intent = new Intent(MainActivity.this, Pictureshow.class);
				startActivity(intent);
				break;

			case CHOOSE_PICTURE:
				ContentResolver resolver = getContentResolver();
				if (requestCode == CHOOSE_PICTURE) {
					try {
						Uri originalUri = data.getData(); // 获得图片的uri
						bmp = MediaStore.Images.Media.getBitmap(resolver, originalUri); // 显得到bitmap图片
						newBitmap = ImageTools.zoomBitmap(bmp, 800,(int) bmp.getWidth()*bmp.getHeight()/800);
						intent = new Intent(MainActivity.this, Pictureshow.class);
						startActivity(intent);
					} catch (IOException e) {
						Log.e("Lostinai", e.toString());
					}

				}

				break;

			default:
				break;
			}
		}
		

	}

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


	/**
	 * 获取图片地址列表
	 * 
	 * @param file
	 * @return
	 */
	public ArrayList<String> imagePath(Context ctxDealFile, String path) {
		ArrayList<String> pathFiles = new ArrayList<String>();
		try {
			// 获取指定目录下的所有文件名称
			String str[] = ctxDealFile.getAssets().list(path);
			String pathF;
			if (str.length > 0) {
				// 如果是目录
				for (String string : str) {
					pathF = path + "/" + string;
					pathFiles.add(pathF);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pathFiles;
	}

	/**
	 * 从Assets中读取图片
	 * 
	 * @param fileName
	 * @return
	 */
	private Bitmap getImageFromAssetsFile(String fileName) {
		Bitmap image = null;
		AssetManager am = getResources().getAssets();
		try {
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;

	}

	private void SvmTrain() {
		ArrayList<String> pos_files = new ArrayList<String>();
		ArrayList<String> neg_files = new ArrayList<String>();
		pos_files = imagePath(this, "type01/train/pos01");
		neg_files = imagePath(this, "type01/train/neg01");

		int pos_num = pos_files.size();
		int neg_num = neg_files.size();
		Mat sampleFeatureMat = new Mat(pos_num + neg_num, 7500, CvType.CV_32SC1);
		Mat sampleLabelMat = new Mat(pos_num + neg_num, 1, CvType.CV_32SC1);

		// 读取正样本并提取特征
		for (int i = 0; i < pos_files.size(); i++) {
			Bitmap bitmap = getImageFromAssetsFile(pos_files.get(i));

			Mat pos_srct = new Mat();
			Mat pos_src = new Mat();
			// 获取彩色图像所对应的像素数据
			Utils.bitmapToMat(bitmap, pos_srct);
			Size msize = new Size(75, 100);
			Imgproc.resize(pos_srct, pos_src, msize);// 改变图片的大小为75*100

			mImg_feature.HSV_split(pos_src);
			mImg_feature.RGB_split(pos_src);
			Mat feature = new Mat();
			feature = mImg_feature.G_feature;

			/************************************************ 存储 **********************************************************/
			int data_h[] = new int[7500];
			feature.get(0, 0, data_h);
			sampleFeatureMat.put(i, 0, data_h);

			int data_l[] = new int[1];
			data_l[0] = 1;
			sampleLabelMat.put(i, 0, data_l);
		}
		// 读取负样本
		for (int i = 0; i < neg_files.size(); i++) {
			Bitmap bitmap = getImageFromAssetsFile(neg_files.get(i));

			Mat neg_srct = new Mat();
			Mat neg_src = new Mat();
			// 获取彩色图像所对应的像素数据
			Utils.bitmapToMat(bitmap, neg_srct);
			Size msize = new Size(75, 100);
			Imgproc.resize(neg_srct, neg_src, msize);// 改变图片的大小为75*100

			mImg_feature.HSV_split(neg_src);
			mImg_feature.RGB_split(neg_src);
			Mat feature = new Mat();
			feature = mImg_feature.G_feature;

			/************************************************ 存储 **********************************************************/
			int data_h[] = new int[7500];
			feature.get(0, 0, data_h);
			sampleFeatureMat.put(i + pos_num, 0, data_h);

			int data_l[] = new int[1];
			data_l[0] = -1;
			sampleLabelMat.put(i + pos_num, 0, data_l);
		}

		/*************************************** SVM训练 ***************************************************/
		sampleFeatureMat.convertTo(sampleFeatureMat, CvType.CV_32FC1);

		TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 1000, 0.01);
		CvSVMParams params = new CvSVMParams();
		params.set_svm_type(CvSVM.C_SVC);
		params.set_kernel_type(CvSVM.LINEAR);
		params.set_degree(0);
		params.set_gamma(1);
		params.set_coef0(0);
		params.set_C(1);
		params.set_nu(0);
		params.set_p(0);
		params.set_term_crit(criteria);
		Mat varIdx = new Mat();
		Mat sampleIdx = new Mat();
		mSvm = new CvSVM();
		mSvm.train(sampleFeatureMat, sampleLabelMat, varIdx, sampleIdx, params);
		// String string = Environment.getExternalStorageState();
		// File datasetFile = new
		// File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
		// "model.xml");
		// String filepath = "/sdcard/Model/model.xml";
		// mSvm.save(filepath);// 此处72行报错

		Mat results = new Mat();
		mSvm.predict_all(sampleFeatureMat, results);
		float result_t[] = new float[pos_num + neg_num];
		results.get(0, 0, result_t);
		Log.i(TAG, "Train success!");
		// 获取支持向量
		// Mat svmv = mSvm.getSupportVectors();
		// float sv[] = new float[60 * 7500];
		// svmv.get(0, 0, sv);
		// svmv.reshape(1, 2);
		// svmv.push_back(svmv);

	}

	private void SvmTrain2() {
		ArrayList<String> pos_files = new ArrayList<String>();
		ArrayList<String> neg_files = new ArrayList<String>();
		pos_files = imagePath(this, "type02/train/pos01");
		neg_files = imagePath(this, "type02/train/neg01");

		int pos_num = pos_files.size();
		int neg_num = neg_files.size();
		Mat sampleFeatureMat = new Mat(pos_num + neg_num, 14112, CvType.CV_32FC1);
		Mat sampleLabelMat = new Mat(pos_num + neg_num, 1, CvType.CV_32SC1);

		// 读取正样本并提取特征
		for (int i = 0; i < pos_files.size(); i++) {
			Bitmap bitmap = getImageFromAssetsFile(pos_files.get(i));

			Mat pos_srct = new Mat();
			Mat pos_src = new Mat();
			// 获取彩色图像所对应的像素数据
			Utils.bitmapToMat(bitmap, pos_srct);
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

			/************************************************ 存储 **********************************************************/
			float data_h[] = new float[14112];
			descriptors.get(0, 0, data_h);
			sampleFeatureMat.put(i, 0, data_h);

			int data_l[] = new int[1];
			data_l[0] = 1;
			sampleLabelMat.put(i, 0, data_l);
		}
		// 读取负样本
		for (int i = 0; i < neg_files.size(); i++) {
			Bitmap bitmap = getImageFromAssetsFile(neg_files.get(i));

			Mat neg_srct = new Mat();
			Mat neg_src = new Mat();
			// 获取彩色图像所对应的像素数据
			Utils.bitmapToMat(bitmap, neg_srct);
			Size msize = new Size(75, 100);
			Imgproc.resize(neg_srct, neg_src, msize);// 改变图片的大小为75*100

			if (neg_src.channels() == 4) {
				Imgproc.cvtColor(neg_src, neg_src, Imgproc.COLOR_RGBA2RGB);
			}
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
			hog.compute(neg_src, descriptors, winStride, padding, locations);

			/************************************************ 存储 **********************************************************/
			float data_h[] = new float[14112];
			descriptors.get(0, 0, data_h);
			sampleFeatureMat.put(i + pos_num, 0, data_h);

			int data_l[] = new int[1];
			data_l[0] = -1;
			sampleLabelMat.put(i + pos_num, 0, data_l);
		}

		/*************************************** SVM训练 ***************************************************/
		sampleFeatureMat.convertTo(sampleFeatureMat, CvType.CV_32FC1);

		TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 1000, 0.01);
		CvSVMParams params = new CvSVMParams();
		params.set_svm_type(CvSVM.C_SVC);
		params.set_kernel_type(CvSVM.LINEAR);
		params.set_degree(0);
		params.set_gamma(1);
		params.set_coef0(0);
		params.set_C(1);
		params.set_nu(0);
		params.set_p(0);
		params.set_term_crit(criteria);
		Mat varIdx = new Mat();
		Mat sampleIdx = new Mat();
		mSvm.train(sampleFeatureMat, sampleLabelMat, varIdx, sampleIdx, params);
		String filePath1 = "/sdcard/Test1/";
		String fileName1 = "Model.xml";
		// mSvm.save(filePath1, fileName1);
		// mSvm.save("/sdcard/Model");
		int tt = mSvm.get_support_vector_count();
		int tt2 = mSvm.C_SVC;

		// mSvm = SVM.create();
		// mSvm.setType(CvSVM.C_SVC);
		// mSvm.setKernel(CvSVM.LINEAR);
		// mSvm.setC(1);
		// mSvm.setCoef0(1.0);
		// mSvm.setP(0);
		// mSvm.setNu(0.5);
		// mSvm.setTermCriteria(new TermCriteria(TermCriteria.EPS +
		// TermCriteria.MAX_ITER, 1000, 0.01));
		// mSvm.train(sampleFeatureMat, Ml.ROW_SAMPLE, sampleLabelMat);
		Mat results = new Mat();
		mSvm.predict_all(sampleFeatureMat, results);
		float result_t[] = new float[pos_num + neg_num];
		results.get(0, 0, result_t);
		Log.i(TAG, "Train success!");
		// File datasetFile = new
		// File(Environment.getExternalStoragePublicDirectory(
		// Environment.DIRECTORY_DOWNLOADS), "model_hsv.xml");
		// svm.save(datasetFile.getAbsolutePath());//此处72行报错

	}

	private float[] SvmTest() {
		ArrayList<String> pos_files = new ArrayList<String>();
		ArrayList<String> neg_files = new ArrayList<String>();
		pos_files = imagePath(this, "type01/test/pos01");
		neg_files = imagePath(this, "type01/test/neg01");

		int pos_num = pos_files.size();
		int neg_num = neg_files.size();
		Mat sampleFeatureMat = new Mat(pos_num + neg_num, 7500, CvType.CV_32SC1);
		// 读取正样本并提取特征
		for (int i = 0; i < pos_files.size(); i++) {
			Bitmap bitmap = getImageFromAssetsFile(pos_files.get(i));

			Mat pos_srct = new Mat();
			Mat pos_src = new Mat();
			// 获取彩色图像所对应的像素数据
			Utils.bitmapToMat(bitmap, pos_srct);
			Size msize = new Size(75, 100);
			Imgproc.resize(pos_srct, pos_src, msize);// 改变图片的大小为75*100

			mImg_feature.HSV_split(pos_src);
			mImg_feature.RGB_split(pos_src);
			Mat feature = new Mat();
			feature = mImg_feature.G_feature;
			// Mat H_feat = new Mat();
			// Mat S_feat = new Mat();
			// Mat V_feat = new Mat();
			// H_feat = mImg_feature.H_feature;
			// S_feat = mImg_feature.S_feature;
			// V_feat = mImg_feature.V_feature;

			/************************************************ 存储 **********************************************************/
			int data_h[] = new int[7500];
			feature.get(0, 0, data_h);
			sampleFeatureMat.put(i, 0, data_h);
		}
		// 读取负样本
		for (int i = 0; i < neg_files.size(); i++) {
			Bitmap bitmap = getImageFromAssetsFile(neg_files.get(i));

			Mat neg_srct = new Mat();
			Mat neg_src = new Mat();
			// 获取彩色图像所对应的像素数据
			Utils.bitmapToMat(bitmap, neg_srct);
			Size msize = new Size(75, 100);
			Imgproc.resize(neg_srct, neg_src, msize);// 改变图片的大小为75*100

			mImg_feature.HSV_split(neg_src);
			mImg_feature.RGB_split(neg_src);
			Mat feature = new Mat();
			feature = mImg_feature.G_feature;
			// Mat H_feat = new Mat();
			// Mat S_feat = new Mat();
			// Mat V_feat = new Mat();
			// H_feat = mImg_feature.H_feature;
			// S_feat = mImg_feature.S_feature;
			// V_feat = mImg_feature.V_feature;

			/************************************************ 存储 **********************************************************/
			int data_h[] = new int[7500];
			feature.get(0, 0, data_h);
			sampleFeatureMat.put(i + pos_num, 0, data_h);
		}

		/*************************************** SVM测试 ***************************************************/
		sampleFeatureMat.convertTo(sampleFeatureMat, CvType.CV_32FC1);
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

			// svm = new
			// CvSVM(Long.parseLong(mSvmFile.getAbsolutePath()));//这个是出错的<pre
			// name="code" class="java">
			mSvm = new CvSVM();
			mSvm.load(mSvmFile.getAbsolutePath());
		} catch (Exception e) {
			Log.e("OpenCVActivity", "Error loading xml", e);
		}
		Mat results = new Mat();
		mSvm.predict_all(sampleFeatureMat, results);
		float result_t[] = new float[pos_num + neg_num];
		results.get(0, 0, result_t);
		return result_t;
	}

	private float[] SvmTest2() {
		ArrayList<String> postest_files = new ArrayList<String>();
		ArrayList<String> negtest_files = new ArrayList<String>();
		postest_files = imagePath(this, "type02/test/pos01");
		negtest_files = imagePath(this, "type02/test/neg01");

		int postest_num = postest_files.size();
		int negtest_num = negtest_files.size();
		Mat sampletestFeatureMat = new Mat(postest_num + negtest_num, 14112, CvType.CV_32FC1);

		// 读取正样本并提取特征
		for (int i = 0; i < postest_files.size(); i++) {
			Bitmap bitmap = getImageFromAssetsFile(postest_files.get(i));

			Mat pos_srct = new Mat();
			Mat pos_src = new Mat();
			// 获取彩色图像所对应的像素数据
			Utils.bitmapToMat(bitmap, pos_srct);
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
			sampletestFeatureMat.put(i, 0, data_h);
		}
		// 读取负样本
		for (int i = 0; i < negtest_files.size(); i++) {
			Bitmap bitmap = getImageFromAssetsFile(negtest_files.get(i));

			Mat neg_srct = new Mat();
			Mat neg_src = new Mat();
			// 获取彩色图像所对应的像素数据
			Utils.bitmapToMat(bitmap, neg_srct);
			Size msize = new Size(75, 100);
			Imgproc.resize(neg_srct, neg_src, msize);// 改变图片的大小为75*100

			if (neg_src.channels() == 4) {
				Imgproc.cvtColor(neg_src, neg_src, Imgproc.COLOR_RGBA2RGB);
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
			hog.compute(neg_src, descriptors, winStride, padding, locations);

			/************************************************
			 * 存储
			 **********************************************************/
			float data_h[] = new float[14112];
			descriptors.get(0, 0, data_h);
			sampletestFeatureMat.put(i + postest_num, 0, data_h);
		}

		/***************************************
		 * SVM测试
		 ***************************************************/
		sampletestFeatureMat.convertTo(sampletestFeatureMat, CvType.CV_32FC1);

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

			// svm = new
			// CvSVM(Long.parseLong(mSvmFile.getAbsolutePath()));//这个是出错的<pre
			// name="code" class="java">
			mSvm = new CvSVM();
			mSvm.load(mSvmFile.getAbsolutePath());
		} catch (Exception e) {
			Log.e("OpenCVActivity", "Error loading xml", e);
		}

		Mat testresults = new Mat();
		// mSvm=new CvSVM();
		// mSvm.load("model/Model2.xml");
		int t = mSvm.get_var_count();
		mSvm.predict_all(sampletestFeatureMat, testresults);
		// mSvm.predict(sampletestFeatureMat, testresults, 0);
		float result_test[] = new float[postest_num + negtest_num];
		testresults.get(0, 0, result_test);
		Log.i(TAG, "test success!");
		return result_test;

	}

	private float[] recognitiontype1(Bitmap bitmap, int info[][]) {
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
		float[] result = new float[50];
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

			result[i] = mSvm.predict(feature);
			Log.i(TAG, "test success!");
		}
		return result;

	}

	private float[] recognitiontype2(Bitmap bitmap, int info[][]) {
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
		float[] result = new float[50];
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

			int t = mSvm.get_var_count();
			result[i] = mSvm.predict(descriptors);
			Log.i(TAG, "test success!");
		}
		return result;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
