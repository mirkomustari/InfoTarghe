package com.example.infotarghe.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Benvenuto in InfoTarghe!\n\nProgetto Sistemi Digitali M\n\n" +
                "Mirko Mustari\nMarco Di Silvio");
    }

    public LiveData<String> getText() {
        return mText;
    }
}