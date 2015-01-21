package com.twh.testruby;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

	private static String SDCARD = "/sdcard/";
	private static String DIR_NAME = "figureExp";
	private static final int SETTING = Menu.FIRST;
	private static final int ABOUT = Menu.FIRST+1;
	
	private int MIN_EXP = 0;
	private int MAX_EXP = 400;
	private ListView listView;
	private Button btn_add;
	private Button showResultBtn;
	private EditText expEditText;
	private EditText minET;
	private EditText maxET;
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
	private String itemExp[] = new String[] { "115200", "28800", "9648", "9600", "8440", "7232", 
			"6024", "5328", "4816", "4800",  "4660", "3992", "3324", "2656", "2448", "2140", "1832",
			"1524", "1216", "400"};
	
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
				showDialog(resultStr, "计算结果：");
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		menu.add(0, SETTING, 0, "设置");
		menu.add(0, ABOUT, 0, "关于");
		
		return true;
	}
	
	
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case SETTING:
			setMinMaxExp();
			return true;
		case ABOUT:
			showAbout();
			return true;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void setMinMaxExp()
	{
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.setexp_arrange,
				(ViewGroup) findViewById(R.id.setexp));
		minET = (EditText)layout.findViewById(R.id.edit_min);
		maxET = (EditText)layout.findViewById(R.id.edit_max);

		new AlertDialog.Builder(this)
		.setTitle("设置")
		.setView(layout)
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
			    try {
                    Field field = arg0.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(arg0, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
				int min, max;
				if(!minET.getText().toString().isEmpty() && !maxET.getText().toString().isEmpty()){
					min = Integer.parseInt(minET.getText().toString());
					max = Integer.parseInt(maxET.getText().toString());
					if(min >= max){
						showInfo("最小值不能大于等于最大值！");
					}else{
						MIN_EXP = min;
						MAX_EXP = max;
						closeDialog(arg0);
					}
				}else{
					showInfo("最大值或最小值不能为空！");
				}
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				closeDialog(arg0);
			}
		})
		.show();
		minET.setText(MIN_EXP+"");
		maxET.setText(MAX_EXP+"");
	}
	
	private void closeDialog(DialogInterface dialog)
	{
		try {
			Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
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
			if(((String) tempListMap.get("item_count")).isEmpty()){
				itemArray[i][1] = 0;
			}else{
				itemArray[i][1] = Integer.parseInt((String) tempListMap.get("item_count"));
			}
			
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
		for(t=1 ; t <= minCount; t++){
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
	
	private void showAbout()
	{
		String aboutStr = "计算雷霆战机升级材料最佳组合的小工具，仅供参考使用 ，还是以功能为主，界面什么的只能惨不忍睹了。在此特别感谢觅哥的支持！\n如有任何意见或发现bug，请联系：\ntwh_eastasia@163.com";
		showDialog(aboutStr, "关于");
	}
	
	protected void showDialog(String resultStr, String title) 
	{
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(resultStr);
		builder.setTitle(title);
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
		new AlertDialog.Builder(this).setTitle("选择经验值（可手动添加）").setIcon(
				android.R.drawable.ic_dialog_info).setSingleChoiceItems(
					itemExp,
//							getDefaultExps(),
						0,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("item_position", ""+pos);
								map.put("item_icon", R.drawable.evolution1);
								map.put("item_btn", itemExp[which]);
								map.put("item_count", text);
								map.put("item_delete", R.drawable.btn_dele);
								listData.set(pos, map);
//								itemArray[pos][1] = Integer.parseInt(itemExp[which]);
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
						.setNegativeButton("添加", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								arg0.dismiss();
								showAddItemExpDialog(pos, text);
							}
						})
						.show();
	}
	
	private void showAddItemExpDialog(final int pos, final String text)
	{
		final EditText et = new EditText(this); 
		et.setInputType(InputType.TYPE_CLASS_NUMBER);
		new AlertDialog.Builder(this).setTitle("请输入经验值").setIcon(
				android.R.drawable.ic_dialog_info)
				.setView(et)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("item_position", ""+listData.size()+1);
						map.put("item_icon", R.drawable.evolution1);
						map.put("item_btn", et.getText().toString());
						map.put("item_count", text);
						map.put("item_delete", R.drawable.btn_dele);
						listData.set(pos, map);
//						if(et.getText().toString().isEmpty()){
//							itemArray[pos][0] = 9600;
//						}else{
//							itemArray[pos][0] = Integer.parseInt(et.getText().toString());
//						}
						renderListView();
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
				if(array[i][j] == 0){
					continue;
				}
				itemResultStr += " " + itemArray[j][0] + " * " + array[i][j] + " 个 ";
			}
			resultStrs += "浪费的经验值: " + array[i][arrayCellLength-1] +", 材料组合： "+ itemResultStr + ";\n";
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
//				holder.itemImg = (ImageView)convertView.findViewById(R.id.items_icon);
				holder.itemBtn = (Button)convertView.findViewById(R.id.item_btn);
				holder.countET = (EditText)convertView.findViewById(R.id.item_count);
				holder.itemDel = (Button)convertView.findViewById(R.id.item_delete);
				convertView.setTag(holder);
				
			}else {
				
				holder = (ViewHolder)convertView.getTag();
			}
			
			holder.indexTV.setText((String)listData.get(position).get("item_position"));
//			holder.itemImg.setBackgroundResource((Integer)listData.get(position).get("item_icon"));
			holder.itemBtn.setText((String)listData.get(position).get("item_btn"));
			holder.countET.setText((String)listData.get(position).get("item_count"));
			holder.itemDel.setBackgroundResource((Integer)listData.get(position).get("item_delete"));
			
			final String btnContent = holder.itemBtn.getText().toString();
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
					if(listData.size() == 1){
						showInfo("至少保留一条数据！");
					}else if(listData.size() > 1){
						listData.remove(position);
						renderListView();
					}
					
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
					map.put("item_icon", R.drawable.evolution1);
					map.put("item_btn", btnContent);
					map.put("item_count", arg0.toString());
					map.put("item_delete", R.drawable.btn_dele);
					listData.set(position, map);
//					if(Integer.parseInt(arg0.toString()) > 50){
//						showInfo("友情提示：个数不要填太多哦，否则会计算的很慢。");
//					}
//					if(arg0.toString().isEmpty()){
//						itemArray[position][1] = 1;
//					}else{
//						itemArray[position][1] = Integer.parseInt(arg0.toString());
//					}
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
	
	private String[] getDefaultExps() throws IOException
	{
		String defaultExps[] = null;
		String fileName = SDCARD+DIR_NAME + "/figureExp.txt";
		File file = new File(fileName);
		if(!file.exists()){
			int i;
			String s= "";
			for(i=0;i<itemExp.length; i++){
				if(i == itemExp.length-1){
					s += itemExp[i];
				}else{
					s += itemExp[i]+"\n";
				}
			}
			createFile(s);
		}
		defaultExps = readFileSdcardFile(fileName).split("\n");
		return defaultExps;
		
	}
	
	public static void createFile(String text) 
	{
		String filename = "expSettings";
		if(newFolder(DIR_NAME)){
			File file = new File(filename);
			if (!file.exists()) {
				try {
					//在指定的文件夹中创建文件
					save(filename, text);
				} catch (Exception e) {
				}
			}
		}
	}
	
	public static void save(String name, String text)
	{
		try {
			File sdCardDir = Environment.getExternalStorageDirectory();//获取SDCard目录
			File saveFile = new File(sdCardDir+"/simpleNote/", name +".txt");
			FileOutputStream outStream = new FileOutputStream(saveFile);
			outStream.write(text.getBytes());
			outStream.close();
		} catch (FileNotFoundException e) {
			return;
		}
		catch (IOException e){
			return ;
		}
	}

	//新建文件夹
	public static boolean newFolder(String file)
	{
		File dirFile = new File( SDCARD +file);
		try
		{
			if (!(dirFile.exists()) && !(dirFile.isDirectory()))
			{
				boolean creadok = dirFile.mkdirs();
				if (creadok)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println(e);
			return false;
		}
		return true;
	}
	
	public String readFileSdcardFile(String fileName) throws IOException{   
		  String res="";   
		  try{   
			  FileInputStream fin = new FileInputStream(fileName);   
			  int length = fin.available();   
			  byte [] buffer = new byte[length];   
			  fin.read(buffer);       
			  res = EncodingUtils.getString(buffer, "UTF-8");   
			  fin.close();       
		  }   

		  catch(Exception e){   
			  e.printStackTrace();   
		  }   
		  return res;   
		}   
}
