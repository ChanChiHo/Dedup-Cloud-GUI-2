# Dedup-Cloud-GUI-2
This project is for Course Code CSCI4999. 
The older version is in [link](https://github.com/ChanChiHo/Dedup-Cloud-GUI)

## Author
Author : Chan Chi Ho (The Chinese University of Hong Kong) - First Version

## Getting Started

The project contain two part : Client Program and Server Program. They have been put in two different folder, each file will have a makefile to make the program.

The textfile folder would store the test file for the testing purpose.

## Prerequisites

- The computer must support java Swing and also SHA-256.
- The port 59090 should be open for both Client and Server side computer.
- The two computer client and server is better at the same network.

## Server Program

Start compile the program by
```sh
$ make
```
Run the program by 
```sh
$ java Server
```
Then you will see the following message in command line:
```sh
The data server is still running...
Running at : 192.168.0.155
```
The Ip address may be different, but please mark it down for the use of Client side.

For empty the file in the server, please use:
```sh
$ make clear
```
It will delete all the file store in folder data.

## Client Program
 
Start compile the program by
```sh
$ make
```
then run the program by 
```sh
$ java GUI
```
You have to enter the IP Address to connect the server, any connection fault will require you to reconnect to the server by enter the IP Address.

## Problem

This program is not very stable, if you find any thing wrong during the make process, use following command
```sh
$ make detail
```
It will show the warning and error during make process.

Known Bugs
- There would be crash when enter an unused IP Address
- There would be delay when the connection of server broken
- The upload page would not be able to upload if you are doing second upload

Possible situation that would appear bug
- Uploading huge file to the server
- Uploading or downloading file, but the server stop running
- The chunk or mydup.index is corrupted

## Fixing in version 2

- Progress Bar added. You can see the estimate time and progress of uploading and downloading file.
- Adding 'Connecting' Label when connecting to server.
- You can now see the filename of selected file and reset your selection in uploading page.
