package com.brunodles.environmentmods;


import com.brunodles.environmentmods.annotation.Moddable;

/**
 * Created by bruno on 23/11/16.
 */
@Moddable
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationMods.apply(this);
    }
}
