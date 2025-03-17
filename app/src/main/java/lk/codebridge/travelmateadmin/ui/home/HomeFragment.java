package lk.codebridge.travelmateadmin.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lk.codebridge.travelmateadmin.R;
import lk.codebridge.travelmateadmin.UpdatePackageActivity;
import lk.codebridge.travelmateadmin.databinding.FragmentHomeBinding;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PackageAdapter packageAdapter;
    private List<JsonObject> allPackagesList = new ArrayList<>();
    private List<JsonObject> filteredPackagesList = new ArrayList<>();
    private ProgressBar progressBar; // Add ProgressBar reference

    private SQLiteOpenHelper sqLiteOpenHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allPackagesList.clear();
        filteredPackagesList.clear();

        sqLiteOpenHelper = new SQLiteOpenHelper(getContext(), "travel_admin.db", null, 1) {
            @Override
            public void onCreate(SQLiteDatabase sqLiteDatabase) {

                sqLiteDatabase.execSQL("CREATE TABLE \"\" (\n" +
                        "    package_id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "    name       TEXT    NOT NULL,\n" +
                        "    price      INTEGER,\n" +
                        "    status     INTEGER\n" +
                        ");\n");
            }

            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

            }
        };

        progressBar = view.findViewById(R.id.progressBar); // Initialize ProgressBar
        RecyclerView recyclerView = view.findViewById(R.id.recycleViewOne);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        packageAdapter = new PackageAdapter(filteredPackagesList, requireActivity());
        recyclerView.setAdapter(packageAdapter);

        String urlText = getString(R.string.url);

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPackages(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPackages(newText); // Perform real-time filtering
                return true;
            }
        });

        loadPackages(urlText); // Load packages
    }

    private void loadPackages(String url) {
        progressBar.setVisibility(View.VISIBLE); // Show ProgressBar

        Thread threadReq = new Thread() {
            @Override
            public void run() {


                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.codebridge.travelmateadmin", Context.MODE_PRIVATE);

                String email = sharedPreferences.getString("email", null);

                OkHttpClient okHttpClient = new OkHttpClient();
                String apiUrl = url + "/travelmate/package/getAllPackages?email=" + email;
                Request request = new Request.Builder().url(apiUrl).build();

                try {
                    Response response = okHttpClient.newCall(request).execute();

                    if (response.body() != null) {
                        String data = response.body().string();
                        Log.i("responseData", data);

                        Gson gson = new Gson();
                        JsonArray jsonArray = gson.fromJson(data, JsonArray.class);

//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                SQLiteDatabase sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();
//
//                                for (JsonElement element : jsonArray) {
//                                    JsonObject packageObject = element.getAsJsonObject();
//
//                                    ContentValues contentValues = new ContentValues();
//                                    contentValues.put("package_id",packageObject.get("id").getAsInt());
//                                    contentValues.put("name",packageObject.get("packageName").getAsString());
//                                    contentValues.put("price",packageObject.get("pricePerPerson").getAsInt());
//                                    contentValues.put("status",packageObject.get("status").getAsString());
//
//
//
//                                }
//
//
//                            }
//                        }).start();

                        // Clear and update both lists
                        allPackagesList.clear();
                        filteredPackagesList.clear();
                        for (JsonElement element : jsonArray) {
                            JsonObject packageObject = element.getAsJsonObject();
                            allPackagesList.add(packageObject); // Store all packages
                        }

                        filteredPackagesList.addAll(allPackagesList); // Show all packages initially

                        requireActivity().runOnUiThread(() -> {
                            packageAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE); // Hide ProgressBar
                        });
                    } else {
                        Log.e("API Error", "Response body is null");

                        requireActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE); // Hide ProgressBar
                        });

                    }
                } catch (IOException e) {
                    Log.e("Network Error", "Failed to fetch data: " + e.getMessage(), e);


                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE); // Hide ProgressBar
                    });

                    String urlText = getString(R.string.url);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Network Error")
                                    .setMessage("Something went wrong with your network. Please check and try again.")
                                    .setCancelable(false)
                                    .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            loadPackages(urlText);
                                            dialog.dismiss();
                                        }
                                    });

                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    });

                }
            }
        };

        threadReq.start();
    }

    private void filterPackages(String query) {
        filteredPackagesList.clear();

        if (query.isEmpty()) {
            // Show all packages when query is empty
            filteredPackagesList.addAll(allPackagesList);
        } else {
            // Filter the list
            for (JsonObject packageObject : allPackagesList) {
                String packageName = packageObject.get("packageName").getAsString();
                if (packageName.toLowerCase().contains(query.toLowerCase())) {
                    filteredPackagesList.add(packageObject);
                }
            }
        }

        // Notify adapter about dataset changes
        if (packageAdapter != null) {
            packageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


class PackageViewHolder extends RecyclerView.ViewHolder {

    TextView textViewPackageName;
    TextView textViewPackagePrice;
    CardView cardViewPackage;

    ImageView imageViewPackage;

    public PackageViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewPackageName = itemView.findViewById(R.id.packageNameTextView);
        textViewPackagePrice = itemView.findViewById(R.id.packagePriceTextView);
        cardViewPackage = itemView.findViewById(R.id.packageCardView);
        imageViewPackage = itemView.findViewById(R.id.packageImageView);
    }

}

class PackageAdapter extends RecyclerView.Adapter<PackageViewHolder> {

    private final Context context;
    private final List<JsonObject> packagesList;

    public PackageAdapter(List<JsonObject> packagesList, Context context) {
        this.packagesList = packagesList;
        this.context = context;
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.package_view, parent, false);
        return new PackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
        JsonObject packageObject = packagesList.get(position);

        String base64Image = packageObject.get("resource").getAsString();
        if (base64Image != null && !base64Image.isEmpty()) {
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.imageViewPackage.setImageBitmap(bitmap);
        } else {
            holder.imageViewPackage.setImageResource(R.drawable.back_home02);
        }

        holder.textViewPackageName.setText(packageObject.get("packageName").getAsString());
        holder.textViewPackagePrice.setText(packageObject.get("pricePerPerson").getAsString());
        holder.textViewPackageName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                packageObject.addProperty("resource", "");
                Intent intent = new Intent(context, UpdatePackageActivity.class);
                intent.putExtra("data", new Gson().toJson(packageObject));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return packagesList.size();
    }
}


