package lk.codebridge.travelmateadmin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(5000);

                    SharedPreferences sharedPreferences = getSharedPreferences("lk.codebridge.travelmateadmin", Context.MODE_PRIVATE);
                    boolean isLogin = sharedPreferences.getBoolean("isLogin",false);

                    if (isLogin){

                        Intent intent = new Intent(LoadingActivity.this,AdminHomeActivity.class);
                        startActivity(intent);
                    }else {
                        Intent intent = new Intent(LoadingActivity.this,MainActivity.class);
                        startActivity(intent);
                    }



                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        thread.start();
    }
}