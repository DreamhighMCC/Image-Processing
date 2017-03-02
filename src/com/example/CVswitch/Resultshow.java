package com.example.CVswitch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;

public class Resultshow extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.resultshow);
		SimpleAdapter adapter = new SimpleAdapter(this, getData(), R.layout.resultshow, new String[] { "img", "info" },
				new int[] { R.id.img, R.id.info });
		setListAdapter(adapter);
		adapter.setViewBinder(new ListViewBinder());
		
		
	}
	private class ListViewBinder implements ViewBinder {  	  
        @Override  
        public boolean setViewValue(View view, Object data,  
                String textRepresentation) {  
            // TODO Auto-generated method stub  
            if((view instanceof ImageView) && (data instanceof Bitmap)) {  
                ImageView imageView = (ImageView) view;  
                Bitmap bmp = (Bitmap) data;  
                imageView.setImageBitmap(bmp);  
                return true;  
            }  
            return false;  
        }  
	}

	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		Bitmap bitmap = Pictureshow.bmp_src;
		float result[][] = Pictureshow.result;

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("img", bitmap);
		list.add(map);

		List<String> stringlist = new ArrayList<String>();
		String resultstring;
		String[] string = new String[10];
		//int i = (int) result[i][0];
		float pre = result[0][0];
		int num=0;
		for (int i = 0;result[i][0] > 0; i++) {
			if (result[i][0] == pre) {
				if(result[i][1]==1){
					string[num]=" 关   ";
					num=num+1;
					String rString = " 关   ";
					stringlist.add(rString);
				}else{
					string[num]=" 关   ";
					num=num+1;
					String rString = " 开  ";
					stringlist.add(rString);
				}
			} else {
				num=0;
				pre = result[i][0];
				map = new HashMap<String, Object>();
				map.put("info", stringlist.toString());
				list.add(map);
				stringlist = new ArrayList<String>();
				if(result[i][1]==1){
					string[num]=" 关   ";
					num=num+1;
					String rString = " 关   ";
					stringlist.add(rString);
				}else{
					string[num]=" 关   ";
					num=num+1;
					String rString = " 开  ";
					stringlist.add(rString);
				}
			}
		}
		map = new HashMap<String, Object>();
		map.put("info", stringlist.toString());
		list.add(map);

		return list;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.resultshow, menu);
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
