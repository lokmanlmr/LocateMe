package com.example.locateme.model.localdatabase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {PhoneNumbers.class},version = 1)
public abstract class PhoneNumbersDataBase extends RoomDatabase {
    public abstract PhoneNumbersDao getPhoneNumbersDao();
    private static PhoneNumbersDataBase dbInstance;
    public static synchronized PhoneNumbersDataBase getInstance(Context context){
        //we check if the db instance doesn't exist first
        //if it doesn't we create a new one
        if(dbInstance==null){
            dbInstance= Room.databaseBuilder(context.getApplicationContext(),
                    PhoneNumbersDataBase.class,
                    "phone_number_db").fallbackToDestructiveMigration().build();
        }
        // if there is an existing db instance we just return it
        return dbInstance ;
    }
}
