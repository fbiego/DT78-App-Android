# ChangeLog

## 3.2
+ Added Charging status
+ Added DND when charging
+ Updated translations
+ Added animated heart rate measurement
+ Added 'Measure all' option

## 3.1
+ Added Italian language
+ Added Polish language

## 3.0
+ Added Czech language
+ Added App level DND mode
+ Implemented In-App Camera from [`here`](https://github.com/mmobin789/Android-Custom-Camera) (Experimental)
+ Bug fixes & improvements

The camera may not work on some devices and on others only the back camera may work. Rooted users can long press to switch between In-app or External camera.


## 2.9
+ Bug Fixes
+ Added Theme chooser
+ Improved Sleep Data algorithm
+ Added advanced error log

## 2.8
+ Added algorithm to parse Sleep Data
+ Added Spanish language
+ Added support for [`DIY ESP32 watch`](https://github.com/fbiego/ESP32_OLED_BLE)

## 2.7
+ Implemented sleep data (experimental)
+ Minor improvements

## 2.6
+ Bug fixes

## 2.5
+ Added German language
+ Added Greek language
+ Added Greek to English font converter
+ Added weekly & monthly view of steps data

## 2.4
+ Added Portuguese language
+ Added watch language settings
+ Added Notify on disconnect ring option (open app or stop service to stop ringer)
+ Added Customized watchface upload for DT66 smartwatch

## 2.3
+ Added Vietnamese language
+ Fixed hourly measurement data not being saved
+ Quiet hours feature will not send notifications during quiet hours
+ Service will not start automatically unless Bluetooth is on and the address has been set

## 2.2
+ Added Notification filter for individual apps
+ Added Indonesian language
+ Added Reset watch feature
+ Phone battery notification when plugged, unplugged & find phone
+ Self Test Notification to verify if Notification listener is working
+ Camera feature will start Camera app (root only)

## 2.1
+ Fixed static year bug, v2.0 and below will not set the correct time after year 2020
+ Fixed dependency of permissions, app should not crash if permission is not granted
+ Call and SMS notification can be turned on or off
+ Added raise to wake setting for DT92
+ *Smart notification v2* on DT78 only, space at the start of each line will be removed
+ Added large number font on Google weather and phone battery notifications

## 2.0
+ Removed manual entry of mac address, watch address will be selected from the list of paired devices
+ Removed *Run as service* option in preferences, the app will run as service by default, can still be stopped and started from the main screen
+ Increased visibility of the text icon for watch percentage
+ Auto detect watch type between DT78 & DT92 and firmware version
+ Added frequent contacts for DT92
+ *Smart notification* on DT78 only, title and message will be in separate lines if notification is short
+ Fixed bug causing `CursorIndexOutOfBoundsException` error
