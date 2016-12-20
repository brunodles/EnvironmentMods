package com.github.brunodles.environmentmods;

import com.github.brunodles.environmentmods.annotation.ModFor;

/**
 * Created by bruno on 23/11/16.
 */
public class StethoMod {

    @ModFor(Application.class)
    public static void addStetho(Application application) {

    }

    @ModFor(Application.class)
    public static void addDbAnalyse(Application application) {

    }
}
