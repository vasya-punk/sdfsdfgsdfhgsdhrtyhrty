package com.example.myapplication2;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import com.example.myapplication2.BR;

public class ItemVO extends BaseObservable {
    private boolean checked;

    @Bindable
    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;

        notifyPropertyChanged(BR.checked);
    }

    private boolean currentAccent;
    @Bindable
    public boolean isCurrentAccent() {
        return currentAccent;
    }

    public void setCurrentAccent(boolean currentAccent) {
        this.currentAccent = currentAccent;

        notifyPropertyChanged(BR.currentAccent);
    }

    public View.OnClickListener clicker = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            setChecked(!checked);
        }
    };
}
