package com.example.locateme.model;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;


import androidx.lifecycle.LiveData;

import com.example.locateme.model.localdatabase.PhoneNumbers;
import com.example.locateme.model.localdatabase.PhoneNumbersDao;
import com.example.locateme.model.localdatabase.PhoneNumbersDataBase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Repository {
    private final PhoneNumbersDao phoneNumbersDao;
    ExecutorService executor;
    Handler handler;

    public Repository(Application application) {
        PhoneNumbersDataBase phoneNumbersDataBase= PhoneNumbersDataBase.getInstance(application);
        this.phoneNumbersDao = phoneNumbersDataBase.getPhoneNumbersDao();

        executor= Executors.newSingleThreadExecutor();
        handler= new Handler(Looper.getMainLooper());

    }

    public void addPhoneNumber(PhoneNumbers phoneNumber){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                phoneNumbersDao.insert(phoneNumber);
            }
        });
    }

    public void deletePhoneNumber(PhoneNumbers phoneNumber){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                phoneNumbersDao.delete(phoneNumber);
            }
        });
    }

    public LiveData<List<PhoneNumbers>> getAllPhoneNumbers(){
        return phoneNumbersDao.getAllPhoneNumbers();
    }
    public LiveData<List<PhoneNumbers>> getSendToNumbers(){
        return phoneNumbersDao.getSendToNumbers();
    }
    public LiveData<List<PhoneNumbers>> getReceiveFromNumbers(){
        return phoneNumbersDao.getReceiveFromNumbers();
    }


}
