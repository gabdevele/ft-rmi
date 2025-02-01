# FT-RMI: File Transfer via RMI in Java
made for *highseas*

![image](https://github.com/user-attachments/assets/27700473-a123-49a7-9017-ef673082689e)

This project implements a file transfer system using Java RMI (yeah I know that it isn't used anymore but it's cool). 
It allows users to upload, download, and list files on a remote server. The project includes both server and client components.

## Features

- **Upload Files**: Upload files to the server, either entirely or in chunks.
- **Download Files**: Download files from the server, either entirely or in chunks.
- **List Files**: List all available files on the server.
- **Generate Test Files**: Generate test files of specified size for testing purposes.

## Prerequisites

- Java 17 or higher

## Getting Started

### Clone the repo

```sh
git clone https://github.com/gabdevele/ft-rmi.git
cd ft-rmi
```
### Build the project
```sh
mvn clean install
```
### Running 
(use java -cp or your favuorite IDE)
- First run the server 
- Then run the client. 

## Acknowledgments

- [ProgressBar](https://github.com/ctongfei/progressbar) library for displaying progress bars during file

## Screenshots
![image](https://github.com/user-attachments/assets/0a968df6-bac2-45d3-a3c6-f0a9083f7364)
![image](https://github.com/user-attachments/assets/9c38a23a-e9f9-405f-a909-4c51f3a5b68a)
![image](https://github.com/user-attachments/assets/9e95facd-4fb5-4935-b7e5-2359bd4c6582)


