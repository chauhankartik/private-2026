# Operating Systems: Three Easy Pieces (OSTEP) — Master Guide & Visual Roadmap

A comprehensive, production-grade reference based on Remzi H. Arpaci-Dusseau and Andrea C. Arpaci-Dusseau's landmark textbook ***Operating Systems: Three Easy Pieces (OSTEP)***.

This guide organizes the 3 core abstractions of modern operating systems — **Virtualization**, **Concurrency**, and **Persistence** — with production C/POSIX system call code examples (`fork`, `exec`, `mmap`, `pthread`, `epoll`, `fsync`), assembly/hardware level mechanics, and **rich Mermaid diagrams**.

---

## 📂 Chapter Index & Roadmap

| Part | Module / Topic | Core Concepts | Location |
|---|---|---|---|
| **Cheatsheet** | Quick Reference | System Calls, Math Formulas, Inode Structures | [`00_ostep_cheatsheet.md`](./00_ostep_cheatsheet.md) |
| **Part I: Virtualization** | 01. CPU Processes & API | Process States, `fork`/`exec`/`wait`, LDE Protocol, Context Switching | [`01_virtualization/01_cpu_processes_and_api.md`](./01_virtualization/01_cpu_processes_and_api.md) |
| | 02. CPU Scheduling | FIFO, SJF, STCF, Round Robin, MLFQ, Lottery Scheduling | [`01_virtualization/02_cpu_scheduling.md`](./01_virtualization/02_cpu_scheduling.md) |
| | 03. Memory & Paging | Base/Bound, Segmentation, Multi-Level Page Tables, TLB Translation | [`01_virtualization/03_memory_virtualization_and_paging.md`](./01_virtualization/03_memory_virtualization_and_paging.md) |
| | 04. Page Replacement | Swap Space, Page Faults, LRU, Clock Algorithm, Thrashing | [`01_virtualization/04_page_replacement_policy.md`](./01_virtualization/04_page_replacement_policy.md) |
| **Part II: Concurrency** | 05. Threads & Locks | POSIX Threads, Test-And-Set, Compare-And-Swap, Linux Futex | [`02_concurrency/05_threads_and_locks.md`](./02_concurrency/05_threads_and_locks.md) |
| | 06. Condition Vars & Semaphores | Bounded Buffer, Producer-Consumer, Semaphores, Reader-Writer Locks | [`02_concurrency/06_condition_variables_and_semaphores.md`](./02_concurrency/06_condition_variables_and_semaphores.md) |
| | 07. Bugs & Event-Driven | Atomicity/Order Bugs, Deadlocks, Event Loop, `epoll`, Async I/O | [`02_concurrency/07_concurrency_bugs_and_event_driven.md`](./02_concurrency/07_concurrency_bugs_and_event_driven.md) |
| **Part III: Persistence** | 08. I/O, Disks & SSDs | Controllers, Interrupts vs Polling, DMA, SCAN, SSD FTL, Wear Leveling | [`03_persistence/08_io_devices_disks_and_ssds.md`](./03_persistence/08_io_devices_disks_and_ssds.md) |
| | 09. Files, Directories & VSFS | Inodes, Directory Layout, File System APIs (`open`, `read`, `write`, `fsync`) | [`03_persistence/09_files_directories_and_vsfs.md`](./03_persistence/09_files_directories_and_vsfs.md) |
| | 10. Crash Consistency & WAL | FSCK, Write-Ahead Logging (Journaling), LFS & Segment Cleaning | [`03_persistence/10_crash_consistency_and_journaling.md`](./03_persistence/10_crash_consistency_and_journaling.md) |

---

## 🎨 Visual Overview: Operating System Architecture

```mermaid
flowchart TD
    subgraph User Mode
        App["User Application / Process"] --> API["POSIX C Library (libc)"]
    end

    subgraph Kernel Mode (Trap Boundary)
        API -->|System Call Interrupt / Trap| SysCall["System Call Interface (sys_enter)"]
        
        SysCall --> Part1["1. Virtualization Subsystem"]
        SysCall --> Part2["2. Concurrency Subsystem"]
        SysCall --> Part3["3. Persistence Subsystem"]

        Part1 --> CPU_Sched["CPU Scheduler (MLFQ / CFS)"]
        Part1 --> Mem_Mgmt["Virtual Memory & MMU (Paging / TLB / Swap)"]

        Part2 --> Thread_Mgmt["Thread Manager & Mutex / Futex Engine"]

        Part3 --> VFS["Virtual File System (VFS)"]
        VFS --> FS["File System Driver (ext4 / XFS / VSFS)"]
        FS --> Buffer_Cache["Page Cache / Buffer Cache"]
        Buffer_Cache --> Disk_Sched["I/O Device Scheduler (SCAN / NVMe)"]
    end

    subgraph Hardware Layer
        CPU_Sched --> Hardware_CPU["Physical CPU Cores"]
        Mem_Mgmt --> Hardware_MMU["Hardware MMU & RAM"]
        Disk_Sched --> Hardware_Storage["HDD / NVMe SSD Controller (DMA)"]
    end
```
