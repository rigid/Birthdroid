# BIRTHDROID

Simple, ad free & open android birthday app/widget.

## 1. Install
Get from [f-droid](https://f-droid.org) or download latest APK at https://github.com/rigid/Birthdroid/releases/latest


## 2. Features

* free & open
* no fancy permissions
* low battery usage
* doesn't phone home


## 3. Compile

Use the provided Makefile wrapper. Available targets are

* make keygen        - generate own keystore
* make update        - update project
* make all           - build release
* make debug         - build debug
* make sign          - sign release
* make install       - push .apk to device using adb
* make install_debug - push debugging .apk to device using adb

e.g. make update && make keygen && make && make sign && make install

You need the appcompat library to compile Birthdroid. You might have to update that path for the update target in the Makefile.


## 4. Usage

1. Launch the app 
2. You'll see a list of contacts with upcoming birthdays 
3. Use the preferences menu to configure various settings
4. (optional) add a widget to your home-screen that continously shows upcoming birthdays


## 5. FAQ

* Q: How can I force-refresh the birthday list or the widget content?
  * A: Data will be refreshed regularly every 24 hours and after you leave the preferences. To force a refresh, open and close the preferences dialog from the app menu.


<img src="https://upload.wikimedia.org/wikipedia/commons/5/50/Bitcoin.png" width="25"> Donate a coffee: **1BLEeqwXnJJbpaLHiskZ5WyJJRyEWqCAQZ**
