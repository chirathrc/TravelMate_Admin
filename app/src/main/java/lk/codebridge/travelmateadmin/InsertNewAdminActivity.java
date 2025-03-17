package lk.codebridge.travelmateadmin;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import lk.codebridge.travelmateadmin.model.Position;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InsertNewAdminActivity extends AppCompatActivity {

    private List<String> positionListFinal = new ArrayList<>();
    private String selectedPosition = null; // Track selected position

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_insert_new_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Gson gson = new Gson();

        positionListFinal.add("Select position");


        String url = getString(R.string.url);

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder().url(url + "/travelmate/admin/getPositions").build();

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Response response = okHttpClient.newCall(request).execute();

                    if (response.isSuccessful()) {

                        String data = response.body().string();

                        JsonArray positionList = gson.fromJson(data, JsonArray.class);

                        for (JsonElement element : positionList) {

//                            positionListFinal.add(new Position(
//                                    element.getAsJsonObject().get("idposition").getAsInt(),
//                                    element.getAsJsonObject().get("name").getAsString()
//                            ));
//

                            positionListFinal.add(element.getAsJsonObject().get("name").getAsString());
                            Log.i("position", element.getAsJsonObject().get("name").getAsString());

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(InsertNewAdminActivity.this, android.R.layout.simple_spinner_item, positionListFinal);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                Spinner positionSpinner = findViewById(R.id.position_spinner);
                                positionSpinner.setAdapter(adapter);

                                positionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                        if (position != 0) {
                                            selectedPosition = positionListFinal.get(position);
                                            Log.i("SelectedPosition", "Selected: " + selectedPosition);
                                        } else {
                                            selectedPosition = null;
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                        selectedPosition = null;
                                    }
                                });

                            }
                        });


                    } else {

                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }).start();

        Button save = findViewById(R.id.save_button);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveData(gson, okHttpClient, url);
            }
        });


    }

    void saveData(Gson gson, OkHttpClient client, String url) {

        EditText nameTxt = findViewById(R.id.name_input);
        EditText mobileTxt = findViewById(R.id.mobile_input);
        EditText emailTxt = findViewById(R.id.email_input);


        String email = emailTxt.getText().toString();
        String name = nameTxt.getText().toString();
        String mobile = mobileTxt.getText().toString();

        if (email.isEmpty() || name.isEmpty() || mobile.isEmpty() || selectedPosition == null) {
            Toasty.error(InsertNewAdminActivity.this, "Insert all details for admin.", Toasty.LENGTH_SHORT, true).show();
            return;
        }else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$") || !mobile.matches("(?:\\+94|94|0)(7[0-9]{8})")){

            Toasty.error(InsertNewAdminActivity.this, "Invalid Email or Mobile.", Toasty.LENGTH_SHORT, true).show();
            return;
        }


        JsonObject adminData = new JsonObject();

        adminData.addProperty("email", email);
        adminData.addProperty("name", name);
        adminData.addProperty("mobile", mobile);
        adminData.addProperty("position", selectedPosition);

        new Thread(new Runnable() {
            @Override
            public void run() {

                RequestBody body = RequestBody.create(gson.toJson(adminData), MediaType.get("application/json"));
                Request request = new Request.Builder().url(url + "/travelmate/admin/addNewAdmin").post(body).build();

                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                nameTxt.setText("");
                                emailTxt.setText("");
                                mobileTxt.setText("");

                                Spinner positionSpinner = findViewById(R.id.position_spinner);
                                positionSpinner.setSelection(0);

                                Toasty.success(InsertNewAdminActivity.this, "Admin register successful.", Toasty.LENGTH_LONG, true).show();

                            }
                        });

                    } else {

                        String responseData = response.body().string();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toasty.error(InsertNewAdminActivity.this, responseData, Toasty.LENGTH_SHORT, true).show();

                            }
                        });


                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        }).start();


    }
}