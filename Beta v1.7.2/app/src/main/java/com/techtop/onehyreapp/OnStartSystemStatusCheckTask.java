package com.techtop.onehyreapp;

import android.os.AsyncTask;

import com.techtop.onehyreapp.databinding.ActivityMainBinding;

public class OnStartSystemStatusCheckTask extends AsyncTask<Void, Void, Void> {

    private ActivityMainBinding binding;
    private  SplashScreenActivity splashScreenActivity;

    public OnStartSystemStatusCheckTask(SplashScreenActivity splashScreenActivity){
        this.splashScreenActivity = splashScreenActivity;
    }

    @Override
    protected Void doInBackground(Void... params) {
        splashScreenActivity.systemStatusCheck();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        //Intent intent = new Intent(splashScreenActivity, MainActivity.class);
        //splashScreenActivity.startActivity(intent);
        //splashScreenActivity.finish();
    }
}
