package com.example.infotarghe.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        // Descrizioni per il MENU
        mText.setValue("SCANNER per scannerizzare le targhe\n\nTARGHE per visualizzare le targhe rilevate");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
