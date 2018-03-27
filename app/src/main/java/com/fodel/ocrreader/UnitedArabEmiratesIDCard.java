package com.fodel.ocrreader;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2018/3/12.
 */

public class UnitedArabEmiratesIDCard implements Parcelable {

    public String IDCardNumber;

    public String name;

    public UnitedArabEmiratesIDCard() {
    }


    protected UnitedArabEmiratesIDCard(Parcel in) {
        IDCardNumber = in.readString();
        name = in.readString();
    }

    public static final Creator<UnitedArabEmiratesIDCard> CREATOR = new Creator<UnitedArabEmiratesIDCard>() {
        @Override
        public UnitedArabEmiratesIDCard createFromParcel(Parcel in) {
            return new UnitedArabEmiratesIDCard(in);
        }

        @Override
        public UnitedArabEmiratesIDCard[] newArray(int size) {
            return new UnitedArabEmiratesIDCard[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(IDCardNumber);
        dest.writeString(name);
    }
}
