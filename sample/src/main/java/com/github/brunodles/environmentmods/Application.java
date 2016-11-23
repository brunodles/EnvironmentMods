package com.github.brunodles.environmentmods;

import com.github.brunodles.environmentmods.annotation.Moddable;

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
