package lk.codebridge.travelmateadmin.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Objects;

import es.dmoral.toasty.Toasty;
import lk.codebridge.travelmateadmin.InsertNewAdminActivity;
import lk.codebridge.travelmateadmin.ManageUserActivity;
import lk.codebridge.travelmateadmin.R;
import lk.codebridge.travelmateadmin.StaticsActivity;


public class NewAdminFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_new_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.codebridge.travelmateadmin", Context.MODE_PRIVATE);

        String position = sharedPreferences.getString("position",null);


        CardView cardView = view.findViewById(R.id.add_admin_card);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Objects.equals(position, "Admin")){

                    Intent intent = new Intent(getContext(),InsertNewAdminActivity.class);
                    startActivity(intent);
                }else {

                    Toasty.warning(getActivity(),"You don't have permission to access this",Toasty.LENGTH_SHORT,true).show();

                }


            }
        });



        CardView cardViewTwo = view.findViewById(R.id.team_management_card);

        cardViewTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Objects.equals(position, "Admin")){

                    Intent intent = new Intent(getContext(), ManageUserActivity.class);
                    startActivity(intent);
                }else {

                    Toasty.warning(getActivity(),"You don't have permission to access this",Toasty.LENGTH_SHORT,true).show();

                }

            }
        });


        CardView cardViewStat = view.findViewById(R.id.manage_users_card);

        cardViewStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Objects.equals(position, "Admin")){

                    Intent intent = new Intent(getContext(), StaticsActivity.class);
                    startActivity(intent);
                }else {

                    Toasty.warning(getActivity(),"You don't have permission to access this",Toasty.LENGTH_SHORT,true).show();

                }

            }
        });



    }
}