# NFCUtils

NFCUtils is an in-development NFC utility app for Android. The goals are to be able to:
- Read from a NFC tag
- Write to a NFC tag
- Support Peer to Peer file sharing via NFC

## Usage: NFC tag read & write 

App usage is controlled via buttons on the home screen.

When the read button is pressed, the device should start scanning for NFC tags. Once a NFC tag is found, a separate page showing the tag content should be displayed.

When the write button is pressed, a page should be displayed in which the user will enter text to be written to a NFC tag, after which the device will start scanning. The text is then written to a found NFC tag. The device should not attempt to write to the tag if the tag has insufficient memory.

## Usage: Peer to Peer mode

Peer to Peer mode involves 2 devices: the receiver and the sender. A device can identify itself as a receiver or a sender from the home screen.

When the device is a sender, the device will ask for a file to send. The sender device then starts scanning for a receiver device. The file is transferred to the receiver device once connection between the two devices is established.
