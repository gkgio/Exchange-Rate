package com.gio.common;

import java.util.TimerTask;

public class ProgressPoint extends TimerTask {
    @Override
    public void run() {
        System.out.print(".");
    }
}