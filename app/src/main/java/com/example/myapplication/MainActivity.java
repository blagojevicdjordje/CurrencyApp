package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.CurrencyAdapter;
import com.example.myapplication.Model.CurrencyItem;
import com.example.myapplication.Model.Rates;
import com.example.myapplication.Model.RootObject;
import com.example.myapplication.Retrofit.RetrofitClient;
import com.example.myapplication.Retrofit.revolutcodesInterface;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recycler_view;
    private CurrencyAdapter adapter;
    private Gson gson;
    private BroadcastReceiver valueBroadcastReceiver, swapBroadcastReceiver;
    private double newValue = 1;
    private List<CurrencyItem> currencyItems;
    private int swapPosition;
    private CurrencyItem currencyItem;
    private CompositeDisposable compositeDisposable;
    private Retrofit retrofit;
    private revolutcodesInterface mService;
    private Handler handler = new Handler();
    private Runnable runnable;
    private int time = 1000;
    private boolean convert = false;
    private boolean isSwaping = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        getAction();
        doBackgroundWork();
    }

    private void init() {
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        recycler_view.setHasFixedSize(true);
        recycler_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        gson = new Gson();
        compositeDisposable = new CompositeDisposable();
        retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(revolutcodesInterface.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("value");
        registerReceiver(valueBroadcastReceiver, intentFilter);
        IntentFilter swapIntentFilter = new IntentFilter("swapItems");
        registerReceiver(swapBroadcastReceiver, swapIntentFilter);
    }

    @Override
    public void onBackPressed() {
    }

    private void checkConverting(boolean convert, List<CurrencyItem> currencyItems) {
        if (convert) {
            for (int i = 1; i < currencyItems.size(); i++) {
                double amount = newValue * currencyItems.get(i).getAmount();
                String subAmount = String.valueOf(amount);
                try {
                    subAmount = subAmount.substring(0, subAmount.indexOf(".") + 3);
                } catch (StringIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                amount = Double.parseDouble(subAmount);
                currencyItems.set(i, new CurrencyItem(currencyItems.get(i).getDescription(), amount, currencyItems.get(i).getCurrencyName(),
                        currencyItems.get(i).getCountryCode()));
                adapter.setConvertingList((ArrayList<CurrencyItem>) currencyItems, i);
            }
        } else {
            setAdapter((ArrayList<CurrencyItem>) currencyItems);
        }
    }

    private void getAction() {
        valueBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    newValue = Double.valueOf(intent.getStringExtra("newValue")) / currencyItems.get(0).getAmount();
                } catch (NumberFormatException e) {
                    newValue = 0;
                    e.printStackTrace();
                }
                setNewValues(newValue);
            }
        };

        swapBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                swapItems(intent);
            }
        };
    }

    private void swapItems(Intent intent) throws IndexOutOfBoundsException {
        swapPosition = intent.getIntExtra("swapPosition", 0);
        isSwaping = intent.getBooleanExtra("isSwaping", false);
        currencyItem = currencyItems.remove(swapPosition);
        currencyItems.remove(currencyItem);
        currencyItems.add(0, currencyItem);
        recycler_view.scrollToPosition(0);
        adapter.notifyItemMoved(swapPosition, 0);
        convert = true;
    }

    private void setNewValues(double newValue) {
        for (int i = 1; i < currencyItems.size(); i++) {
            double amount = newValue * currencyItems.get(i).getAmount();
            String subAmount = String.valueOf(amount);
            try {
                subAmount = subAmount.substring(0, subAmount.indexOf(".") + 3);
            } catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            amount = Double.parseDouble(subAmount);
            currencyItems.set(i, new CurrencyItem(currencyItems.get(i).getDescription(), amount, currencyItems.get(i).getCurrencyName(),
                    currencyItems.get(i).getCountryCode()));
            adapter.notifyItemChanged(i);
        }
    }

    private void setAdapter(ArrayList<CurrencyItem> currencyItems) {
        if (adapter == null) {
            adapter = new CurrencyAdapter(getApplicationContext(), currencyItems);
            recycler_view.setAdapter(adapter);
        } else {
            adapter.setList(currencyItems);
        }
    }

    private void getRatesFieldValue(ArrayList<Double> list, Rates rates) throws IllegalAccessException, StringIndexOutOfBoundsException {
        // Get the all field objects of Rates class
        Field[] fields = Rates.class.getFields();

        for (int i = 0; i < fields.length; i++) {
            // get value of the fields
            Object value = fields[i].get(rates);
            String subValue = String.valueOf(value);
            subValue = subValue.substring(0, subValue.indexOf(".") + 3);
            value = Double.parseDouble(subValue);
            list.add((Double) value);
        }
    }

    private void getRatesFieldName(ArrayList<String> list) throws IllegalAccessException {
        // Get the all field objects of Rates class
        Field[] fields = Rates.class.getFields();

        for (int i = 0; i < fields.length; i++) {
            // get name of the fields
            Object value = fields[i].getName();
            list.add((String) value);
        }
    }

    private void setHasMap(HashMap<String, Double> hashMap, Rates rates) throws IllegalAccessException {
        ArrayList<String> fieldNameList = new ArrayList<>();
        ArrayList<Double> valueList = new ArrayList<>();
        getRatesFieldValue(valueList, rates);
        getRatesFieldName(fieldNameList);
        for (int i = 0; i < fieldNameList.size(); i++) {
            hashMap.put(fieldNameList.get(i), valueList.get(i));
        }
    }

    private void setInformation(RootObject rootObject) throws IllegalAccessException {
        ArrayList<String> fieldsNameList = new ArrayList<>();
        ArrayList<String> currencyName = new ArrayList<>();
        ArrayList<Double> fieldsValueList = new ArrayList<>();
        ArrayList<String> countryCodeList = new ArrayList<>();
        currencyName.addAll(Arrays.asList(getResources().getStringArray(R.array.currency_name)));
        countryCodeList.addAll(Arrays.asList(getResources().getStringArray(R.array.contry_code)));
        getRatesFieldName(fieldsNameList);
        getRatesFieldValue(fieldsValueList, rootObject.getRates());
        setCurrencyItem(fieldsNameList, fieldsValueList, currencyName, countryCodeList);
    }

    private void setCurrencyItem(ArrayList<String> fieldsNameList, ArrayList<Double> list,
                                 ArrayList<String> currencyName, ArrayList<String> countryCodeList) {
        currencyItems = new ArrayList<>();
        for (int i = 0; i < fieldsNameList.size(); i++) {
            currencyItems.add(new CurrencyItem(fieldsNameList.get(i), list.get(i), currencyName.get(i), countryCodeList.get(i)));
        }
    }

    private void getCurrencyInformation() {
        compositeDisposable.add(mService.getCurrency()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RootObject>() {
                    @Override
                    public void accept(RootObject rootObject) throws Exception {
                        if (isSwaping) {
                            HashMap<String, Double> hashMap = new HashMap<>();
                            setHasMap(hashMap, rootObject.getRates());
                            updateValues(hashMap);

                        } else {
                            setInformation(rootObject);
                        }
                        checkConverting(convert, currencyItems);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                }));
    }

    private void updateValues(HashMap<String, Double> hashMap) throws NullPointerException {
        for (int i = 0; i < currencyItems.size(); i++) {
            if (i == 0) {
                double value = hashMap.get(currencyItems.get(i).getDescription());
                currencyItems.set(i, new CurrencyItem(currencyItems.get(i).getDescription(), value, currencyItems.get(i).getCurrencyName(),
                        currencyItems.get(i).getCountryCode()));
            } else {
                double value = hashMap.get(currencyItems.get(i).getDescription());
                currencyItems.set(i, new CurrencyItem(currencyItems.get(i).getDescription(), value, currencyItems.get(i).getCurrencyName(),
                        currencyItems.get(i).getCountryCode()));
            }
        }
    }

    private void doBackgroundWork() {
        runnable = new Runnable() {
            @Override
            public void run() {
                getCurrencyInformation();
                handler.postDelayed(runnable, time);
            }
        };
        handler.postDelayed(runnable, 10);
    }
}
