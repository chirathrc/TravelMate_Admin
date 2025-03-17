package lk.codebridge.travelmateadmin.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import lk.codebridge.travelmateadmin.R;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class AdminProfileFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.codebridge.travelmateadmin", Context.MODE_PRIVATE);


        EditText etFirstName = view.findViewById(R.id.etFirstName);
        EditText etLastName = view.findViewById(R.id.etLastName);
        EditText etMobile = view.findViewById(R.id.etMobile);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etPosition = view.findViewById(R.id.etPosition);
        EditText etEmail = view.findViewById(R.id.etEmail);

        etFirstName.setText(sharedPreferences.getString("FirstName", ""));
        etLastName.setText(sharedPreferences.getString("LastName", ""));
        etMobile.setText(sharedPreferences.getString("mobile", ""));
        etPassword.setText(sharedPreferences.getString("password", ""));
        etPosition.setText(sharedPreferences.getString("position", ""));
        etEmail.setText(sharedPreferences.getString("email", ""));

        Button update = view.findViewById(R.id.btnUpdate);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String firstName = etFirstName.getText().toString();
                String lastName = etLastName.getText().toString();
                String mobile = etMobile.getText().toString();
                String password = etPassword.getText().toString();
                String email = etEmail.getText().toString();

                if (firstName.isEmpty() || firstName.trim().isEmpty()) {

                    Toasty.warning(getContext(), "Invalid first name", Toasty.LENGTH_SHORT, true).show();

                } else if (lastName.isEmpty() || lastName.trim().isEmpty()) {

                    Toasty.warning(getContext(), "Invalid last name", Toasty.LENGTH_SHORT, true).show();

                } else if (mobile.isEmpty() || mobile.trim().isEmpty() || !mobile.matches("^(?:\\+94|0)(7[01245678])\\d{7}$")) {

                    Toasty.warning(getContext(), "Invalid mobile number", Toasty.LENGTH_SHORT, true).show();

                } else if (password.isEmpty() || password.trim().isEmpty()) {

                    Toasty.warning(getContext(), "Invalid password", Toasty.LENGTH_SHORT, true).show();

                } else {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            JsonObject userDataUpdate = new JsonObject();
                            userDataUpdate.addProperty("firstName", firstName);
                            userDataUpdate.addProperty("lastName", lastName);
                            userDataUpdate.addProperty("mobile", mobile);
                            userDataUpdate.addProperty("password", password);
                            userDataUpdate.addProperty("email", email);

                            String url = getString(R.string.url);
                            OkHttpClient okHttpClient = new OkHttpClient();

                            RequestBody body = RequestBody.create(new Gson().toJson(userDataUpdate), MediaType.get("application/json"));

                            Request request = new Request.Builder().url(url+"/travelmate/admin/updateAdmin").post(body).build();

                            try {
                                Response response = okHttpClient.newCall(request).execute();

                                if (response.isSuccessful()) {


                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toasty.success(getContext(), "Updated Successfully.", Toasty.LENGTH_SHORT, true).show();
                                        }
                                    });

                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("FirstName", firstName);
                                    editor.putString("LastName", lastName);
                                    editor.putString("mobile", mobile);
                                    editor.putString("password", password);
                                    editor.apply();


                                } else {

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toasty.error(getContext(), "Something went wrong, try again later.", Toasty.LENGTH_SHORT, true).show();
                                        }
                                    });

                                }
                            } catch (IOException e) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toasty.error(getContext(), "Something went wrong, try again later.", Toasty.LENGTH_SHORT, true).show();
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