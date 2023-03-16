package net.viacash.myvia;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.defaults.DefaultReactActivityDelegate;
import com.misnaplib.MainActivityResult;
import com.zeugmasolutions.localehelper.LocaleHelper;
import com.zeugmasolutions.localehelper.LocaleHelperApplicationDelegate;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class MainActivity extends ReactActivity implements MainActivityResult {

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "MiSnapLibExample";
  }

  @Override
protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(null);
}

  /**
   * Returns the instance of the {@link ReactActivityDelegate}. Here we use a util class {@link
   * DefaultReactActivityDelegate} which allows you to easily enable Fabric and Concurrent React
   * (aka React 18) with two boolean flags.
   */
  @Override
  protected ReactActivityDelegate createReactActivityDelegate() {
    return new DefaultReactActivityDelegate(
        this,
        getMainComponentName(),
        // If you opted-in for the New Architecture, we enable the Fabric Renderer.
        DefaultNewArchitectureEntryPoint.getFabricEnabled(), // fabricEnabled
        // If you opted-in for the New Architecture, we enable Concurrent React (i.e. React 18).
        DefaultNewArchitectureEntryPoint.getConcurrentReactEnabled() // concurrentRootEnabled
        );
  }

  Callable<Void> onActivityResult;
  Function0<Unit> onLocaleSet;

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    try {
      onActivityResult.call();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void registerForActivityResult(@NonNull Callable<Void> activityResult) {
    this.onActivityResult = activityResult;
  }

  @Override
  public void setupLocale(@NotNull String language, @NotNull Function0<Unit> applied) {
    Handler mainHandler = new Handler(getMainLooper());

    Runnable myRunnable = () -> {
      String locale = "en-US";
      if (language.equalsIgnoreCase("es")) {
        locale = "es-US";
      }

      LocaleListCompat newLocale = LocaleListCompat.forLanguageTags(locale);

      if (AppCompatDelegate.getApplicationLocales().toLanguageTags().equals(newLocale.toLanguageTags())) {
        System.out.println("MYSNAP Locale already applied");
        applied.invoke();
      } else {
        this.onLocaleSet = applied;
      }

      AppCompatDelegate.setApplicationLocales(newLocale);
    };

    mainHandler.post(myRunnable);
  }

  private LocaleHelperApplicationDelegate localeAppDelegate = new LocaleHelperApplicationDelegate();


  @Override
  public void attachBaseContext(Context base) {
    super.attachBaseContext(localeAppDelegate.attachBaseContext(base));
  }


  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    System.out.println("MYSNAP New Locale Applied: " + newConfig.locale.toString());

    localeAppDelegate.onConfigurationChanged(this);

    try {
      this.onLocaleSet.invoke();
      this.onLocaleSet = null;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public Context getApplicationContext() {
    return LocaleHelper.INSTANCE.onAttach(super.getApplicationContext());
  }

  private void setLocale(Locale locale) {
    Resources resources = getResources();
    Configuration configuration = resources.getConfiguration();
    DisplayMetrics displayMetrics = resources.getDisplayMetrics();
    configuration.setLocale(locale);
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
      getApplicationContext().createConfigurationContext(configuration);
    } else {
      resources.updateConfiguration(configuration,displayMetrics);
    }
  }

}
