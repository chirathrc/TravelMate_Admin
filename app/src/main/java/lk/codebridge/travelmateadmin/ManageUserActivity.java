package lk.codebridge.travelmateadmin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ManageUserActivity extends AppCompatActivity {

    private List<JsonObject> filteredPackagesList = new ArrayList<>();

    private List<JsonObject> userList = new ArrayList<>();

    private UserDetailsAdapter userDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_user);

        // Handle window insets for system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ProgressBar progressBar = findViewById(R.id.progressBar_userLoad);
        progressBar.setVisibility(View.VISIBLE);

        userList.clear();
        filteredPackagesList.clear();

        String url = getString(R.string.url);
        RecyclerView recyclerView = findViewById(R.id.user_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(ManageUserActivity.this));

        userDetailsAdapter = new UserDetailsAdapter(filteredPackagesList, ManageUserActivity.this);
        recyclerView.setAdapter(userDetailsAdapter);

        new Thread(new Runnable() {
            @Override
            public void run() {

                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder().url(url + "/travelmate/findAllUsers").build();

                try {
                    Response response = okHttpClient.newCall(request).execute();

                    if (response.isSuccessful()) {


                        Gson gson = new Gson();

                        String data = response.body().string();

                        JsonArray jsonArray = gson.fromJson(data, JsonArray.class);

                        userList.clear();
                        filteredPackagesList.clear();

                        for (JsonElement element : jsonArray) {

                            userList.add(element.getAsJsonObject());

                        }

                        filteredPackagesList.addAll(userList);

                        runOnUiThread(() -> {
                            userDetailsAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        });

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

//                                RecyclerView recyclerView = findViewById(R.id.user_list_view);
//                                recyclerView.setLayoutManager(new LinearLayoutManager(ManageUserActivity.this));
//
//                                // Create and set the adapter
//                                userDetailsAdapter = new UserDetailsAdapter(userList, ManageUserActivity.this);
//                                recyclerView.setAdapter(userDetailsAdapter);

                                EditText editText = findViewById(R.id.search_bar);

                                editText.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                        filterPackages(String.valueOf(charSequence));


                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Log.i("text", String.valueOf(charSequence));
                                            }
                                        });

                                    }

                                    @Override
                                    public void afterTextChanged(Editable editable) {

                                    }
                                });

                            }
                        });

                    } else {

                        progressBar.setVisibility(View.GONE);

                        Toasty.error(ManageUserActivity.this, "Something went wrong.Please try again later.", Toasty.LENGTH_SHORT, true).show();

                    }

                } catch (IOException e) {
                    progressBar.setVisibility(View.GONE);
                    throw new RuntimeException(e);
                }

            }
        }).start();


    }


    private void filterPackages(String query) {
        filteredPackagesList.clear();


        if (query.isEmpty()) {

            // Show all packages when query is empty
            filteredPackagesList.addAll(userList);
        } else {
            // Filter the list
            for (JsonObject userObject : userList) {
                String userEmail = userObject.get("email").getAsString();
                if (userEmail.toLowerCase().contains(query.toLowerCase())) {
                    filteredPackagesList.add(userObject);
                }
            }
        }

        // Notify adapter about dataset changes
        if (userDetailsAdapter != null) {
            userDetailsAdapter.notifyDataSetChanged();
        }
    }
}

class UserViewHolder extends RecyclerView.ViewHolder {

    TextView userNameTextView;

    TextView email;

    LinearLayout linearLayout;

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);
        // Initialize the TextView for displaying the user's name
        userNameTextView = itemView.findViewById(R.id.user_name);
        email = itemView.findViewById(R.id.user_email);
        linearLayout = itemView.findViewById(R.id.user_manage);
    }
}

class UserDetailsAdapter extends RecyclerView.Adapter<UserViewHolder> {

    private final Context context;
    private final List<JsonObject> userList;

    public UserDetailsAdapter(List<JsonObject> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.user_manage, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        JsonObject user = userList.get(position);

        holder.userNameTextView.setText(user.get("name").getAsString());
        holder.email.setText(user.get("email").getAsString());
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, SingleUserManagmentActivity.class);
                intent.putExtra("data", new Gson().toJson(user));
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
