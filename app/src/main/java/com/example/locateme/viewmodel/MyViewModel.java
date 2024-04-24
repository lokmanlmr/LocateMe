package com.example.locateme.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;


import com.example.locateme.model.Repository;
import com.example.locateme.model.localdatabase.PhoneNumbers;

import java.util.List;

public class MyViewModel extends AndroidViewModel {
    private Repository myrepository;
    private LiveData<List<PhoneNumbers>> allPhoneNumbers;
    private LiveData<List<PhoneNumbers>> sentToPhoneNumbers;
    private LiveData<List<PhoneNumbers>> ReceivedFromPhoneNumbers;




    public MyViewModel(@NonNull Application application) {
        super(application);
        this.myrepository=new Repository(application);
    }

    public LiveData<List<PhoneNumbers>> getAllContacts(){
        allPhoneNumbers= myrepository.getAllPhoneNumbers();
        return allPhoneNumbers;
    }
    public LiveData<List<PhoneNumbers>> getSendToNumbers(){
        sentToPhoneNumbers= myrepository.getSendToNumbers();
        return sentToPhoneNumbers;
    }
    public LiveData<List<PhoneNumbers>> getReceiveFromNumbers(){
        ReceivedFromPhoneNumbers= myrepository.getReceiveFromNumbers();
        return ReceivedFromPhoneNumbers;
    }

    public void addPhoneNumber(PhoneNumbers phoneNumber) {
        myrepository.addPhoneNumber(phoneNumber);
    }

    public void deletePhoneNumber(PhoneNumbers phoneNumber) {
        myrepository.deletePhoneNumber(phoneNumber);
    }

}
