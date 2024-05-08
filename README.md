# AAWirelessClient
Inspired by the work done by Emil (@borconi, https://github.com/borconi/AAGateWay) and others. Thanks!

## How it works
In wireless client mode:
- Automatically starts when the selected gateway tries to connect via Bluetooth.
- Connects to the gateway Wifi hotspot and start Wireless Android Auto pointing it to the gateway.
- If everything works, the Android Auto should start on the car display wirelessly.

### Manually Connect to Android Auto Wireless
For wireless client:
- Enable "Use this device as wireless client" option.
- Enter the gateway Hotspot details in "Gateway Wifi SSID" and "Gateway Wifi password".
- Enable "Use Gateway Wifi for Internet" or enter "Gateway Wifi BSSID".
- Select the gateway device in "Gateway Bluetooth Device". Make sure the device is already paired.
- On the first connection there might be a notification or dialog for allowing Wifi connection. Make sure you allow that.
- Make sure your Bluetooth and Wifi are enabled in the device.
