package com.example.hackathon;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;


public class CrepeAccessibilityService extends AccessibilityService {

    private static final String TAG = "crepeAccessibilityService: ";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.e(TAG, "An accessibility event going on");
        int eventType = accessibilityEvent.getEventType();
        Log.e(TAG, "Event type: " + String.valueOf(eventType));
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "Accessibility service interrupted");
    }
}
