package com.bourke.glimmr;

public class PhotoStreamGridFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/PhotoStreamGridFragment";

    public static PhotoStreamGridFragment newInstance() {
        return new PhotoStreamGridFragment();
    }

    @Override
    protected void startTask() {
        super.startTask();
        new LoadPhotostreamTask(this, mOAuth.getUser()).execute(mOAuth);
    }
}
