package com.example.covid_tracker;

import android.app.VoiceInteractor;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tv_confirmed, tv_confirmed_new, tv_active,tv_active_new,tv_recovered, tv_recovered_new, tv_death, tv_death_new, tv_tests, tv_tests_new, tv_date, tv_time;

    private String str_confirmed, str_confirmed_new, str_active, str_active_new, str_recovered, str_recovered_new, str_deaths, str_deaths_new, str_tests, str_tests_new, str_last_update_time;

    private LinearLayout lin_state_data, lin_world_data;

    private SwipeRefreshLayout swipeRefreshLayout;

    private PieChart pieChart;
    private int int_active_new;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         //Initialize
        Init();
        FetchData();
    }

    private void FetchData() {
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        String apiUrl="https://api.covid19india.org/data.json";

        pieChart.clearChart();

        JsonObjectRequest  jsonObjectRequest=new JsonObjectRequest(
                Request.Method.GET,
                apiUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //As the Data of the JSON are in nested array, we need to define the array from which we want to fetch the data.
                        JSONArray all_state_jsonArray=null;
                        JSONArray testData_jsonArray=null;

                        try {
                            all_state_jsonArray=response.getJSONArray("statewise");
                            testData_jsonArray=response.getJSONArray("tested");
                            JSONObject data_india=all_state_jsonArray.getJSONObject(0);
                            JSONObject test_data_india=testData_jsonArray.getJSONObject(testData_jsonArray.length()-1);


                            //Fetching data for India and storing it in String
                            str_confirmed=data_india.getString("confirmed");
                            str_confirmed_new=data_india.getString("deltaconfirmed");

                            str_active=data_india.getString("active");

                            str_recovered=data_india.getString("recovered");
                            str_recovered_new=data_india.getString("deltarecovered");

                            str_deaths=data_india.getString("deaths");
                            str_deaths_new=data_india.getString("deltadeaths");

                            str_last_update_time=data_india.getString("lastupdatedtime");

                            str_tests=test_data_india.getString("totalsamplestested");
                            str_tests_new=test_data_india.getString("samplereportedtoday");

                            Handler delayToshowProgress=new Handler();   // This statement lets us delay the process to updation of data for desired amount of time to let the progress activity show
                            delayToshowProgress.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Setting text in the textView
                                    tv_confirmed.setText(NumberFormat.getInstance().format(Integer.parseInt(str_confirmed)));
                                    tv_confirmed_new.setText("+"+ NumberFormat.getInstance().format(Integer.parseInt(str_confirmed_new)));

                                    tv_active.setText(NumberFormat.getInstance().format(Integer.parseInt(str_active)));
                                    int_active_new=Integer.parseInt(str_confirmed_new)-(Integer.parseInt(str_recovered_new)+Integer.parseInt(str_deaths_new));
                                    tv_active_new.setText("+"+NumberFormat.getInstance().format(int_active_new));

                                    tv_recovered.setText(NumberFormat.getInstance().format(Integer.parseInt(str_recovered)));
                                    tv_recovered_new.setText("+"+NumberFormat.getInstance().format(Integer.parseInt(str_recovered_new)));

                                    tv_death.setText(NumberFormat.getInstance().format(Integer.parseInt(str_deaths)));
                                    tv_death_new.setText("+"+NumberFormat.getInstance().format(Integer.parseInt(str_deaths_new)));

                                    tv_tests.setText(NumberFormat.getInstance().format(Integer.parseInt(str_tests)));
                                    tv_tests_new.setText("+"+NumberFormat.getInstance().format(Integer.parseInt(str_tests_new)));

                                    tv_date.setText(FormatDate(str_last_update_time, 1));
                                    tv_time.setText(FormatDate(str_last_update_time, 2));

                                    pieChart.addPieSlice(new PieModel("Active", Integer.parseInt(str_active), Color.parseColor("#007afe")));
                                    pieChart.addPieSlice(new PieModel("Recovered", Integer.parseInt(str_recovered),Color.parseColor("#08a045")));
                                    pieChart.addPieSlice(new PieModel("Deceased", Integer.parseInt(str_deaths), Color.parseColor("#F6404F")));


                                }
                            },1000);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    public String FormatDate (String date, int testCase){
        Date mDate =null;
        String dateFormat;
        try {
            mDate =new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).parse(date);
            if(testCase==0){
                dateFormat=new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(mDate);
                return dateFormat;
            }else if (testCase==1){
                dateFormat =new SimpleDateFormat("dd MMM yyyy").format(mDate);
                return dateFormat;
            }else if(testCase==2){
                dateFormat=new SimpleDateFormat("hh:mm a").format(mDate);
                return dateFormat;
            }else{

                Log.d("error", "Wrong input! Choose from 0 to 2");
                return "Error";
            }
        }catch (ParseException e){
            e.printStackTrace();
            return date;
        }
    }
    private void Init() {
        tv_confirmed=findViewById(R.id.activity_main_confirme_title_textview);
        tv_confirmed_new=findViewById(R.id.activity_main_confirmed_new_textview);
        tv_active=findViewById(R.id.activity_main_active_textview);
        tv_active_new=findViewById(R.id.activity_main_active_new_textview);
        tv_recovered=findViewById(R.id.activity_main_recovered_textview);
        tv_recovered_new=findViewById(R.id.activity_main_recovered_new_textview);
        tv_death=findViewById(R.id.activity_main_death_textview);
        tv_death_new=findViewById(R.id.activity_main_death_new_textview);
        tv_tests=findViewById(R.id.activity_main_samples_textview);
        tv_tests_new=findViewById(R.id.activity_main_samples_new_textview);
        tv_date=findViewById(R.id.activity_main_date_new_textview);
        tv_time=findViewById(R.id.activity_main_time_textview);

        pieChart=findViewById(R.id.activity_main_piechart);
        swipeRefreshLayout=findViewById(R.id.activity_main_swipe_refresh_layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.menu_about){
            Toast.makeText(MainActivity.this, "About menu icon clicked", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);

    }
}