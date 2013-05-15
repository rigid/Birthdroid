BIRTHDROID
==========

Simple, ad free & open android birthday app/widget.

Download APK at https://github.com/rigid/Birthdroid/blob/master/bin/Birthdroid.apk?raw=true


[![Flattr this git repo](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/thing/1345949)



1. Features
===========
* free & open
* no fancy permissions
* low battery usage
* doesn't phone home


2. Compile
==========
Use the provided Makefile wrapper. Available targets are

make keygen        - generate own keystore
make update        - update project
make all           - build release
make debug         - build debug
make sign          - sign release
make install       - push .apk to device using adb
make install_debug - push debugging .apk to device using adb

e.g. make update && make keygen && make && make sign && make install


3. Usage
========

3.1    Launch the app 
3.2    You'll see a list of contacts with upcoming birthdays 
3.3    Use the preferences menu to configure various settings
3.4    (optional) add a widget to your home-screen that continously 
       shows upcoming birthdays
