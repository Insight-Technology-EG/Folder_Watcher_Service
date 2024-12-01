# Folder Watcher and Transfer Tool

## Overview
This Java program monitors a specified folder for new subfolders. When a new folder is detected, it moves the folder to a remote destination over a network. After transferring, it verifies the transfer and deletes the source folder if successful.

## Requirements
- **Java Runtime Environment (JRE)**: Java 11 or higher.
- **Network Connectivity**: Ethernet connection for transferring data.
- **Permissions**: Ensure read/write access to both the source and destination folders.

## Project Structure
```
project-folder/
│
├── src/
│   └── FolderWatcher.java  # Main application logic.
│
├── lib/  # (Optional) Additional libraries.
│
├── run_program.bat  # Batch script to run the program in detached mode.
│
└── README.md  # This readme file.
```

## Installation

### 1. Compile the Code
Navigate to the `src` directory and compile the code:
```bash
javac FolderWatcher.java
```

### 2. Package as JAR (Optional)
Package the compiled class into a JAR file for easier distribution:
```bash
jar cvf folder-watcher.jar FolderWatcher.class
```

## Configuration
- **Source Folder**: Modify the `sourceFolderPath` variable in the code to point to the folder you want to watch.
- **Destination Folder**: Modify the `destinationFolderPath` variable to specify where the folder should be transferred.

## How to Run

### 1. Run in Detached Mode Using Java Command
Use the `javaw` command to run the program without a console window:
```bash
javaw -jar folder-watcher.jar
```
