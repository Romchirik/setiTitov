Second lab, simple file transmitter

Starting with:
```
gradle build
./gradlew run --args="args"
```
Example
```
gradle build
./gradlew run --args="-s -f=myfile.pdf -p="
```
Usage:
```
mp-transmitter [-hrs] [-a=<address>] [-f=FILE] [-p=<port>]
Simple my-protocol file transmitter
  -a, --address=<address>   server address (only for client)
  -f, --file=FILE           transmit this file to server
  -h, --help                print this help screen
  -p, --port=<port>         server port, if starting as server, overrides
                              default server port
  -r, --receive             receive files (start as server)
  -s, --send                send file to server (start as client, selected by
                              default)
```