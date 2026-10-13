# Hijri Date Picker (Enhanced & Fixed Edition) 🌙

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-19%2B-blue.svg)](https://android-arsenal.com/api?level=19)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg)](license)

A modern, robust Hijri (Islamic Calendar / Umm Al-Qura) Date & Time Picker dialog for Android based on Material Design principles.

This repository is an updated, maintained, and bug-fixed edition of the Hijri Date Picker library, resolving critical calendar calculation crashes and compatibility issues present in previous versions.

---

## 🚀 Key Improvements & Bug Fixes Over Previous Versions

The original version suffered from fatal crashes during specific date transitions and compatibility issues with modern Android dependencies:

### 1. 🐛 Fix for Fatal Crash on the 30th Day (`DateTimeException`)
* **The Problem:** In the Umm Al-Qura lunar calendar, months dynamically alternate between 29 and 30 days. In previous versions, calendar manipulation (such as changing the year or month) directly modified the `YEAR` or `MONTH` fields while the calendar day was set to 30. If the target month only had 29 days, `UmmalquraCalendar` threw an unhandled `DateTimeException` (`Day of month is out of range`), causing the entire application to crash. This occurred in:
  * `MonthView.java`: When drawing the month matrix and computing accessibility item descriptions (`getItemDescription`).
  * `HijriDatePickerDialog.java`: In `initialize`, `onYearSelected`, `onDayOfMonthSelected`, and during configuration changes (`onSaveInstanceState` / `onCreate`).
* **The Solution:** Implemented a safe atomic update sequence:
  1. Temporarily reset `DAY_OF_MONTH` to `1`.
  2. Apply the requested `YEAR` and `MONTH`.
  3. Clamp the day safely using `Math.min(day, UmmalquraCalendar.lengthOfMonth(year, month))`.

### 2. 🛡️ Safe Boundary & Highlight Checks (`isOutOfRange` & `isHighlighted`)
* **The Problem:** Calling `isOutOfRange()` or `isHighlighted()` on the 30th day of a 29-day month caused internal exception crashes. In addition, `getEndDate()` was hardcoded to day 29 (`output.set(DAY_OF_MONTH, 29)`), corrupting the maximum selectable date range when the final month had 30 days.
* **The Solution:** `getEndDate()` now dynamically queries `UmmalquraCalendar.lengthOfMonth(...)` to retrieve the actual last day, and all date lookup methods safely validate boundaries without throwing exceptions.

### 3. 🎨 Modern Material Components Theme Support
* **The Problem:** Older versions crashed with `IllegalStateException` ("You need to use a Theme.AppCompat theme...") when integrated into modern applications using `Theme.MaterialComponents` or `Theme.Material3`.
* **The Solution:** Added `HijriDatePickerDialogTheme` based on `Theme.MaterialComponents.DayNight.Dialog` with `windowNoTitle=true`. Also added an overloaded `newInstance(Context context, ...)` method for flexible instantiation.

### 4. 📦 Build System & Dependency Modernization
* **The Problem:** The old library used deprecated Gradle build scripts, defunct Bintray plugins (`com.novoda.bintray-release`), and discontinued JCenter endpoints.
* **The Solution:** Cleaned all dead repository links, modernized Maven Central / JitPack resolution, and introduced comprehensive unit tests (`HijriDatePickerFixTest`) verifying month transitions and 30th-day edge cases across all 12 Hijri months.

---

## 📥 Installation

### Option 1: JitPack (Recommended)

**1. Add JitPack to your root `settings.gradle` or `build.gradle`:**

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

**2. Add the dependency to your app module `build.gradle`:**

```groovy
dependencies {
    implementation 'com.github.AbanoubAzer:HijriDatePicker:v1.0.0'
}
```

---

### Option 2: Include as a Local Module

1. Copy the `library` folder into your project's root directory.
2. In your `settings.gradle`, include:
   ```groovy
   include ':library'
   ```
3. In your app `build.gradle`, add:
   ```groovy
   dependencies {
       implementation project(':library')
   }
   ```

---

## 💻 Usage

### 1. Implement `OnDateSetListener`

```java
import net.alhazmy13.hijridatepicker.date.hijri.HijriDatePickerDialog;
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar;

public class MainActivity extends AppCompatActivity implements HijriDatePickerDialog.OnDateSetListener {

    @Override
    public void onDateSet(HijriDatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        // monthOfYear is 0-indexed (0 = Muharram, 11 = Dhu al-Hijjah)
        String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
        Toast.makeText(this, "Selected Hijri Date: " + date, Toast.LENGTH_SHORT).show();
    }
}
```

### 2. Show the Hijri Date Picker Dialog

```java
UmmalquraCalendar now = new UmmalquraCalendar();

HijriDatePickerDialog dpd = HijriDatePickerDialog.newInstance(
        this,
        now.get(UmmalquraCalendar.YEAR),
        now.get(UmmalquraCalendar.MONTH),
        now.get(UmmalquraCalendar.DAY_OF_MONTH)
);

// Optional configurations:
dpd.setMinYear(1400);
dpd.setMaxYear(1500);
dpd.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimary));
dpd.setVersion(HijriDatePickerDialog.Version.VERSION_2);

dpd.show(getSupportFragmentManager(), "HijriDatePickerDialog");
```

---

### Kotlin Example

```kotlin
import net.alhazmy13.hijridatepicker.date.hijri.HijriDatePickerDialog
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar

val now = UmmalquraCalendar()
val dpd = HijriDatePickerDialog.newInstance(
    { _, year, monthOfYear, dayOfMonth ->
        val formattedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
        // Handle date selection
    },
    now.get(UmmalquraCalendar.YEAR),
    now.get(UmmalquraCalendar.MONTH),
    now.get(UmmalquraCalendar.DAY_OF_MONTH)
).apply {
    setMinYear(1400)
    setMaxYear(1500)
    setVersion(HijriDatePickerDialog.Version.VERSION_2)
}

dpd.show(supportFragmentManager, "HijriDatePickerDialog")
```

---

## ⚛️ React Native Integration (Android Bridge)

You can easily bridge and use this native picker inside any React Native project:

### 1. Android Dependencies
In your React Native project:

**`android/build.gradle`:**
```groovy
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

**`android/app/build.gradle`:**
```groovy
dependencies {
    implementation 'com.github.AbanoubAzer:HijriDatePicker:v1.0.0'
}
```

---

### 2. Create the Native Module (`HijriDatePickerModule.java`)
Create `android/app/src/main/java/com/yourproject/HijriDatePickerModule.java`:

```java
package com.yourproject;

import androidx.fragment.app.FragmentActivity;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar;
import net.alhazmy13.hijridatepicker.date.hijri.HijriDatePickerDialog;

public class HijriDatePickerModule extends ReactContextBaseJavaModule {

    public HijriDatePickerModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "HijriDatePicker";
    }

    @ReactMethod
    public void open(ReadableMap options, Promise promise) {
        UiThreadUtil.runOnUiThread(() -> {
            try {
                FragmentActivity activity = (FragmentActivity) getCurrentActivity();
                if (activity == null) {
                    promise.reject("ACTIVITY_DOES_NOT_EXIST", "Activity doesn't exist");
                    return;
                }

                UmmalquraCalendar now = new UmmalquraCalendar();
                int year = options != null && options.hasKey("year") ? options.getInt("year") : now.get(UmmalquraCalendar.YEAR);
                int month = options != null && options.hasKey("month") ? options.getInt("month") : now.get(UmmalquraCalendar.MONTH);
                int day = options != null && options.hasKey("day") ? options.getInt("day") : now.get(UmmalquraCalendar.DAY_OF_MONTH);

                HijriDatePickerDialog dpd = HijriDatePickerDialog.newInstance(
                        (view, selectedYear, monthOfYear, dayOfMonth) -> {
                            WritableMap result = Arguments.createMap();
                            result.putString("action", "dateSetAction");
                            result.putInt("year", selectedYear);
                            result.putInt("month", monthOfYear + 1); // 1-indexed for JS
                            result.putInt("day", dayOfMonth);
                            promise.resolve(result);
                        },
                        year,
                        month,
                        day
                );

                if (options != null && options.hasKey("minYear")) dpd.setMinYear(options.getInt("minYear"));
                if (options != null && options.hasKey("maxYear")) dpd.setMaxYear(options.getInt("maxYear"));

                dpd.setOnCancelListener(dialog -> {
                    WritableMap result = Arguments.createMap();
                    result.putString("action", "dismissedAction");
                    promise.resolve(result);
                });

                dpd.show(activity.getSupportFragmentManager(), "HijriDatePickerDialog");
            } catch (Exception e) {
                promise.reject("E_PICKER_ERROR", e.getMessage(), e);
            }
        });
    }
}
```

---

### 3. Create the Package (`HijriDatePickerPackage.java`)
Create `android/app/src/main/java/com/yourproject/HijriDatePickerPackage.java`:

```java
package com.yourproject;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HijriDatePickerPackage implements ReactPackage {
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new HijriDatePickerModule(reactContext));
        return modules;
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }
}
```

Add `new HijriDatePickerPackage()` to the `getPackages()` list in your `MainApplication.java` (or `MainApplication.kt`).

---

### 4. JavaScript / TypeScript Usage

```javascript
import { NativeModules, Platform } from 'react-native';

const { HijriDatePicker } = NativeModules;

export const showHijriDatePicker = async (options = {}) => {
  if (Platform.OS !== 'android') {
    console.warn('HijriDatePicker is currently supported on Android.');
    return;
  }

  try {
    const result = await HijriDatePicker.open({
      year: 1448,     // optional default year
      month: 3,       // optional default month (0-11)
      day: 30,        // optional default day
      minYear: 1400,  // optional
      maxYear: 1500,  // optional
      ...options,
    });

    if (result.action === 'dateSetAction') {
      console.log('Selected Hijri Date:', `${result.day}/${result.month}/${result.year}`);
      return result; // { year: 1448, month: 4, day: 30 }
    } else {
      console.log('Picker was cancelled');
      return null;
    }
  } catch (error) {
    console.error('Error opening Hijri date picker:', error);
  }
};
```

---

## 🛠️ Testing

Comprehensive test cases have been added to verify that month and year transitions from 30-day months into 29-day months execute cleanly without `DateTimeException`.

To run the unit tests:
```bash
./gradlew :library:test
```

---

## 👤 Maintainer & Author

- **Abanoub Gerges Azer** ([@AbanoubAzer](https://github.com/AbanoubAzer))

---

## 📄 License

```
Copyright 2026 Abanoub Gerges Azer

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
