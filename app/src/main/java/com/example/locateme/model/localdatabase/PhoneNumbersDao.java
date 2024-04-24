package com.example.locateme.model.localdatabase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
@Dao
public interface PhoneNumbersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PhoneNumbers phoneNumber);

    @Delete
    void delete(PhoneNumbers phoneNumber);

    @Query("SELECT * FROM PHONE_NUMBERS")
    LiveData<List<PhoneNumbers>> getAllPhoneNumbers();

    @Query("SELECT * FROM PHONE_NUMBERS WHERE send_to = 1")
    LiveData<List<PhoneNumbers>> getSendToNumbers();

    @Query("SELECT * FROM PHONE_NUMBERS WHERE receive_from = 1")
    LiveData<List<PhoneNumbers>> getReceiveFromNumbers();
}
