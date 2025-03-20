# GitFP - Git-like FTP Synchronization Tool

[![Java Version](https://img.shields.io/badge/java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

GitFP is a powerful file synchronization tool that combines Git-like versioning with FTP file transfer capabilities. It enables automatic file synchronization between local and remote directories while maintaining a version history of all changes.

## Features

- **Real-time Local File Monitoring**: Automatically detects file changes in the monitored directory
- **Secure File Transfer**: Supports encrypted file transfers for enhanced security
- **Version History**: Maintains historical versions of files with timestamps
- **Interactive Menu**: User-friendly command-line interface for file operations
- **Connection Pooling**: Efficient FTP connection management
- **Cross-Platform**: Works on Windows, macOS, and Linux

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven for dependency management
- FTP server access

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/gitfp.git
   cd gitfp
   ```

2. Build with Maven:
   ```bash
   mvn clean package
   ```

3. Create a `config.properties` file in the project root:
   ```properties
   ftp.server=your-ftp-server.com
   ftp.port=21
   ftp.user=username
   ftp.password=password
   encryption.enabled=true
   ```

4. Run the application:
   ```bash
   java -jar target/gitfp-1.jar
   ```

## Usage

The application provides an interactive menu with the following options:

```
==== GitFP Menu ====
1. Listar archivos remotos
2. Descargar archivo
3. Descargar versión
4. Borrar archivo remoto
5. Salir
```

### File Synchronization

Files placed in the `syncro` directory are automatically uploaded to the FTP server. When changes are detected, GitFP creates versioned backups with timestamps.

### Downloading Files

Select option 2 to download the latest version of a file, or option 3 to view and download from available historical versions.

### Managing Remote Files

Option 1 lists all remote files, while option 4 allows you to delete files from the remote server.

## Project Structure

```
├── src/                  # Source code
│   ├── App.java          # Main application entry point
│   └── lib/              # Core libraries
│       ├── Connection/   # FTP connection management
│       ├── Enum/         # Type definitions
│       ├── Factory/      # Object factories
│       ├── Handlers/     # File transfer handlers
│       ├── Interfaces/   # Core interfaces
│       ├── Security/     # Encryption utilities
│       └── Utils/        # Utility classes
├── syncro/               # Local directory for file synchronization
├── downloads/            # Directory for downloaded files
└── config.properties     # Configuration file
```

## Architecture

GitFP follows a modular architecture:

- **Jefazo**: Orchestrates the system and manages event handling
- **Supervisor**: Monitors the local directory for file changes
- **Obrero**: Processes file events and triggers appropriate actions
- **Comunicador**: Handles communication between components
- **FileOperationService**: Manages file operations with the remote server

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Apache Commons Net for FTP functionality
- The Java community for valuable resources and support

---

