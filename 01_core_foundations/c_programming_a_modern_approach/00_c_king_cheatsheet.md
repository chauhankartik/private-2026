# C Programming: A Modern Approach — 30-Second Engineering Cheatsheet

This cheatsheet aggregates operator precedence, pointer arithmetic laws, struct padding math, common Undefined Behavior (UB) traps, and C89/C99/C11 standard comparisons.

---

## 1. C Operator Precedence & Associativity Matrix

```
Precedence Level    Operators                           Associativity   Description
------------------------------------------------------------------------------------------------------
1 (Highest)         ()  []  ->  .                       Left-to-Right   Function call, Array, Member
2                   !  ~  ++  --  +  -  *  &  (type) sizeof Right-to-Left Unary, Dereference, Address-of
3                   *  /  %                             Left-to-Right   Multiplicative
4                   +  -                                Left-to-Right   Additive
5                   <<  >>                              Left-to-Right   Bitwise Shift
6                   <  <=  >  >=                        Left-to-Right   Relational
7                   ==  !=                              Left-to-Right   Equality
8                   &                                   Left-to-Right   Bitwise AND
9                   ^                                   Left-to-Right   Bitwise XOR
10                  |                                   Left-to-Right   Bitwise OR
11                  &&                                  Left-to-Right   Logical AND (Short-Circuit)
12                  ||                                  Left-to-Right   Logical OR (Short-Circuit)
13                  ?:                                  Right-to-Left   Ternary Conditional
14 (Lowest)         =  +=  -=  *=  /=  %=  &=  ^=  |=   Right-to-Left   Assignment
```

---

## 2. Pointer Arithmetic Laws

For a pointer `T *p` pointing to element `p[0]` of type `T`:
* **Increment**: `p + i` adds `i * sizeof(T)` bytes to the memory address.
* **Difference**: If `p` and `q` point to elements of the same array, `q - p` returns element distance of type `ptrdiff_t`.
* **Void Pointers**: `void *` represents generic raw memory; **pointer arithmetic on `void *` is illegal in standard C** (must cast to `char *` or `uint8_t *`).

---

## 3. Structure Padding & Alignment Math

Compilers align structure members on addresses divisible by their natural alignment size (e.g., 4-byte `int` on 4-byte boundary, 8-byte `double` on 8-byte boundary):

```c
struct Padded {
    char a;      // 1 byte  + 3 bytes padding
    int b;       // 4 bytes
    char c;      // 1 byte  + 3 bytes padding
};               // Total = 12 bytes!

struct Optimized {
    int b;       // 4 bytes
    char a;      // 1 byte
    char c;      // 1 byte  + 2 bytes padding
};               // Total = 8 bytes!
```

---

## 4. Undefined Behavior (UB) Catalog

| Category | Illegal Action | Consequence |
| :--- | :--- | :--- |
| **Dangling Pointer** | Accessing freed memory (`free(p); *p = 5;`) | Memory corruption / Segfault |
| **Buffer Overflow** | Writing past array boundary (`a[10] = 0` for 10-elem array) | Stack smashing / Exploit |
| **Signed Overflow** | `INT_MAX + 1` | Undefined compiler optimizations |
| **Strict Aliasing** | Dereferencing float via int pointer (`*(int*)&f`) | Wrong value due to reordering |
| **Sequence Violation** | `i = i++ + 1;` | Unpredictable register state |

---

## 5. C Standards Comparison (C89 vs C99 vs C11)

```
Feature                         C89 / C90       C99             C11
------------------------------------------------------------------------------------------------------
Variable-Length Arrays (VLAs)   No              Yes (Mandatory) Yes (Optional)
Designated Initializers        No              Yes             Yes
Inline Functions                No              Yes             Yes
Single-Line Comments (//)       No              Yes             Yes
Type-Generic Macros (_Generic)  No              No              Yes
Atomic Operations (_Atomic)     No              No              Yes (<stdatomic.h>)
Alignof / Alignas               No              No              Yes (<stdalign.h>)
```
