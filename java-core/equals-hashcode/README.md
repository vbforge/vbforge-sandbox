# equals-hashcode
Experiments with Java equals() & hashCode(), how Java compares objects.

## Setup

- Java 21
- Maven

---

## A correction about `hashCode()`

A common beginner misconception:

> "`hashCode()` returns the object's memory address."

That is **not** true in Java.

A correct beginner definition:

> `hashCode()` returns an integer hash value used by hash-based collections
> such as `HashMap` and `HashSet`. The default `Object.hashCode()` is
> identity-based, but Java does **not** guarantee that it equals the object's
> memory address. The important part is the **contract**: equal objects must
> produce the same hash code.

---

## The two rules you must know

### Rule 1 — `==` vs `equals()`
- `==` compares **references** (are they literally the same object?).
- `equals()` compares **object equivalence** (do they mean the same thing?).
- The default `Object.equals()` behaves like `==`.

### Rule 2 — `equals()` and `hashCode()` travel together
- If you override `equals()`, you **must** override `hashCode()`.
- If `a.equals(b)` is `true`, then `a.hashCode() == b.hashCode()` **must** be `true`.
- The reverse is not required — collisions are allowed.

---

## The 4 stages

### Stage 1 — `com.vbforge.stage1`
**`StringComparison.java`**

Shows the difference between `==` and `equals()` on `String`.

Key facts:
- String literals are interned (reused).
- `new String(...)` always creates a new object.
- `==` compares references; `equals()` compares content.

---

### Stage 2 — `com.vbforge.stage2`
**`Book.java`** — a plain class with no `equals()` / `hashCode()`.

Demos:
- **`ReferenceDemo`** — two variables pointing to the same object (alias).
  Changing one changes the other.
- **`DefaultEqualityDemo`** — two different objects with identical fields
  are **not** equal by default.
- **`DefaultHashCodeDemo`** — two different objects usually have
  different hash codes; an alias has the same hash code.

This stage is the "before" picture.

---

### Stage 3 — `com.vbforge.stage3`
**`Book.java`** — the correct, modern override using
`Objects.equals()` and `Objects.hash()`.

**`ManualBook.java`** — the same idea written by hand, so you can
see exactly what those utility methods do (null check, identity
check, type check, field comparison, 31-based hash).

**`EqualityDemo`** — compares two `Book`s with the same data.
Now `equals()` returns `true` and `hashCode()` matches.

**`ManualEqualityDemo`** — checks the contract explicitly:
reflexivity, null-safety, symmetry.

---

### Stage 4 — `com.vbforge.stage4`
**`StringHashDemo`** — confirms that `"test"` and `new String("test")`
are different references but have the same content, same `equals()`,
and same `hashCode()`.

**`HashSetDemo`** — the real-world payoff:
`HashSet<Book>` keeps only **one** of two equal books.
Remove `equals()`/`hashCode()` and the `HashSet` would keep both.

---

## The `equals()` contract (memorize these)

A correct `equals()` must be:

1. **Reflexive** — `x.equals(x)` is `true`.
2. **Symmetric** — `x.equals(y)` is `true` iff `y.equals(x)` is `true`.
3. **Transitive** — if `x.equals(y)` and `y.equals(z)`, then `x.equals(z)`.
4. **Consistent** — repeated calls return the same answer as long as
   the compared fields do not change.
5. **Null-safe** — `x.equals(null)` returns `false`.

## The `hashCode()` contract

1. Consistent during a single execution.
2. **Equal objects must have equal hash codes.**
3. Unequal objects may share a hash code (a collision — perfectly legal).

---

## Run the tests

The project includes a JUnit 5 test suite that verifies the five properties of
`equals()` and the `hashCode()` contract.

### What is tested

| # | Property        | Test class (nested group)                |
|---|-----------------|-------------------------------------------|
| 1 | Reflexivity     | `BookEqualityTest.Reflexivity`            |
| 2 | Symmetry        | `BookEqualityTest.Symmetry`               |
| 3 | Transitivity    | `BookEqualityTest.Transitivity`           |
| 4 | Consistency     | `BookEqualityTest.Consistency`            |
| 5 | Null-safety     | `BookEqualityTest.NullSafety`             |
| – | Field correctness | `BookEqualityTest.FieldCorrectness`     |
| – | `hashCode()` contract | `BookEqualityTest.HashCodeContract` |
| – | Collection impact    | `BookEqualityTest.HashSetIntegration` |
| – | Manual implementation | `ManualBookEqualityTest`              |

### The one surprising test — collision

`BookEqualityTest.HashCodeContract.unequalObjectsCanCollide` proves:

```
a.equals(b) == false     <-- fields differ
a.hashCode() == b.hashCode()   <-- perfectly legal
```

This is the "one-way" rule of `hashCode()`:

- **Required:** `a.equals(b) == true` → `a.hashCode() == b.hashCode()`
- **Not required:** same hash → same object

The example uses the classic Java quirk `"Aa".hashCode() == "BB".hashCode() == 2112`.

### Run

```bash
mvn test
```

### Suggested experiments

1. **Delete** `hashCode()` from `stage3.Book` and re-run — every
   `BookEqualityTest` still passes, **except** `HashSetIntegration`
   (the set now contains duplicates). This is the classic "overrode `equals()`
   but forgot `hashCode()`" bug.
2. **Change** `Book.equals()` to use `instanceof` instead of
   `getClass() != o.getClass()` and re-run — all tests still pass, but the
   class becomes vulnerable to the symmetry-violation trap with subclasses.
3. **Mutate** a field inside `Consistency.repeatedCallsAreStable` between two
   `equals()` calls — the loop still passes, because consistency only applies
   while the fields are unchanged. Notice how the test *would* break if you
   moved the mutation inside the loop.

---

## Summary in one sentence

> `==` asks *"is it the same object?"*; `equals()` asks *"does it mean the same
> thing?"* — and any time you customize `equals()`, you must customize
> `hashCode()` alongside it.

---

