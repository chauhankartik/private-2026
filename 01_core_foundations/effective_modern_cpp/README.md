# Effective Modern C++ (Scott Meyers)

## Type Deduction & `auto`
- `auto` uses template type deduction rules.
- `decltype` yields the exact type (often used for trailing return types).
- Use `auto` to avoid uninitialized variables, verbose type names, and implicit conversions.

## Smart Pointers
Manual memory management with `new`/`delete` is dangerous. Smart pointers provide automated lifecycle management via RAII.
- **`std::unique_ptr`**: Exclusive ownership. Zero overhead over raw pointers. Only movable, not copyable.
- **`std::shared_ptr`**: Shared ownership via reference counting (control block). Slower due to atomic reference count increments.
- **`std::weak_ptr`**: Non-owning observer of `std::shared_ptr` to break cyclic references.

```cpp
// Proper creation
auto ptr = std::make_unique<Widget>(); // Exception safe, optimal allocation
auto shared = std::make_shared<Widget>(); // Allocates object + control block together
```

## Move Semantics & Perfect Forwarding
- **Universal References (Forwarding References)**: `T&&` in a deduced context can bind to both lvalues and rvalues.
- **`std::forward`**: Conditionally casts a forwarding reference to an rvalue if it was initialized with an rvalue.

```cpp
template<typename T>
void wrapper(T&& arg) {
    // Preserve lvalue/rvalue category of arg
    process(std::forward<T>(arg)); 
}
```

## Lambdas
Anonymous inline functions with variable capture semantics.
- `[=]` capture by value.
- `[&]` capture by reference.
- Init capture `[x = std::move(ptr)]` (C++14).

```cpp
auto closure = [x = 10](int y) { return x + y; };
```
