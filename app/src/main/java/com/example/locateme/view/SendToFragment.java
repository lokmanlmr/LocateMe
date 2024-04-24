package com.example.locateme.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.locateme.R;
import com.example.locateme.databinding.FragmentSendToBinding;
import com.example.locateme.model.localdatabase.PhoneNumbers;
import com.example.locateme.model.localdatabase.PhoneNumbersDataBase;
import com.example.locateme.viewmodel.MyViewModel;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.List;


public class SendToFragment extends Fragment {

    FragmentSendToBinding binding;
    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    PhoneNumbersDataBase phoneNumbersDataBase;
    ArrayList<PhoneNumbers> allPhoneNumbers =new ArrayList<>();
    ArrayList<PhoneNumbers> SendToNumbers =new ArrayList<>();
    ArrayList<PhoneNumbers> ReceiveFromNumbers =new ArrayList<>();
    LinearLayout SAndRLayout,SendLayout,ReceiveLayout;
    CountryCodePicker countryCodePicker;

    public SendToFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentSendToBinding.inflate(inflater, container, false);

        countryCodePicker=binding.ccp;

        countryCodePicker.registerCarrierNumberEditText(binding.TypePhone);


        binding.plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( binding.constraintLayout.getVisibility() == View.VISIBLE) {
                    binding.constraintLayout.setVisibility(View.GONE);
                } else {
                    binding.constraintLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        //Creating an Instance of the database
        phoneNumbersDataBase= PhoneNumbersDataBase.getInstance(requireContext());

        //ViewModel
        MyViewModel viewModel=new ViewModelProvider(this).
                get(MyViewModel.class);

        recyclerView=binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);


        viewModel.getAllContacts().observe(getViewLifecycleOwner(), new Observer<List<PhoneNumbers>>() {
            @Override
            public void onChanged(List<PhoneNumbers> phoneNumbers) {
                allPhoneNumbers.clear();
                allPhoneNumbers.addAll(phoneNumbers);
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.getSendToNumbers().observe(getViewLifecycleOwner(), new Observer<List<PhoneNumbers>>() {
            @Override
            public void onChanged(List<PhoneNumbers> phoneNumbers) {
                SendToNumbers.clear();
                SendToNumbers.addAll(phoneNumbers);
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.getReceiveFromNumbers().observe(getViewLifecycleOwner(), new Observer<List<PhoneNumbers>>() {
            @Override
            public void onChanged(List<PhoneNumbers> phoneNumbers) {
                ReceiveFromNumbers.clear();
                ReceiveFromNumbers.addAll(phoneNumbers);
                adapter.notifyDataSetChanged();
            }
        });





        //Adapter
        adapter=new RecyclerViewAdapter(allPhoneNumbers,viewModel);
        //set the adapter
        recyclerView.setAdapter(adapter);

        ImageButton saveBtn=binding.saveBtn;
        EditText name,phone;
        CheckBox option1,option2;
        name=binding.typeName;
        phone=binding.TypePhone;
        option1=binding.checkboxOption1;
        option2=binding.checkboxOption2;
        //here we implement the logic of adding a new contact
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText()==null
                        || phone.getText()==null
                        ||name.getText().toString().isEmpty()
                        || phone.getText().toString().isEmpty()
                        || (!option1.isChecked() && !option2.isChecked())
                        || (phone.getText().toString().length()<12))
                {
                    Toast.makeText(requireContext(), "Error in Input ", Toast.LENGTH_SHORT).show();
                    Log.v("tag","name.getText()==null "+(name.getText()==null));
                    Log.v("tag","phone.getText()==null "+(phone.getText()==null));
                    Log.v("tag","name.getText().toString().isEmpty() "+(name.getText().toString().isEmpty()));
                    Log.v("tag","phone.getText().toString().isEmpty() "+(phone.getText().toString().isEmpty()));
                    Log.v("tag","phone.getText().toString().length()<15 "+(phone.getText().toString().length()<15));
                }else {
                    PhoneNumbers phoneNumber =new PhoneNumbers(countryCodePicker.getFullNumberWithPlus(),
                            name.getText().toString(),option1.isChecked(),option2.isChecked());
                    viewModel.addPhoneNumber(phoneNumber);
                    name.setText("");
                    phone.setText("");
                    option1.setChecked(false);
                    option2.setChecked(false);
                }

            }
        });


       SAndRLayout=binding.SendAndReceiveLL;
       SendLayout =binding.SendLL;
       ReceiveLayout=binding.ReceiveLL;

       SAndRLayout.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               turnBackgroundsToDefault();
               SAndRLayout.setBackgroundResource(R.drawable.list_item_selected);
               adapter.updateAdapterSource(allPhoneNumbers);

           }
       });
        SendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnBackgroundsToDefault();
                SendLayout.setBackgroundResource(R.drawable.list_item_selected);
                adapter.updateAdapterSource(SendToNumbers);

            }
        });
        ReceiveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnBackgroundsToDefault();
                ReceiveLayout.setBackgroundResource(R.drawable.list_item_selected);
                adapter.updateAdapterSource(ReceiveFromNumbers);


            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                PhoneNumbers phoneNumber=allPhoneNumbers.get(viewHolder.getAbsoluteAdapterPosition());
                viewModel.deletePhoneNumber(phoneNumber);
            }
        }).attachToRecyclerView(recyclerView);

        return binding.getRoot() ;
    }

    public void turnBackgroundsToDefault(){
        SAndRLayout.setBackgroundResource(R.drawable.list_item_unselected);
        SendLayout.setBackgroundResource(R.drawable.list_item_unselected);
        ReceiveLayout.setBackgroundResource(R.drawable.list_item_unselected);
    }


}