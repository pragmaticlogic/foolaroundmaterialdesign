package com.codeprototype.kevin.foolaroundmaterialdesign;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by kevinle on 10/4/15.
 */
public class FoolAroundApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "B4pZyohhGgsqqmlqRIC8Qloq6NvDvX3RvHgjE24n", "QCd3jZh6zroxpCNpSHOuDARiZUkDPb3nmlnDAIVW");
        ParseObject obj = new ParseObject("TestObj");
        obj.put("foo", "bar");
        obj.saveInBackground();

    }
}
