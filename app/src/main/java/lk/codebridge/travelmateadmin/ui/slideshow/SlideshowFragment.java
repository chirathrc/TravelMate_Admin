package lk.codebridge.travelmateadmin.ui.slideshow;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import lk.codebridge.travelmateadmin.databinding.FragmentSlideshowBinding;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;

    private List<JsonObject> orderList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressBar progressBar = view.findViewById(R.id.progressBar2);

        RecyclerView recyclerView = view.findViewById(R.id.orderPackageRecycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        progressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {

                OkHttpClient okHttpClient = new OkHttpClient();

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.codebridge.travelmateadmin", Context.MODE_PRIVATE);

                String email = sharedPreferences.getString("email", null);

                String url = getString(R.string.url);

                Request request = new Request.Builder().url(url + "/travelmate/packageOrdering/getOrderedDetails?email=" + email).build();

                try {
                    Response response = okHttpClient.newCall(request).execute();

                    if (response.isSuccessful()) {

                        String data = response.body().string();

                        Log.i("responseData", data);

                        Gson gson = new Gson();

                        JsonArray jsonArray = gson.fromJson(data, JsonArray.class);


                        for (JsonElement element : jsonArray) {

                            JsonObject jsonObject = element.getAsJsonObject();
                            orderList.add(jsonObject);

                        }

                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                OrderAdapter orderAdapter = new OrderAdapter(orderList, requireContext());
                                recyclerView.setAdapter(orderAdapter);
                                progressBar.setVisibility(View.GONE);

                            }
                        });


                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        }).start();


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


class OrderViewHolder extends RecyclerView.ViewHolder {

    TextView textViewPackageName;
    TextView textViewPackagePrice;

    TextView buyerName;

    TextView buyerEmail;

    TextView buyerMobile;
    TextView packageId;
    TextView personCount;
    TextView priceTotal;
    TextView days;
    TextView nights;

    TextView orderId;

    TextView checking_date;

    TextView checkout_date;


    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewPackageName = itemView.findViewById(R.id.order_package_name);
        textViewPackagePrice = itemView.findViewById(R.id.order_package_price);
        buyerName = itemView.findViewById(R.id.customer_name);
        buyerEmail = itemView.findViewById(R.id.customer_email);
        buyerMobile = itemView.findViewById(R.id.customer_mobile);
        packageId = itemView.findViewById(R.id.package_id);
        personCount = itemView.findViewById(R.id.people_details);
        priceTotal = itemView.findViewById(R.id.total_price);
        days = itemView.findViewById(R.id.text_days);
        nights = itemView.findViewById(R.id.text_nights);
        orderId = itemView.findViewById(R.id.order_id);
        checking_date = itemView.findViewById(R.id.checkin_date);
        checkout_date = itemView.findViewById(R.id.checkout_date);

    }

}


class OrderAdapter extends RecyclerView.Adapter<OrderViewHolder> {

    private final Context context;
    private final List<JsonObject> orderPackagesList;

    public OrderAdapter(List<JsonObject> packagesList, Context context) {
        this.orderPackagesList = packagesList;
        this.context = context;
    }


    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {

        JsonObject jsonObject = orderPackagesList.get(position);
        String pricePerPerson = jsonObject.get("travelPackage").getAsJsonObject().get("pricePerPerson").getAsString();

        holder.textViewPackageName.setText(jsonObject.get("travelPackage").getAsJsonObject().get("packageName").getAsString());
        holder.textViewPackagePrice.setText(jsonObject.get("travelPackage").getAsJsonObject().get("pricePerPerson").getAsString());
        holder.buyerName.setText(jsonObject.get("user").getAsJsonObject().get("name").getAsString());
        holder.buyerEmail.setText(jsonObject.get("user").getAsJsonObject().get("email").getAsString());
        holder.buyerMobile.setText(jsonObject.get("user").getAsJsonObject().get("mobile").getAsString());
        holder.packageId.setText(jsonObject.get("travelPackage").getAsJsonObject().get("id").getAsString());
        holder.days.setText(jsonObject.get("travelPackage").getAsJsonObject().get("days").getAsString());
        holder.nights.setText(jsonObject.get("travelPackage").getAsJsonObject().get("nights").getAsString());
        holder.personCount.setText(jsonObject.get("persons").getAsString());
        holder.orderId.setText(jsonObject.get("id").getAsString());
        holder.checking_date.setText(jsonObject.get("checkIn").getAsString());
        holder.checkout_date.setText(jsonObject.get("checkout").getAsString());

        float pricePerPersonValue = Float.parseFloat(pricePerPerson);
        int persons = Integer.parseInt(jsonObject.get("persons").getAsString());

        int total = (int) pricePerPersonValue * persons;

        holder.priceTotal.setText(String.valueOf(total));

    }

    @Override
    public int getItemCount() {
        return orderPackagesList.size();
    }


}