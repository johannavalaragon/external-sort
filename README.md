# External Sorting System

A high-performance Java application designed for out-of-core processing of datasets that exceed physical RAM limits. This project implements a robust external sort to handle I/O-bound constraints, mirroring the core patterns used in large-scale batch record processing.

## Technical Highlights
* **Algorithmic Pipeline:** Generated sorted runs utilizing **Heapsort** before reconstructing the final records via a **Multiway Merge**.
* **Memory Management:** Designed to strictly operate within predefined memory limits, ensuring stability and performance regardless of the input file size.
* **Disk I/O Optimization:** Minimized expensive disk read/write operations by intelligently chunking data and managing buffer limits.

## Tech Stack
* **Language:** Java
* **Concepts:** External Sorting, Out-of-Core Processing, Disk I/O, Buffer Management
* **Algorithms:** Heapsort, Multiway Merge

## Setup and Installation
To run this project locally, ensure you have the Java Development Kit (JDK) installed.

1. Clone the repository and navigate into the project directory:
   ```bash
   git clone https://github.com/johannavalaragon/external-sort.git
   cd external-sort
2. Compile the Java source files:
   ```bash
   javac src/*.java
   ```
## Usage
**Execution Command:**
   ```bash
   java -cp src ExternalSortProj [data-file.bin] [number-of-buffers]
   ```

**Example Execution:**
   ```bash
   java -cp src ExternalSortProj testa12.bin 16
   ```
