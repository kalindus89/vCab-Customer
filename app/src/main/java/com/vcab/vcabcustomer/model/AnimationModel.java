package com.vcab.vcabcustomer.model;

import android.os.SystemClock;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class AnimationModel {

    private boolean isRun;
    private GeoQueryModel geoQueryModel;

    public AnimationModel(boolean isRun, GeoQueryModel geoQueryModel) {
        this.isRun = isRun;
        this.geoQueryModel = geoQueryModel;
    }

    public boolean isRun() {
        return isRun;
    }

    public void setRun(boolean run) {
        isRun = run;
    }

    public GeoQueryModel getGeoQueryModel() {
        return geoQueryModel;
    }

    public void setGeoQueryModel(GeoQueryModel geoQueryModel) {
        this.geoQueryModel = geoQueryModel;
    }


}
