package com.example.locateme.model.localdatabase;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName ="phone_numbers" )
public class PhoneNumbers  {
    @ColumnInfo(name = "phone_number_id")
    @PrimaryKey(autoGenerate = true)
    private int id ;
    @ColumnInfo(name = "phone_number")
    private String phone_number ;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "send_to")
    private boolean send_to ;

    @ColumnInfo(name = "receive_from")
    private boolean receive_from ;

    public PhoneNumbers(String phone_number, String name, boolean send_to, boolean receive_from) {
        this.phone_number = phone_number;
        this.name = name;
        this.send_to = send_to;
        this.receive_from = receive_from;
    }

    public PhoneNumbers(String phone_number, String name) {
        this.phone_number = phone_number;
        this.name = name;
    }

    public PhoneNumbers() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public boolean isSend_to() {
        return send_to;
    }

    public void setSend_to(boolean send_to) {
        this.send_to = send_to;
    }

    public boolean isReceive_from() {
        return receive_from;
    }

    public void setReceive_from(boolean receive_from) {
        this.receive_from = receive_from;
    }






}
