package com.example.infotarghe.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("BENVENUTI IN INFOTARGHE\n\nProgetto Sistemi Digitali M\n" +
                "di Mirko Mustari e Marco Di Silvio\n\n" +
                "ISTRUZIONI:\n'Scanner' per scannerizzare le targhe.\n'Targhe' per visualizzare le targhe rilevate");
    }

    public LiveData<String> getText() {
        return mText;
    }
}