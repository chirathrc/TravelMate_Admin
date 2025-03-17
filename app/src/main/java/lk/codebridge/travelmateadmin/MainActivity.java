package lk.codebridge.travelmateadmin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

public class MainActivity extends AppCompatActivity {

    public static String urlKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        urlKey = getString(R.string.url);

        Button buttonAdminSignIn = findViewById(R.id.sign_in_button);

        buttonAdminSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                adminSignIn();

            }
        });
    }


    public void adminSignIn() {

        EditText emailText = findViewById(R.id.email_input);
        EditText passwordText = findViewById(R.id.password_input);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        Gson gson = new Gson();

        JsonObject signInObject = new JsonObject();
        signInObject.addProperty("email", email);
        signInObject.addProperty("password", password);

        Thread threadOne = new Thread(new Runnable() {
            @Override
            public void run() {

                RequestBody body = RequestBody.create(gson.toJson(signInObject), MediaType.get("application/json"));

                Request request = new Request.Builder().url(urlKey + "/travelmate/admin/signIn").post(body).build();

                OkHttpClient okHttpClient = new OkHttpClient();

                try {
                    Response response = okHttpClient.newCall(request).execute();

                    String responseString = response.body().string();

                    if (response.isSuccessful()) {

                        if (responseString.equals("Account is Deactivated")) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toasty.error(MainActivity.this, responseString).show();

                                    Intent intent = new Intent(MainActivity.this,OtpActivity.class);
                                    intent.putExtra("email",email);
                                    startActivity(intent);

                                }
                            });

                        } else {

                            Log.i("user", responseString);

                            JsonObject jsonObjectSignInUser = gson.fromJson(responseString, JsonObject.class);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toasty.success(MainActivity.this, "Logging Success").show();
                                }
                            });


                            SharedPreferences sharedPreferences = getSharedPreferences("lk.codebridge.travelmateadmin", Context.MODE_PRIVATE);

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("FirstName", jsonObjectSignInUser.get("firstName").getAsString());
                            if (!jsonObjectSignInUser.get("lastName").isJsonNull()) {
                                editor.putString("LastName", jsonObjectSignInUser.get("lastName").getAsString());
                            }

                            editor.putString("email", jsonObjectSignInUser.get("email").getAsString());
                            editor.putString("password", jsonObjectSignInUser.get("password").getAsString());
                            editor.putString("mobile", jsonObjectSignInUser.get("mobile").getAsString());
                            editor.putString("position", jsonObjectSignInUser.get("position").getAsJsonObject().get("name").getAsString());
                            editor.putBoolean("isLogin", true);


                            editor.apply();

                            Intent intent = new Intent(MainActivity.this,AdminHomeActivity.class);
                            startActivity(intent);

                        }

                    } else {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toasty.error(MainActivity.this, responseString).show();
                            }
                        });

                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        threadOne.start();

    }
}