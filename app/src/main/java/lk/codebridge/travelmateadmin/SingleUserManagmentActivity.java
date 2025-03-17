package lk.codebridge.travelmateadmin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SingleUserManagmentActivity extends AppCompatActivity {


    int status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_single_user_managment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        Gson gson = new Gson();

        OkHttpClient okHttpClient = new OkHttpClient();


        String url = getString(R.string.url);

        String userData = intent.getStringExtra("data");

        JsonObject user = gson.fromJson(userData, JsonObject.class);

        TextView userName = findViewById(R.id.single_user_name);
        TextView userEmail = findViewById(R.id.single_user_email);
        TextView userMobile = findViewById(R.id.single_user_mobile);
        TextView password = findViewById(R.id.single_user_password);
        Switch status_switch = findViewById(R.id.status_switch);


        userName.setText(user.get("name").getAsString());
        userEmail.setText(user.get("email").getAsString());
        userMobile.setText(user.get("mobile").getAsString());
        password.setText(user.get("password").getAsString());

        Log.i("status", String.valueOf(user.get("status").getAsInt()));

        status = user.get("status").getAsInt();

        if (status == 1) {

            status_switch.setChecked(true);
            status_switch.setText(getString(R.string.otTxt10));

        } else {

            status_switch.setChecked(false);
            status_switch.setText(getString(R.string.otTxt11));

        }


        status_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String requestLink = url + "/travelmate/changeUserStatus?email=" + user.get("email").getAsString() + "&status=" + b;

                        Log.i("link", requestLink);

                        Request request = new Request.Builder().url(requestLink).build();

                        try {
                            Response response = okHttpClient.newCall(request).execute();

                            if (response.isSuccessful()) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (status == 1) {

                                            status = 2;
                                            status_switch.setText(getString(R.string.otTxt11));

                                        } else {
                                            status = 1;
                                            status_switch.setText(getString(R.string.otTxt10));
                                        }

                                    }
                                });


                                boolean isSucces = Boolean.parseBoolean(response.body().string());

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toasty.info(SingleUserManagmentActivity.this, (isSucces ? "Successfully Updated" : "Please try again later."), Toasty.LENGTH_SHORT, true).show();

                                    }
                                });


                            } else {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toasty.error(SingleUserManagmentActivity.this, "Something went wrong. Please try again later.", Toasty.LENGTH_SHORT, true).show();
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


        new Thread(new Runnable() {
            @Override
            public void run() {

                Request request = new Request.Builder().url(url + "/travelmate/packageOrdering/getOrderedDetailsByUser?email=" + user.get("email").getAsString()).build();

                try {
                    Response response = okHttpClient.newCall(request).execute();

                    if (response.isSuccessful()) {

                        String responseData = response.body().string();

                        JsonArray jsonArray = gson.fromJson(responseData, JsonArray.class);

                        List<JsonObject> orderData = new ArrayList<>();

                        for (JsonElement element : jsonArray) {

                            orderData.add(element.getAsJsonObject());

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                PastOrderPackageAdapter packageAdapter = new PastOrderPackageAdapter(orderData, SingleUserManagmentActivity.this);

                                RecyclerView recyclerView = findViewById(R.id.past_bookings);
                                recyclerView.setLayoutManager(new LinearLayoutManager(SingleUserManagmentActivity.this));

                                recyclerView.setAdapter(packageAdapter);

                            }
                        });


                    } else {


                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        }).start();

    }
}


class PastOrderPackageViewHolder extends RecyclerView.ViewHolder {

    TextView textViewStartDate;
    TextView textViewEndDate;

    TextView past_order_tittle;

    TextView past_order_price;

    public PastOrderPackageViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewStartDate = itemView.findViewById(R.id.textViewStartDate);
        textViewEndDate = itemView.findViewById(R.id.textViewEndDate);

        past_order_price = itemView.findViewById(R.id.past_order_price);
        past_order_tittle = itemView.findViewById(R.id.past_order_tittle);

    }

}

class PastOrderPackageAdapter extends RecyclerView.Adapter<PastOrderPackageViewHolder> {

    private final Context context;
    private final List<JsonObject> packagesList;

    public PastOrderPackageAdapter(List<JsonObject> packagesList, Context context) {
        this.packagesList = packagesList;
        this.context = context;
    }


    @NonNull
    @Override
    public PastOrderPackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.past_booking, parent, false);
        return new PastOrderPackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PastOrderPackageViewHolder holder, int position) {

        JsonObject dataObject = packagesList.get(position);

        holder.past_order_tittle.setText(dataObject.get("travelPackage").getAsJsonObject().get("packageName").getAsString());
        holder.textViewStartDate.setText(dataObject.get("checkIn").getAsString());
        holder.textViewEndDate.setText(dataObject.get("checkout").getAsString());

        float pricePerPerson = dataObject.get("travelPackage").getAsJsonObject().get("pricePerPerson").getAsFloat();
        int persons = dataObject.get("persons").getAsInt();

        int total = (int) (pricePerPerson * persons);

        holder.past_order_price.setText(String.valueOf(total));

    }

    @Override
    public int getItemCount() {
        return packagesList.size();
    }
}
