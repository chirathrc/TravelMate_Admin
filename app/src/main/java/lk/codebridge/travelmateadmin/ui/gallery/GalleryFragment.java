package lk.codebridge.travelmateadmin.ui.gallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import es.dmoral.toasty.Toasty;
import lk.codebridge.travelmateadmin.R;
import lk.codebridge.travelmateadmin.databinding.FragmentGalleryBinding;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;

    private Uri selectImage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String url = getString(R.string.url);


        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {

                        ImageView imageView = view.findViewById(R.id.img_upload_preview);

                        imageView.setImageURI(uri);
                        selectImage = uri;

                        Log.d("PhotoPicker", "Selected URI: " + uri);

                    } else {

                        Log.d("PhotoPicker", "No media selected");

                    }
                });

        CardView button = view.findViewById(R.id.card_image_upload);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());

            }
        });


        Button buttonSubmit = view.findViewById(R.id.btn_update_package);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText travelPackageNameTxt = view.findViewById(R.id.input_package_name);
                EditText pricePerPersonTxt = view.findViewById(R.id.input_price_per_person);
                EditText placeDescriptionOneTxt = view.findViewById(R.id.input_place_desc1);
                EditText placeDescriptionTwoTxt = view.findViewById(R.id.input_place_desc2);
                EditText travelNightsTxt = view.findViewById(R.id.input_nights);
                EditText travelDaysTxt = view.findViewById(R.id.input_days);
                EditText travelPlaceWikiTxt = view.findViewById(R.id.input_wikipedia_url);
                EditText travelPlaceTxt = view.findViewById(R.id.input_package_city);
                EditText locationPlaceTxt = view.findViewById(R.id.locationUrl);

                String travelPackageName = travelPackageNameTxt.getText().toString();
                String pricePerPerson = pricePerPersonTxt.getText().toString();
                String placeDescOne = placeDescriptionOneTxt.getText().toString();
                String placeDescTwo = placeDescriptionTwoTxt.getText().toString();
                String travelNights = travelNightsTxt.getText().toString();
                String travelDays = travelDaysTxt.getText().toString();
                String travelWiki = travelPlaceWikiTxt.getText().toString();
                String travelCity = travelPlaceTxt.getText().toString();
                String urlLoaction = locationPlaceTxt.getText().toString();

                if (travelPackageName.isEmpty() || pricePerPerson.isEmpty() ||
                        placeDescOne.isEmpty() || placeDescTwo.isEmpty() ||
                        travelNights.isEmpty() || travelDays.isEmpty() || travelWiki.isEmpty() || travelCity.isEmpty() || urlLoaction.isEmpty()) {

                    Toasty.error(requireContext(), "You must fill all details to add package.", Toasty.LENGTH_SHORT, true).show();
                } else if (selectImage == null) {
                    Toasty.error(requireContext(), "Add image for your package.", Toasty.LENGTH_SHORT, true).show();
                } else {
//                    Toasty.success(requireContext(), "Success", Toasty.LENGTH_SHORT, true).show();

                    Thread threadNewPackageCreate = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            File file;
                            try {
                                file = getFileFromUri(selectImage);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/jpeg"));

                            MultipartBody multipartBody = new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("image", file.getName(), fileBody)
                                    .addFormDataPart("packageName", travelPackageName)
                                    .addFormDataPart("descOne", placeDescOne)
                                    .addFormDataPart("descTwo", placeDescTwo)
                                    .addFormDataPart("pricePerPerson", pricePerPerson)
                                    .addFormDataPart("days", travelDays)
                                    .addFormDataPart("nights", travelNights)
                                    .addFormDataPart("wikiUrl", travelWiki)
                                    .addFormDataPart("city", travelCity)
                                    .addFormDataPart("location", urlLoaction)
                                    .build();

                            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("lk.codebridge.travelmateadmin", Context.MODE_PRIVATE);

                            String email = sharedPreferences.getString("email", null);

                            Request request = new Request.Builder().url(url + "/travelmate/package/addNew?email=" + email).post(multipartBody).build();

                            OkHttpClient okHttpClient = new OkHttpClient();

                            try {

                                Response response = okHttpClient.newCall(request).execute();

                                boolean Issuccess = Boolean.parseBoolean(response.body().string());

                                if (response.isSuccessful()) {

                                    if (Issuccess) {

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                Toasty.success(requireContext(), "Package Added Succesfully.", Toasty.LENGTH_SHORT, true).show();

                                                travelPackageNameTxt.setText("");
                                                pricePerPersonTxt.setText("");
                                                placeDescriptionOneTxt.setText("");
                                                placeDescriptionTwoTxt.setText("");
                                                travelNightsTxt.setText("");
                                                travelDaysTxt.setText("");
                                                travelPlaceWikiTxt.setText("");
                                                travelPlaceTxt.setText("");
                                                locationPlaceTxt.setText("");

                                            }
                                        });

                                    } else {

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                Toasty.error(requireContext(), "Something went wrong, Try again later..", Toasty.LENGTH_SHORT, true).show();

                                            }
                                        });

                                    }

                                } else {

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            Toasty.error(requireContext(), "Something went wrong, Try again later..", Toasty.LENGTH_SHORT, true).show();

                                        }
                                    });

                                }


                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    });

                    threadNewPackageCreate.start();

                }
            }
        });

    }

    private File getFileFromUri(Uri uri) throws IOException {
        File file = new File(requireContext().getCacheDir(), "selected_image.jpg");

        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(file)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return file;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}