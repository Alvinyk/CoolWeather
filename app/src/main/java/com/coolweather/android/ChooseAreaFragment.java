package com.coolweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.LogUtil;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by alvin on 2017/9/1.
 */
public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE  = 0x00;
    public static final int LEVEL_CITY      = 0x01;
    public static final int LEVEL_COUNTY    = 0x02;

    private static final int TYPE_PROVINCE  = 0x10;
    private static final int TYPE_CITY      = 0x11;
    private static final int TYPE_COUNTY    = 0x12;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button  backButton;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savadInstanceState){
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);

        adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent,View view,int position,long id){
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    showWeatherPage(weatherId);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });

        queryProvinces();
    }


    private void showWeatherPage(String weatherId){
        if(getActivity() instanceof MainActivity){
            Intent intent = new Intent(getActivity(),WeatherActivity.class);

            intent.putExtra("weather_id",weatherId);
            startActivity(intent);
            getActivity().finish();
        }else if(getActivity() instanceof WeatherActivity){
            WeatherActivity activity = (WeatherActivity)getActivity();
            activity.closeDrawers();
            activity.startRefreshing();
            activity.requestWeather(weatherId);
        }

    }
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);

        if(provinceList.size() > 0){
            dataList.clear();
            for(Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,TYPE_PROVINCE);
        }
    }

    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size() > 0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address,TYPE_CITY);
        }
    }

    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size() > 0){
            dataList.clear();
            for(County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,TYPE_COUNTY);
        }
    }

    private void queryFromServer(String address,final int typeId){
        showProgressDialog();

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                LogUtil.i("receiveHttpResponse responseText : "+responseText);
                boolean result = handleHttpResponse(responseText,typeId);

                if(result){
                    queryhttpDate(typeId);
                }
            }
        });
    }

    private boolean handleHttpResponse(String responseText,int typeId){
        boolean result = false;
        switch (typeId){
            case TYPE_PROVINCE:
                result = Utility.handleProvinceResponse(responseText);
                break;
            case TYPE_CITY:
                result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                break;
            case TYPE_COUNTY:
                result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                break;
            default:
                break;
        }

        return result;
    }

    private void queryhttpDate(final int typeId){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeProgressDialog();
                switch (typeId){
                    case TYPE_PROVINCE:
                        queryProvinces();
                        break;
                    case TYPE_CITY:
                        queryCities();
                        break;
                    case TYPE_COUNTY:
                        queryCounties();
                        break;
                    default:
                        break;
                }
            }
        });
    }
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }

        progressDialog.show();
    }


    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}
