# Dedup-Cloud-GUI-2
This project is for Course Code CSCI4999. 
The older version is in [link](https://github.com/ChanChiHo/Dedup-Cloud-GUI)

## Author
Author : Chan Chi Ho (The Chinese University of Hong Kong) - First Version

## Getting Started

The project contain two part : Client Program and Server Program. They have been put in two different folder, each file will have a makefile to make the program.

The textfile folder would store the test file for the testing purpose.

## Prerequisites

- The computer must support java Swing, SSL, abd SHA-256.
- The port 59090 should be open for both Client and Server side computer.
- The two computer client and server is better at the same network.

## Init: Key Generation

The project will need to first generate the private key and public key for both client and server program. The makefile will also finish the key generation and also java .class file creation.

Start by this command:
```sh
$ make
```

In Window (that have JDE):
```bash
make
```

## Server Program

After the init process, run the program by: 
```sh
$ java Server
```
Then you will see the following message in command line:
```sh
Server running..
Running at :
192.168.0.152		OR
192.168.0.152
```
The Ip address may be different, but please mark it down for the use of Client side.

For empty the file in the server, please use:
```sh
$ make clear
```
It will delete all the file store in folder data.

### Timeout for client

The server has a timeout setting in class Server.IndexFile, which you will see this in the class
```
public static final int TIMEOUT_IN_MIN = 1;
```
You can change the timeout though this line.

### Client Program Duplication

You can make another client program, which must create in the root folder though makefile.
```sh
$ make duplicate ver=<version>
```

In Window (that have JDE):
```bash
duplicate <version>
```

There are serveral things that need to know after duplicate the client program:
- Another key will generate for duplicated client program, this will also import the certificate to the server's truststore, therefore, the duplicated client program, can be only use to connect to that server program in the root folder.  

This will create a folder, which will include ssl key, the client class, client gui, which can be use directly.

## Server User management tools

The purpose of this tool is to read the information of server indexFile, while the server is still running.

There are serveral function:

### User List

```sh
$ java UserManager
```
This will generate a list, that include username, and the hash value(sha-256) of their password.

### Delete User
```sh
$ java UserManager delete <Username>
```
This will delete the user data from the server. However, their file data will not be deleted.

### Session List
```sh
$ java UserManager session
```
This will list the session id, the username that the id point with, and the expire time in LocalDateTime.

### Delete Session
```sh
$ java UserManager session clear
```
This will manually remove all session that store in the server.

## Client Program
 
Run the program by 
```sh
$ java GUI
```
You have to enter the IP Address to connect the server, any connection fault will require you to reconnect to the server by enter the IP Address.

Before you access the server, you must first have a accoount. Though the "create account" button, you can make a account to upload or download the file.




## Problem

This program is not very stable, if you find any thing wrong during the make process, Please follow these step.

1. Go to the client/server prorgam
2. use following command
```sh
$ make detail
```
It will show the warning and error during make process.

Known Bugs
- There would be crash when enter an unused IP Address
- The upload page would not be able to upload if you are doing second upload
- The back button in upload progress page will cause crash when you are still uploading file.

Possible situation that would appear bug
- Uploading huge file to the server
- Uploading or downloading file, but the server stop running
- The chunk or mydup.index is corrupted

## Fixing in version 2

- Progress Bar added. You can see the estimate time and progress of uploading and downloading file.
- Adding 'Connecting' Label when connecting to server.
- You can now see the filename of selected file and reset your selection in uploading page.
- Remove the BigInteger from code to enhance performance.
- Introducing of account system. The file that the user upload will not be viewed by other users.
~~- Muti Threading of server.~~
- Introduce timeout, client will not be able operate after certain time of doing nothing.
- SSL Connection is used. With the script to import the truststore and generate public/private key.
- Duplication of Client program and can be used together with origin one at the same time.
