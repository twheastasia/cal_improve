package com.twh.testruby;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainListActivity extends Activity {

	private ListView listView;
	private Button btn_add;
	private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	private ArrayList<Object> result = new ArrayList<Object>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btn_add = (Button)findViewById(R.id.btn_add);
		listView = (ListView)findViewById(R.id.main_listview);
		getData();
		renderListView();
		
		btn_add.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getData();
				renderListView();
				
				int totalExperience = 37800;
				int array[][] = {
						  {9648, 1},
						  {9600, 1},
						  {8440, 0},
						  {6024, 1},
						  {5328, 1},
						  {4816, 1},
						  {4812, 1},
						  {4810, 1},
						  {4660, 0},
						  {2448, 1},
						  {400, 1}
				};
				cal_improve(array, totalExperience, 0, new ArrayList<Object>());
				System.out.print(result);
 				Log.i("tag", result.toString());
			}
		});
	}
	
	private void getData() {

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("item_position", "1");
		map.put("item_icon", R.drawable.btn_add);
		map.put("item_btn", "9600");
		map.put("item_count", "1");
		map.put("item_delete", R.drawable.btn_dele);
		list.add(map);

		map.put("item_position", "2");
		map.put("item_icon", R.drawable.btn_add);
		map.put("item_btn", "8600");
		map.put("item_count", "3");
		map.put("item_delete", R.drawable.btn_dele);
		list.add(map);
//
//		map.put("item_position", "3");
//		map.put("item_icon", R.drawable.btn_add);
//		map.put("item_btn", "7600");
//		map.put("item_count", "5");
//		map.put("item_delete", R.drawable.btn_add);
//		list.add(map);
		
	}
	
	private void renderListView()
	{
		SimpleAdapter adapter = new SimpleAdapter(this,list,R.layout.adapter_listview_item,
				new String[]{"item_position","item_icon","item_btn","item_count","item_delete"},
				new int[]{R.id.item_position,R.id.items_icon,R.id.item_btn,R.id.item_count,R.id.item_delete});
		listView.setAdapter(adapter);
	}
	
	@SuppressWarnings("unchecked")
	private void cal_improve(int arr[][], int total, int index, ArrayList<Object> last_result )
	{
		int t;
		ArrayList<Object> arrayList = new ArrayList<Object>();
		ArrayList<Object> tempList = new ArrayList<Object>();
		arrayList = (ArrayList<Object>) last_result.clone();
//		Iterator it = map.entrySet().iterator();
//		while (it.hasNext()){  
//			Entry entry = (Entry) it.next();  
//			// entry.getKey() 返回与此项对应的键  
//			// entry.getValue() 返回与此项对应的值  
//			System.out.print(entry.getValue());  
//		}  
		int minCount = Math.min(total/arr[index][0]+1,arr[index][1]);
		for(t=0 ; t <= minCount; t++){
			tempList = (ArrayList<Object>) arrayList.clone();
			tempList.add(t);
			Log.i("new", tempList.toString());
			if(index == arr.length-1){
				tempList.add(-(total-arr[index][0]*t));
				Log.i("final", tempList.toString());
				result.add(tempList);
				continue;
			}else{
				cal_improve(arr, (total- arr[index][0]*t), index+1, tempList);
			}
		}


		
	}
}
