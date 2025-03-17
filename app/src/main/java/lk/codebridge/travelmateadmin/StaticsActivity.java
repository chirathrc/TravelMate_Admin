package lk.codebridge.travelmateadmin;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StaticsActivity extends AppCompatActivity {
    private BarChart barChart;

    private float totalEran = 0;

    private float todayEarn;

    private Gson gson = new Gson();

    private int activeUsers;

    private int deactiveUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statics);

        barChart = findViewById(R.id.chart01); // Initialize barChart

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getCount();

        new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

                firebaseFirestore.collection("order").get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();

                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {

                                        for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {

                                            Log.i("data", String.valueOf(documentSnapshot.get("booked_date")));

                                            Object totalField = documentSnapshot.get("total");

                                            String booked_date = (String) documentSnapshot.get("booked_date");

                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                            String todayDate = sdf.format(new Date()); // Get current date as a string


                                            if (totalField instanceof String) {
                                                try {

                                                    if (booked_date != null && booked_date.equals(todayDate)) {

                                                        todayEarn += Float.parseFloat((String) totalField);
                                                    }

                                                    totalEran += Float.parseFloat((String) totalField);


                                                } catch (NumberFormatException e) {
                                                    Log.e("data", "Error parsing total value", e);
                                                }
                                            } else if (totalField instanceof Number) {
                                                // If it's already a number (e.g., Float or Integer), cast it directly
                                                totalEran += ((Number) totalField).floatValue();
                                            }
                                        }


                                        // Log the accumulated total price
                                        Log.i("total", String.valueOf(totalEran));


                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                setupBarChart();
                                            }
                                        });

                                    } else {
                                        Log.i("data", "No documents found in the 'order' collection.");
                                    }
                                } else {
                                    Log.e("data", "Error getting documents", task.getException());
                                }
                            }
                        });

            }
        }).start();

        // Configure chart
    }

    public void setupBarChart() {
        BarChart barChart = findViewById(R.id.chart01);

        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        barChart.animateY(2000, Easing.EaseInOutQuad);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);


        float todayIncome = todayEarn;  // Replace with actual calculated today's income
        float totalIncome = totalEran;  // Total earned income (already calculated)
        int otherIncome = (activeUsers + deactiveUsers);  // Placeholder for additional income category

        Log.i("totFinal", String.valueOf(totalIncome));

        // Bar Entries
        ArrayList<BarEntry> barEntryArrayList = new ArrayList<>();
        barEntryArrayList.add(new BarEntry(0, totalIncome));  // Total Income
        barEntryArrayList.add(new BarEntry(1, todayIncome));  // Today's Income

        // Data Set
        BarDataSet barDataSet = new BarDataSet(barEntryArrayList, "Income Stats");

        // Bar Colors
        ArrayList<Integer> colorArrayList = new ArrayList<>();
        colorArrayList.add(getColor(R.color.green_500));  // Total Income color
        colorArrayList.add(getColor(R.color.blue));  // Today's Income color
        barDataSet.setColors(colorArrayList);

        // Set Data
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.5f);
        barChart.setData(barData);
        barData.setValueTextSize(16f);

        Legend legend = barChart.getLegend();
        legend.setTextSize(10f);
        legend.setFormSize(10f);

        // Customize X-Axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Total Income ($)", "Today's Income ($)"}));

        // Customize Y-Axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1000f);
        leftAxis.setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);

        // Refresh Chart
        barChart.invalidate();
    }


    private void getCount() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String url = getString(R.string.url);

                OkHttpClient okHttpClient = new OkHttpClient();

                Request request = new Request.Builder().url(url + "/travelmate/userCount").build();

                try {
                    Response response = okHttpClient.newCall(request).execute();


                    if (response.isSuccessful()) {

                        String body = response.body().string();
                        JsonObject jsonObject = gson.fromJson(body, JsonObject.class);

                        activeUsers = jsonObject.get("activeUsers").getAsInt();
                        deactiveUsers = jsonObject.get("nonActiveusers").getAsInt();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LoadPieChart();
                            }
                        });

                    } else {
                        Log.i("Error in request", "error");
                    }
                } catch (IOException e) {
                    Log.i("Error in request", "error");
                    throw new RuntimeException(e);
                }

            }
        }).start();
    }


    private void LoadPieChart() {
        PieChart pieChart = findViewById(R.id.chart02);

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(activeUsers, "Active"));
        entries.add(new PieEntry(deactiveUsers, "Inactive"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.GREEN, Color.BLUE);
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(Color.BLACK);

        // Increase spacing between slices
        dataSet.setSliceSpace(5f);

        // Improve label visibility and add margins
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1OffsetPercentage(10f);
        dataSet.setValueLinePart1Length(0.4f);
        dataSet.setValueLinePart2Length(0.4f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);

        // Set center text
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("User Status");
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(Color.BLACK);

        // Customize Legend with padding
        Legend legend = pieChart.getLegend();
        legend.setTextSize(14f);
        legend.setFormSize(14f);
        legend.setTextColor(Color.BLACK);
        legend.setWordWrapEnabled(true);
        legend.setYOffset(10f);
        legend.setXOffset(10f);
        legend.setFormToTextSpace(15f);  // Space between color and text
        legend.setXEntrySpace(20f);  // Space between legend items

        // Align legend
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);

        // Animate Pie Chart
        pieChart.animateY(1000);
        pieChart.invalidate();
    }




}


