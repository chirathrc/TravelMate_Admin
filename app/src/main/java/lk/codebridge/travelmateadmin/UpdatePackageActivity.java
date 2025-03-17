package lk.codebridge.travelmateadmin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class UpdatePackageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_update_package);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Gson gson = new Gson();

        String url = getString(R.string.url);

        Intent intent = getIntent();

        String jsonData = intent.getStringExtra("data");
        JsonObject jsonObject = new Gson().fromJson(jsonData, JsonObject.class);

        OkHttpClient okHttpClient = new OkHttpClient();

        EditText editTextPackageName = findViewById(R.id.input_package_name);
        EditText editTextPackageCity = findViewById(R.id.input_package_city);
        EditText editTextPackagePrice = findViewById(R.id.input_price_per_person);
        EditText editTextPackagePlaceDesc1 = findViewById(R.id.input_place_desc1);
        EditText editTextPackagePlaceDesc2 = findViewById(R.id.input_place_desc2);
        EditText editTextPackageNights = findViewById(R.id.input_nights);
        EditText editTextPackageDays = findViewById(R.id.input_days);
        EditText editTextPackageWiki = findViewById(R.id.input_wikipedia_url);

        editTextPackageName.setText(jsonObject.get("packageName").getAsString());
        editTextPackageDays.setText(jsonObject.get("days").getAsString());
        editTextPackageCity.setText(jsonObject.get("city").getAsString());
        editTextPackagePrice.setText(jsonObject.get("pricePerPerson").getAsString());
        editTextPackagePlaceDesc1.setText(jsonObject.get("descOne").getAsString());
        editTextPackagePlaceDesc2.setText(jsonObject.get("descTwo").getAsString());
        editTextPackageNights.setText(jsonObject.get("nights").getAsString());
        editTextPackageWiki.setText(jsonObject.get("wikiUrl").getAsString());

        String id = jsonObject.get("id").getAsString();

        Switch statusSwitch = findViewById(R.id.switch_package_status);

        //1 --- active
        if (jsonObject.get("status").getAsInt() == 1) {

            statusSwitch.setChecked(true);
            statusSwitch.setText(getString(R.string.otTxt10));

        } else {

            statusSwitch.setChecked(false);
            statusSwitch.setText(getString(R.string.otTxt11));

        }

        statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {


                statusSwitch.setText(b ? R.string.otTxt10 : R.string.otTxt11);


                new Thread(new Runnable() {
                    @Override
                    public void run() {


                        String requestUrl = url + "/travelmate/package/updateStatus/" + id + "?status=" + b;

//                        Log.i("url", requestUrl);

                        Request request = new Request.Builder().url(requestUrl).build();

                        try {
                            Response response = okHttpClient.newCall(request).execute();

                            if (response.isSuccessful()) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toasty.success(UpdatePackageActivity.this, "Successfully changed.", Toasty.LENGTH_SHORT, true).show();
                                    }
                                });

                            } else {


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toasty.error(UpdatePackageActivity.this, "Something went wrong. try again later.", Toasty.LENGTH_SHORT, true).show();
                                    }
                                });

                            }

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }


                    }
                }).start();

            }
        });

        ImageButton back = findViewById(R.id.backToPackages);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(UpdatePackageActivity.this, AdminHomeActivity.class);

                startActivity(intent);

            }
        });


        Button updateButton = findViewById(R.id.btn_update_package);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String packageName = editTextPackageName.getText().toString();
                String packageCity = editTextPackageCity.getText().toString();
                String packagePrice = editTextPackagePrice.getText().toString();
                String placeDesc1 = editTextPackagePlaceDesc1.getText().toString();
                String placeDesc2 = editTextPackagePlaceDesc2.getText().toString();
                String nights = editTextPackageNights.getText().toString();
                String days = editTextPackageDays.getText().toString();
                String wikiUrl = editTextPackageWiki.getText().toString();

                if (TextUtils.isEmpty(packageName)) {
                    editTextPackageName.setError("Package name cannot be empty");
                }
                if (TextUtils.isEmpty(packageCity)) {
                    editTextPackageCity.setError("City cannot be empty");
                }
                if (TextUtils.isEmpty(packagePrice)) {
                    editTextPackagePrice.setError("Price per person cannot be empty");
                }
                if (TextUtils.isEmpty(placeDesc1)) {
                    editTextPackagePlaceDesc1.setError("Place description 1 cannot be empty");
                }
                if (TextUtils.isEmpty(placeDesc2)) {
                    editTextPackagePlaceDesc2.setError("Place description 2 cannot be empty");
                }
                if (TextUtils.isEmpty(nights)) {
                    editTextPackageNights.setError("Nights cannot be empty");
                }
                if (TextUtils.isEmpty(days)) {
                    editTextPackageDays.setError("Days cannot be empty");
                }
                if (TextUtils.isEmpty(wikiUrl)) {
                    editTextPackageWiki.setError("Wikipedia URL cannot be empty");

                } else {

                    JsonObject packageData = new JsonObject();

                    try {
                        packageData.addProperty("packageName", packageName);
                        packageData.addProperty("city", packageCity);
                        packageData.addProperty("pricePerPerson", packagePrice);
                        packageData.addProperty("descOne", placeDesc1);
                        packageData.addProperty("descTwo", placeDesc2);
                        packageData.addProperty("nights", nights);
                        packageData.addProperty("days", days);
                        packageData.addProperty("wikiUrl", wikiUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            String requestUrl = url + "/travelmate/package/updatePackage/" + id;

                            RequestBody body = RequestBody.create(gson.toJson(packageData), MediaType.get("application/json"));
                            Request request = new Request.Builder().url(requestUrl).post(body).build();

                            try {
                                Response response = okHttpClient.newCall(request).execute();

                                if (response.isSuccessful()) {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toasty.success(UpdatePackageActivity.this, "Successfully Updated.", Toasty.LENGTH_SHORT, true).show();
                                        }
                                    });


                                } else {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toasty.error(UpdatePackageActivity.this, "Something went wrong. try again later.", Toasty.LENGTH_SHORT, true).show();
                                        }
                                    });

                                }

                            } catch (IOException e) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toasty.error(UpdatePackageActivity.this, "Something went wrong. try again later.", Toasty.LENGTH_SHORT, true).show();
                                    }
                                });

                                throw new RuntimeException(e);

                            }


                        }
                    }).start();


                }

            }
        });


    }
}