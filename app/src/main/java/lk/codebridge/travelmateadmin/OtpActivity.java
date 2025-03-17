package lk.codebridge.travelmateadmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OtpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otp);
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

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        TextView emailView = findViewById(R.id.adminTxtViewSendOtpMail);
        emailView.setText(email);

        Button buttonSubmitOtp = findViewById(R.id.adminBtnOtpSubmission);

        buttonSubmitOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText otpText = findViewById(R.id.adminTxtOtp);
                String otp = otpText.getText().toString();

                Thread thread = new Thread() {
                    @Override
                    public void run() {

                        Request request = new Request.Builder().url(MainActivity.urlKey + "/travelmate/admin/otpVerification?email=" + email + "&otp=" + otp).build();

                        OkHttpClient okHttpClient = new OkHttpClient();

                        try {
                            Response response = okHttpClient.newCall(request).execute();

                            String stringResponse = response.body().string();

                            if (response.isSuccessful()) {

                                boolean isVerified = Boolean.parseBoolean(stringResponse);

                                if (isVerified) {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toasty.success(OtpActivity.this, "You are verified. Please SignIn again to complete our process.", Toasty.LENGTH_LONG, true).show();
                                            Intent intentToSignIn = new Intent(OtpActivity.this, MainActivity.class);
                                            startActivity(intentToSignIn);
                                        }
                                    });

                                } else {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toasty.error(OtpActivity.this, "Invalid OTP.",Toasty.LENGTH_SHORT,true).show();
                                        }
                                    });

                                }

                            } else {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toasty.error(OtpActivity.this, "Something went wrong. Please try again later.",Toasty.LENGTH_SHORT,true).show();
                                    }
                                });
                            }

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                };

                thread.start();


            }
        });
    }
}