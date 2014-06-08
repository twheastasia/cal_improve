package com.twh.testruby;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainListActivity extends Activity {

	private static int MIN_EXP = 0;
	private static int MAX_EXP = 4000;
	
	private ListView listView;
	private Button btn_add;
	private Button showResultBtn;
	private EditText expEditText;
	private int cellLength;
	private AlertDialog loadingDialog;
	private Thread mThread;
	private String resultStr;
	private MyAdapter listAdapter; 
	private List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
	private ArrayList<Object> result;
	private int[][] finalResult = null;
	private int totalExperience;
	private int itemArray[][] = {{400, 3},{200,1}};
	private String itemExp[] = new String[] { "9600", "8000", "6440", "4500", "3332", "2048", "1024", "512", "400" };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		showResultBtn = (Button)findViewById(R.id.showResult);
		btn_add = (Button)findViewById(R.id.btn_add);
		listView = (ListView)findViewById(R.id.main_listview);
		expEditText = (EditText)findViewById(R.id.editText1);
		
		getData(1);
		listAdapter = new MyAdapter(this);
		renderListView();
		
		btn_add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getData(listData.size()+1);
				renderListView();
			}
		});
		
		showResultBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(expEditText.getText().toString().isEmpty()){
					showInfo("请输入升级所需经验值");
				}else{
					createLoading();
					loadingDialog.show();
					totalExperience = Integer.parseInt(expEditText.getText().toString());
					mThread = null;
					mThread = new Thread(runnable);
					mThread.start();
				}
				

			}
		});
	}
	
	private Handler mHandler = new Handler() {
		// 重写handleMessage()方法，此方法在UI线程运行
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				loadingDialog.dismiss();
				showDialog(resultStr);
				break;
			case 2:
				break;
			}
		}
	};

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			result = new ArrayList<Object>();
			finalResult = null;
			resultStr = "";
			cal_improve(itemArray, totalExperience, 0, new ArrayList<Object>());
			finalResult = screenArray(reorderArray(arrayListToArray(result)), MIN_EXP, MAX_EXP);
			resultStr = resultString(finalResult, 3);
			mHandler.obtainMessage(1).sendToTarget();
		}
	};
	
	//add data of listview
	private void getData(int pos) {

//		int Math.random()
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("item_position", ""+pos);
		map.put("item_icon", R.drawable.evolution1);
		map.put("item_btn", "9600");
		map.put("item_count", "1");
		map.put("item_delete", R.drawable.btn_dele);
		listData.add(map);
		
	}
	
	//refresh the listview
	private void renderListView()
	{
		renderListData();
		listView.setAdapter(listAdapter);
	}
	
	private void renderListData()
	{
		itemArray = new int[listData.size()][2];
		Object tempListData[] = listData.toArray();
		Map<String, Object> tempListMap = new HashMap<String, Object>();
		int i;
		for(i=0 ; i< listData.size(); i++){
			tempListMap = (Map<String, Object>) tempListData[i];
			itemArray[i][0] = Integer.parseInt((String) tempListMap.get("item_btn"));
			itemArray[i][1] = Integer.parseInt((String) tempListMap.get("item_count"));
		}
		cellLength = itemArray.length +1;
	}
	
	//figure out all the case of using items, not consider whether the experience is beyond or not
	@SuppressWarnings("unchecked")
	private void cal_improve(int arr[][], int total, int index, ArrayList<Object> last_result )
	{
		int t;
		ArrayList<Object> arrayList = new ArrayList<Object>();
		ArrayList<Object> tempList = new ArrayList<Object>();
		arrayList = (ArrayList<Object>) last_result.clone();
		//单使用这个材料所需要的个数，和这个材料实际拥有的个数，两者之间取一个最小值
		int minCount = Math.min(total/arr[index][0]+1,arr[index][1]);
		for(t=0 ; t <= minCount; t++){
			tempList = (ArrayList<Object>) arrayList.clone();
			tempList.add(t);
			if(index == arr.length-1){
				//最后一个，把这一材料组合得到的经验值和所需的经验做比较，负数表示还缺多少，正数就是经验值太多了
				tempList.add(-(total-arr[index][0]*t));
				result.add(tempList);
				continue;
			}else{
				//递归嵌套
				cal_improve(arr, (total- arr[index][0]*t), index+1, tempList);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private int[][] reorderArray(int last_result[][])
	{
		//如果参数为空时就返回null
		if(last_result.length == 0){
			return null;
		}
		
		//下面这三个都属于临时变量
		int temp[] = null;
		int eachResult[]; 
		int eachNextResult[];
		int index;
		int index2;
		
		int resultAfterReorder[][] = new int[last_result.length][cellLength];
		resultAfterReorder = last_result.clone();
		temp = new int[cellLength];
		eachResult = new int[cellLength];
		eachNextResult = new int[cellLength];
		
		for(index = 0; index < resultAfterReorder.length;index++){
			for(index2 = 0; index2 < resultAfterReorder.length-1; index2++ ){
				//把ArrayList先转成Array，然后比较数组里最后一个值的大小，把数值大的排前面
				eachResult = resultAfterReorder[index2];
				eachNextResult = resultAfterReorder[index2 + 1];
				int a = eachResult[cellLength-1];
				int b = eachNextResult[cellLength-1];
				if( a < b){
					temp = resultAfterReorder[index2];
					resultAfterReorder[index2] = resultAfterReorder[index2 + 1];
					resultAfterReorder[index2 + 1] = temp;
				}
			}
		}
		return resultAfterReorder;
	}
	
	//筛选数据
	private int[][] screenArray(int array[][], int minExp, int maxExp)
	{
		int afterScreenArray[][];
		int index;
		int temp[] = new int[cellLength];
		ArrayList<Object> arraylist = new ArrayList<Object>();
		for(index = 0; index < array.length; index++){
			temp = array[index];
			if((Integer) temp[cellLength-1]>= minExp && (Integer) temp[cellLength-1]<= maxExp){
				arraylist.add(temp);
			}
		}
		if(arraylist.size() == 0 || arraylist == null){
			return null;
		}else{
			Collections.reverse(arraylist);
			afterScreenArray = new int[arraylist.size()][cellLength];
			Object[] tempArrayList = new Object[arraylist.size()];
			int[] tempArrayListCells = new int[cellLength];
			tempArrayList = arraylist.toArray();
			for(int i =0;i< tempArrayList.length;i++){
				tempArrayListCells = (int[]) tempArrayList[i];
				for(int j=0;j<tempArrayListCells.length;j++ ){
					afterScreenArray[i][j] = (Integer) tempArrayListCells[j];
				}
			}
		}
		return afterScreenArray;
		
	}
	
	private int[][] arrayListToArray(ArrayList<Object> arraylist)
	{
		int result[][] = new int[arraylist.size()][cellLength]; 
		ArrayList<Object> templist = new ArrayList<Object>();
		Object temp[] = new Object[cellLength];
		int i,j;
		for(i=0; i<arraylist.size(); i++){
			templist =(ArrayList<Object>) ((arraylist.toArray())[i]);
			temp = templist.toArray();
			for(j=0;j<temp.length;j++){
				result[i][j] = (Integer) temp[j];
			}
		}
		return result;
	}
	
	protected void showDialog(String resultStr) 
	{
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(resultStr);
		builder.setTitle("结果:");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
		
	}
	
	private void createLoading()
	{
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.loadingdlg, (ViewGroup) findViewById(R.id.loadingdlg));
		AlertDialog.Builder builder = new Builder(this);
		builder.setView(layout)
				.setTitle("loading...")
				.setCancelable(false);
		loadingDialog = builder.create();
	}
	
	private void showChooseItemDialog(final int pos, final String text)
	{
		new AlertDialog.Builder(this).setTitle("单选框").setIcon(
				android.R.drawable.ic_dialog_info).setSingleChoiceItems(
						itemExp, 0,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("item_position", ""+pos);
								map.put("item_icon", R.drawable.btn_add);
								map.put("item_btn", itemExp[which]);
								map.put("item_count", text);
								map.put("item_delete", R.drawable.btn_dele);
								listData.set(pos, map);
								itemArray[pos][1] = Integer.parseInt(itemExp[which]);
								renderListView();
							}
						})
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub

								arg0.dismiss();
							}
						})
						.setNegativeButton("取消", null)
						.show();
	}
	
	private String resultString(int[][] array, int resultCount)
	{
		if(array == null){
			return "好像没有找到什么经济实惠的解决方法啊！";
		}
		String resultStrs = "";
		String itemResultStr = "";
		int arrayCellLength = array[0].length;
		int i,j;
		for(i=0; i< Math.min(array.length, resultCount);i++){
			itemResultStr = "";
			for(j=0; j<arrayCellLength-1; j++){
				itemResultStr += " " + itemArray[j][0] + " * " + array[i][j] + " ";
			}
			resultStrs += "wasted: " + array[i][arrayCellLength-1] +", "+ itemResultStr + ";\n";
		}
		return resultStrs;
	}
	
	//自定义listview的适配器
	public class MyAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		
		public MyAdapter(Context context){
			this.mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listData.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder = null;
			if (convertView == null) {
				
				holder=new ViewHolder();  
				
				convertView = mInflater.inflate(R.layout.adapter_listview_item, null);
				holder.indexTV = (TextView)convertView.findViewById(R.id.item_position);
				holder.itemImg = (ImageView)convertView.findViewById(R.id.items_icon);
				holder.itemBtn = (Button)convertView.findViewById(R.id.item_btn);
				holder.countET = (EditText)convertView.findViewById(R.id.item_count);
				holder.itemDel = (Button)convertView.findViewById(R.id.item_delete);
				convertView.setTag(holder);
				
			}else {
				
				holder = (ViewHolder)convertView.getTag();
			}
			
			holder.indexTV.setText((String)listData.get(position).get("item_position"));
			holder.itemImg.setBackgroundResource((Integer)listData.get(position).get("item_icon"));
			holder.itemBtn.setText((String)listData.get(position).get("item_btn"));
			holder.countET.setText((String)listData.get(position).get("item_count"));
			holder.itemDel.setBackgroundResource((Integer)listData.get(position).get("item_delete"));
			
//			final String editTextContent = holder.countET.getText().toString();
			holder.itemBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showChooseItemDialog(position, itemArray[position][1]+"");
//					showInfo("click which one "+ position);	
				}
			});
			
			holder.itemDel.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
//					showInfo("delete which one "+ position);
					listData.remove(position);
					renderListView();
				}
			});
			
			holder.countET.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
					// TODO Auto-generated method stub
			
				}
				
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
						int arg3) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void afterTextChanged(Editable arg0) {
					// TODO Auto-generated method stub
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("item_position", ""+position);
					map.put("item_icon", R.drawable.btn_add);
					map.put("item_btn", "9600");
					map.put("item_count", arg0.toString());
					map.put("item_delete", R.drawable.btn_dele);
					listData.set(position, map);
					if(arg0.toString().isEmpty()){
						itemArray[position][1] = 1;
					}else{
						itemArray[position][1] = Integer.parseInt(arg0.toString());
					}
				}
			});
			return convertView;
		}
	}
	
	public final class ViewHolder{
		public TextView indexTV;
		public ImageView itemImg;
		public Button itemBtn;
		public EditText countET;
		public Button itemDel;
	}
	
	private void showInfo(String info)
	{
		Toast.makeText(this, info, Toast.LENGTH_LONG).show();
	}
	
	
}
