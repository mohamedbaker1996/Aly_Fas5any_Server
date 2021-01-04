package com.simpelexo.alyfas5anyserver.ui;

import androidx.fragment.app.Fragment;



public class BaseFragment extends Fragment {
 public BaseActivity baseActivity;
    public void setUpActivity() {
        baseActivity = (BaseActivity) getActivity();

        assert baseActivity != null;
        baseActivity.baseFragment = this;
    }
 public void onBack(){
     baseActivity.superBackPressed();
    }
}