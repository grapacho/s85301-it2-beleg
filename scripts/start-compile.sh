#!/bin/bash

# es wird die Verzeichnisstruktur von IntelliJ IDEA angenommen
# für Eclipse sind die Verzeichnisse entsprechend anzupassen

port=8554
video=htw.mjpeg
src=src
bin=out/production/RTSP-Streaming
cp=".:src:lib/webcam-capture-0.3.12.jar"
#cp=".:src:lib/webcam-capture-0.3.12.jar:lib/bridj-0.7.0.jar:lib/slf4j-api-1.7.2.jar"

# Kompilierung
echo "compile classes..."
javac -cp $cp ${src}/Server.java  -d $bin 
javac -cp $src ${src}/Client.java  -d $bin 

# Start
#echo "start classes..."
#java -cp $bin  Server $port &
#sleep 1s
#java -cp $bin  Client localhost $port $video &
