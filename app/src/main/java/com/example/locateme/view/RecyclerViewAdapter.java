package com.example.locateme.view;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locateme.R;
import com.example.locateme.databinding.PhoneNumberItemBinding;
import com.example.locateme.model.localdatabase.PhoneNumbers;
import com.example.locateme.viewmodel.MyViewModel;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private ArrayList<PhoneNumbers> allNumbers;
    MyViewModel viewModel;

    public RecyclerViewAdapter(ArrayList<PhoneNumbers> allNumbers, MyViewModel viewModel) {
        this.allNumbers = allNumbers;
        this.viewModel = viewModel;
    }

    public void setAllNumbers(ArrayList<PhoneNumbers> allNumbers) {
        this.allNumbers = allNumbers;
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PhoneNumberItemBinding binding= DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext())
                        , R.layout.phone_number_item,
                        parent,
                        false
                );
        return new MyViewHolder(binding,viewModel);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.binding.setMyPhoneNumber(allNumbers.get(position));
            holder.binding.itemSaveButton.setEnabled(false);
    }

    @Override
    public int getItemCount() {
         if(allNumbers!=null){
            return allNumbers.size();
        }
        else{
            return 0;
        }
    }
    public void updateAdapterSource(ArrayList<PhoneNumbers> list){
        this.allNumbers=list;
        notifyDataSetChanged();
    }
    static class MyViewHolder extends RecyclerView.ViewHolder{
        private PhoneNumberItemBinding binding;
        private MyViewModel viewModel;
        boolean isToggled = false;
        public MyViewHolder(PhoneNumberItemBinding binding,MyViewModel viewModel) {
            super(binding.getRoot());
            this.binding = binding;
            this.viewModel=viewModel;
            CountryCodePicker countryCodePicker=binding.ccp;
            countryCodePicker.registerCarrierNumberEditText(binding.PhoneNumber);

            countryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
                @Override
                public void onCountrySelected() {
                    binding.PhoneNumber.setText(""+binding.PhoneNumber.getText().toString().substring(binding.PhoneNumber.length() - 9));
                }
            });

            binding.threeDots.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (isToggled) {
                        // If already toggled, revert changes
                        if(watchForChanges()){
                            binding.setMyPhoneNumber(binding.getMyPhoneNumber());
                        }

                        binding.threeDots.setImageResource(R.drawable.editbutton);
                        binding.itemSaveButton.setEnabled(false);
                        binding.personName.setEnabled(false);
                        binding.PhoneNumber.setEnabled(false);
                        binding.checkboxOption1.setEnabled(false);
                        binding.checkboxOption2.setEnabled(false);
                        countryCodePicker.setCcpClickable(false);
                        Log.i("tag", "Changes reverted");
                    } else {
                        // If not toggled, apply changes
                        binding.threeDots.setImageResource(R.drawable.editpink);
                        binding.itemSaveButton.setEnabled(true);
                        binding.personName.setEnabled(true);
                        binding.PhoneNumber.setEnabled(true);
                        binding.checkboxOption1.setEnabled(true);
                        binding.checkboxOption2.setEnabled(true);
                        countryCodePicker.setCcpClickable(true);
                        Log.i("tag", "Changes applied");
                    }
                    // Toggle the state
                    isToggled = !isToggled;
                }
            });


            binding.itemSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                     if (binding.personName.getText()==null
                            || binding.PhoneNumber.getText()==null
                            ||binding.personName.getText().toString().isEmpty()
                            || binding.PhoneNumber.getText().toString().isEmpty()
                            || (!binding.checkboxOption1.isChecked() && !binding.checkboxOption2.isChecked())
                            || (binding.ccp.getFullNumber().length()<9)
                     )
                    {
                        Toast.makeText(view.getContext(), "Error in Input", Toast.LENGTH_SHORT).show();
                    }else {

                        PhoneNumbers phoneNumber =new PhoneNumbers(binding.ccp.getFullNumberWithPlus(),
                                binding.personName.getText().toString(),
                                binding.checkboxOption1.isChecked(),
                                binding.checkboxOption2.isChecked());
                        phoneNumber.setId(binding.getMyPhoneNumber().getId());
                        viewModel.addPhoneNumber(phoneNumber);
                        binding.personName.setEnabled(false);
                        binding.PhoneNumber.setEnabled(false);
                        binding.checkboxOption1.setEnabled(false);
                        binding.checkboxOption2.setEnabled(false);
                        countryCodePicker.setCcpClickable(false);
                        binding.threeDots.setImageResource(R.drawable.editbutton);
                    }
                }
            });


        }
        boolean watchForChanges(){
            boolean changed=false;
            PhoneNumbers item=binding.getMyPhoneNumber();
            if(!(item.getPhone_number().equals(binding.PhoneNumber.getText().toString()))){
                changed=true;
            }
            if(!(item.getName().equals(binding.personName.toString()))){
                changed=true;
            }

            if(!(item.isSend_to()==(binding.checkboxOption1.isChecked()))){
                changed=true;
            }
            if(!(item.isReceive_from()==(binding.checkboxOption2.isChecked()))){
                changed=true;
            }


            return changed;
        }


    }
}
