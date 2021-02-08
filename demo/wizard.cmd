rem @echo off
set JAR=F:\leksi\YandexDisk\Java8\SE\net.leksi.contest.assistant\dist\net.leksi.contest.assistant.jar
if not exist %JAR% set JAR=C:\Users\leksi\YandexDisk\Java8\SE\net.leksi.contest.assistant\dist\net.leksi.contest.assistant.jar
java -jar %JAR%  -in in -src src %*
