package net.viacash.myvia;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import android.util.Log;

public class CalendarModule extends ReactContextBaseJavaModule {
  CalendarModule(ReactApplicationContext context) {
    super(context);
  }

  @Override
  public String getName() {
    return "CalendarModule";
  }

  @ReactMethod
  public void createCalendarEvent(String name, String location, Promise promise) {
    Log.d("CalendarModule", "Create event called with name: " + name
      + " and location: " + location);
    try {
      Integer eventId = 12;
      promise.resolve(eventId);
    } catch(Exception e) {
      promise.reject("Create Event Error", e);
    }
  }


}
