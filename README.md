# Android Network Audio Recorder Server
This project provides an interface to receive audio data from an Android device and then saves data to a PCM and WAV file.
## Requirements
- Apache mina (which has included)
## Usage
- text area: server ip
- start recording: establishs a TCP connection to the server and sends all audio data to the server.
- echo testing: (headset required!) tests your own voice. your audio data WILL also send to the server but WILL NOT save to a file in the server side.
- stop and exit: closes everything
 
