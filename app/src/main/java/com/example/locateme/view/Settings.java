package com.example.locateme.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.locateme.R;
import com.example.locateme.databinding.FragmentSettingsBinding;
import com.example.locateme.model.SendSmsAndLocation;
import com.example.locateme.model.localdatabase.PhoneNumbers;
import com.example.locateme.viewmodel.MyViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Settings extends Fragment implements AdapterView.OnItemSelectedListener {

    ArrayList<PhoneNumbers> SendToNumbers =new ArrayList<>();
    SwitchCompat switchBtn;
    Spinner spinner;
    FragmentSettingsBinding binding;
    private static final String GPS_ACTION = "android.location.PROVIDERS_CHANGED";

    public Settings() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentSettingsBinding.inflate(inflater,container,false);



        //ViewModel
        MyViewModel viewModel=new ViewModelProvider(this).get(MyViewModel.class);

        //here we observe the livedata list of getSendToNumbers() and put it value in the SendToNumbers
        viewModel.getSendToNumbers().observe(getViewLifecycleOwner(), new Observer<List<PhoneNumbers>>() {
            @Override
            public void onChanged(List<PhoneNumbers> phoneNumbers) {
                SendToNumbers.clear();
                SendToNumbers.addAll(phoneNumbers);

            }
        });

        switchBtn = binding.switchCompat;
        switchBtn.setChecked(retrieveState());
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(!areAllPermissionsGranted() || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                switchBtn.setChecked(false);
                saveState(false);
            }
        }


        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if(areAllPermissionsGranted()){
                        switchBtn.setChecked(false);

                        if (!(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_BACKGROUND_LOCATION)==PackageManager.PERMISSION_GRANTED)) {
                            switchBtn.setChecked(false);
                            showCustomDialog("ACCESS_BACKGROUND_LOCATION", "we need to access your background location ",
                                    "Allow", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intent = new Intent();
                                            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                                            intent.setData(uri);
                                            // Verify that the intent can be resolved before starting
                                            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                                                startActivity(intent);
                                            }
                                        }
                                    },"Deny",null);
                            return;
                        }
                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            saveState(true);
                            switchBtn.setChecked(true);
                        }else {
                            // Prompt the user to enable GPS
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                            builder.setMessage("GPS is disabled. Do you want to enable it?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // Open GPS settings if the user agrees
                                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            requireContext().startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // Call methodB if user refuses to enable GPS
                                            saveState(false);
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                            // Register broadcast receiver to listen for GPS provider changes
                            requireContext().registerReceiver(gpsReceiver, new IntentFilter(GPS_ACTION));

                        }


                    } else if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS) ||
                            shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS) ||
                            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                            shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE) ||
                            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                    ) {
                        switchBtn.setChecked(false);
                        saveState(false);
                        showCustomDialog("Needed Permissions", "This App needs The following permissions to work properly ", "Allow", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                activityResultLauncher.launch(new String[]{
                                        Manifest.permission.SEND_SMS,Manifest.permission.RECEIVE_SMS,Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_PHONE_STATE,
                                        Manifest.permission.POST_NOTIFICATIONS
                                });
                            }
                        },"Deny",null);

                    } else{
                        switchBtn.setChecked(false);
                        saveState(false);
                        activityResultLauncher.launch(new String[]{
                                Manifest.permission.SEND_SMS,Manifest.permission.RECEIVE_SMS,Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.POST_NOTIFICATIONS
                        });
                    }

                } else {
                    // Switch is OFF
                    //set the value of the variable to false in shared preference
                    Intent serviceIntent = new Intent(requireContext(), SendSmsAndLocation.class);
                    requireContext().stopService(serviceIntent);
                    saveState(false);
                }
            }
        });

        spinner=binding.spinner;
        SharedPreferences preferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String savedSelectedItem = preferences.getString("selectedItem", "");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.SpinnerTimeArray,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        spinner.setEnabled(false);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        if (!savedSelectedItem.isEmpty()) {
            int position = adapter.getPosition(savedSelectedItem);
            spinner.setSelection(position);
        }

        binding.setMsg(preferences.getString("sentMsg","My location is "));
        binding.setStartWord(preferences.getString("startWord","start tracking"));
        binding.setStopWord(preferences.getString("stopWord","stop tracking "));


        ImageButton save=binding.saveButton;
        ImageButton edit=binding.editButton;

        EditText sentMsg=binding.locationMsg,startWord=binding.startWord,stopWord=binding.stopWord;
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                boolean changesMade = false;

                if (!sentMsg.getText().toString().isEmpty()) {
                    if (!sentMsg.getText().toString().equals(preferences.getString("sentMsg", ""))) {
                        editor.putString("sentMsg", sentMsg.getText().toString());
                        changesMade = true;
                    }
                } else {
                    Toast.makeText(requireContext(), "sentMsg can't be empty", Toast.LENGTH_SHORT).show();
                }

                if (!startWord.getText().toString().isEmpty()) {
                    if (!startWord.getText().toString().equals(preferences.getString("startWord", ""))) {
                        editor.putString("startWord", startWord.getText().toString());
                        changesMade = true;
                    }
                } else {
                    Toast.makeText(requireContext(), "startWord can't be empty", Toast.LENGTH_SHORT).show();
                }

                if (!stopWord.getText().toString().isEmpty()) {
                    if (!stopWord.getText().toString().equals(preferences.getString("stopWord", ""))) {
                        editor.putString("stopWord", stopWord.getText().toString());
                        changesMade = true;
                    }
                } else {
                    Toast.makeText(requireContext(), "stopWord can't be empty", Toast.LENGTH_SHORT).show();
                }
                edit.setImageResource(R.drawable.editbutton);
                sentMsg.setEnabled(false);
                startWord.setEnabled(false);
                stopWord.setEnabled(false);
                spinner.setEnabled(false);
                if (changesMade) {
                    editor.apply();
                    Toast.makeText(requireContext(), "Changes were Saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "No changes were made", Toast.LENGTH_SHORT).show();
                }
            }
        });


        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.setImageResource(R.drawable.editpink);
                sentMsg.setEnabled(true);
                startWord.setEnabled(true);
                stopWord.setEnabled(true);
                spinner.setEnabled(true);
            }
        });







        return binding.getRoot();
    }

    private ActivityResultLauncher<String[]> activityResultLauncher=registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions()
            , new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    boolean allGranted=true;
                    for (String key: result.keySet()) {
                        allGranted=allGranted && result.get(key);
                    }
                    if(allGranted){
                        switchBtn.setChecked(true);
                        saveState(true);
                        //set the value of the variable to true in shared preference
                    }else{
                        if(!(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.SEND_SMS)==PackageManager.PERMISSION_GRANTED)){
                            showCustomDialog("SEND SMS Permission", "How the heck would this app work without sms ! ", "Go to Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent();
                                    intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                                    intent.setData(uri);
                                    // Verify that the intent can be resolved before starting
                                    if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                                        startActivity(intent);
                                    }
                                }
                            },"Cancel",null);
                        }
                        if(!(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)){
                            showCustomDialog("ACCESS FINE LOCATION Permission", "come on do it your self now ! ", "Go to Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent();
                                    intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                                    intent.setData(uri);
                                    // Verify that the intent can be resolved before starting
                                    if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                                        startActivity(intent);
                                    }
                                }
                            },"Cancel",null);
                        }
                        if(!(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED)){
                            showCustomDialog("READ_PHONE_STATE Permission", "idk why we need this but we do ! ", "Go to Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent();
                                    intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                                    intent.setData(uri);
                                    // Verify that the intent can be resolved before starting
                                    if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                                        startActivity(intent);
                                    }
                                }
                            },"Cancel",null);
                        }
                        if(!(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)){
                            showCustomDialog("POST_NOTIFICATIONS Permission", "idk why we need this but we do ! ", "Go to Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent();
                                    intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                                    intent.setData(uri);
                                    // Verify that the intent can be resolved before starting
                                    if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                                        startActivity(intent);
                                    }
                                }
                            },"Cancel",null);
                        }


                    }
                }
            }

    );

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private boolean areAllPermissionsGranted(){
        //This Method checks if all the necessary Permissions are Granted by the user or not
        return ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.SEND_SMS)==PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.RECEIVE_SMS)==PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED;
    }


    private void showCustomDialog(String Title,String Message,
                                  String PositiveBtnTitle,DialogInterface.OnClickListener PositiveListener,
                                  String NegativeBtnTitle,DialogInterface.OnClickListener NegativeListener){
        //simple method to show a dialog with a message,title,2 Buttons
            AlertDialog.Builder builder=new AlertDialog.Builder(requireContext());
            builder.setTitle(Title)
                    .setMessage(Message)
                    .setPositiveButton(PositiveBtnTitle,PositiveListener)
                    .setNegativeButton(NegativeBtnTitle,NegativeListener);
            builder.create().show();

    }
    public void saveState(boolean value) {
        //This method Saves the New Value of the Boolean variable
        // that has to be true for the Service to get started
        SharedPreferences preferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isBoolean", value);
        editor.apply();
    }

    private boolean retrieveState() {
        //This method Retrieves the value of the Boolean variable
        // that has to be true for the Service to get started
        SharedPreferences preferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return preferences.getBoolean("isBoolean", false);
    }

    private final BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        //This dynamic Broadcast Receiver listens for the GPS state
        // if it isn't activated then it should toggle off the switch button and saves it's state as "false"
        @Override
        public void onReceive(Context context, Intent intent) {
            final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                saveState(false);
                switchBtn.setChecked(false);
            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        // Register broadcast receiver to listen for GPS provider changes
        requireActivity().registerReceiver(gpsReceiver, new IntentFilter(GPS_ACTION));
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        switchBtn.setChecked(retrieveState());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(!areAllPermissionsGranted() || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                switchBtn.setChecked(false);
                saveState(false);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences preferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        EditText sentMsg=binding.locationMsg,satrtWord=binding.startWord,stopWord=binding.stopWord;

        if(!sentMsg.getText().toString().isEmpty()){
            editor.putString("sentMsg",sentMsg.getText().toString()+" ");
        }
        if(!satrtWord.getText().toString().isEmpty()){
            editor.putString("startWord",satrtWord.getText().toString());
        }
        if(!stopWord.getText().toString().isEmpty()){
            editor.putString("stopWord",stopWord.getText().toString());
        }

        editor.apply();
        saveState(switchBtn.isChecked());
        // Unregister broadcast receiver when fragment is paused
        requireActivity().unregisterReceiver(gpsReceiver);


    }

    @Override
    public void onStop() {
        super.onStop();
        saveState(switchBtn.isChecked());
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        String selectedItem = adapterView.getItemAtPosition(pos).toString();
        SharedPreferences preferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        long period=preferences.getLong("period",15*60*1000);
        switch (selectedItem){
            case "10 Minutes":
                period=10*60*1000;
                break;
            case "15 Minutes":
                period=15*60*1000;
                break;
            case "20 Minutes":
                period=20*60*1000;
                break;
            case "30 Minutes":
                period=30*60*1000;
                break;
            case "1 Hours":
                period=60*60*1000;
                break;
            case "2 Hours":
                period=2*60*60*1000;
                break;
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("period", period);
        editor.putString("selectedItem", selectedItem);
        editor.apply();
        Log.v("period is "," "+period);

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}