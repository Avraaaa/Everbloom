# EverBloom — Design Decisions

A record of the significant design decisions behind EverBloom: the problem that
forced each one, the alternatives that were weighed, and the change each one is
meant to make cheap later.

Each entry follows the same shape:

- **Context** — the problem in the application that forced a decision.
- **Decision** — what we did.
- **Alternatives considered** — what else was on the table, and why it lost.
- **Consequences** — what this costs us today.
- **What it makes cheap later** — the change this design absorbs without a rewrite.

Diagrams referenced here live in [`docs/diagrams/`](diagrams/); the schema is documented in
[`domain-database-design.md`](domain-database-design.md).

---

## Part A — Architecture

### A1. Three layers: presentation, service, repository

**Context.** The application has seven screens and nine services. Business rules
(stock checks, discount arithmetic, order-status transitions, phone-number
uniqueness) have to run somewhere. If they run inside JavaFX controllers, they can
only be exercised by clicking through a window, and they get duplicated on every
screen that needs them.

**Decision.** Three layers with a strictly one-way dependency:
`controller → service → repository → DatabaseConnection → SQLite`. Controllers
hold no SQL and no rules; services hold no `ResultSet` and no JavaFX type;
repositories hold no rules. The domain model is shared by all three.

**Alternatives considered.**

- *Controllers calling repositories directly.* Fewer classes, but the rules end up
  in the UI, and validation drifts between screens.
- *A single `EverbloomService` facade.* One class would have grown to every rule in
  the system; the per-aggregate split keeps each service small enough to read.

**Consequences.** More classes, and a small amount of pass-through delegation
(`DashboardService` mostly forwards to `DashboardRepository`). We accepted that:
the uniform shape is worth more than the saved lines.

**What it makes cheap later.** The 18 JUnit classes construct services directly and
run headless — no JavaFX toolkit is started anywhere in the test suite. A second
front end (a web view, a CLI reporting tool) reuses the entire service layer
unchanged.

---

### A2. Hand-written JDBC repositories, one per aggregate — no ORM

**Context.** Persisting an order is not a single-row insert. It writes the order,
its arrangement tree, the flower lines and the extra lines, and it must be
all-or-nothing.

**Decision.** One repository class per aggregate root, using plain JDBC with
`PreparedStatement`. Multi-table writes open one `Connection`, turn off
auto-commit, and commit or roll back as a unit (see `OrderRepository.create` and
`createEventPackage`).

**Alternatives considered.**

- *Hibernate/JPA.* It would hide exactly the thing this project is meant to
  demonstrate — schema design, foreign keys, transactions — behind annotations, and
  it adds a heavy dependency for ten tables.
- *One generic `Repository<T>` base class.* The eight repositories differ more than
  they overlap (`ReportRepository` runs aggregate queries, `OrderRepository` writes
  a tree). A shared base would have been an abstraction over three similar CRUD
  cases and five exceptions.

**Consequences.** The SQL is verbose and there is repeated
`readX(ResultSet)` mapping code. That repetition is deliberate: it is local and
obvious, and each repository stays independently readable.

**What it makes cheap later.** All SQL is confined to one package. Moving to
another JDBC database means editing the SQL strings and `DatabaseConnection`, and
nothing above the repository layer changes.

---

### A3. The application creates and seeds its own schema on start-up

**Context.** The project has to run from a clean clone, on a marker's machine,
with no setup step. It also has to survive being closed and reopened with data
intact.

**Decision.** `DatabaseInitializer.initialize()` runs on every start-up. It
executes `CREATE TABLE IF NOT EXISTS` for all ten tables and then seeds reference
data (flowers, extras, customers, templates) only where it is absent. The whole
initialisation runs in one transaction and rolls back on failure.

**Alternatives considered.**

- *Ship a pre-built `.db` file.* The schema would then be invisible in the source
  tree and would drift from the code silently.
- *A separate migration tool (Flyway/Liquibase).* Real value on a long-lived
  product, but another dependency and another thing to explain for a schema that
  is created once.

**Consequences.** There is no versioned migration path — a change to an existing
column requires deleting the local database file. Acceptable while the schema is
still settling.

**What it makes cheap later.** `DatabaseInitializer` is the single place a new
table or seed row is added, and every test gets a real, fully-seeded schema in a
`@TempDir` in one line.

---

### A4. Money is stored as an integer count of minor units

**Context.** Prices, subtotals, discounts and line totals all have to add up
exactly, and the schema enforces that arithmetic with `CHECK` constraints
(`total_snapshot = subtotal_snapshot - discount_snapshot`).

**Decision.** Every monetary value is a `long` holding poisha (1/100 BDT).
Formatting for display happens in exactly one place, `MoneyFormatter`, which scales
by 2 with `BigDecimal`. No `double` or `float` appears anywhere in the money path.

**Alternatives considered.**

- *`double`.* Rejected outright: binary floating point cannot represent 0.10
  exactly, and the `CHECK` constraints would fail on rounding drift.
- *`BigDecimal` in the model.* Correct, but heavier to compare and sum, and SQLite
  has no decimal type — it would be stored as text or real anyway.

**Consequences.** Every price literal in seed data and tests is in poisha (a rose
is `3000`), which reads oddly until you know the convention.

**What it makes cheap later.** Adding tax, a service charge, or a second currency
is arithmetic on integers plus a change in one formatter, with no risk of
accumulated rounding error.

---

### A5. Orders store snapshots, not live references

**Context.** A flower's price changes. An extra is deactivated. A customer edits
their address. None of that may retroactively change what an order from last month
says it cost.

**Decision.** Every column on `orders`, `arrangements`, `arrangement_flowers` and
`arrangement_extras` that affects the printed order is a `*_snapshot` copy taken at
the moment the order was placed — the occasion, the delivery address, the pricing
policy name, the flower name, the unit price, the line total. Foreign keys to
`flowers`, `extras` and `customers` are kept for reporting, but they are
`ON DELETE RESTRICT`, and the money is read from the snapshot.

**Alternatives considered.**

- *Join to the current `flowers` row at read time.* Simpler schema, but order
  history silently rewrites itself whenever the catalogue changes — the classic
  invoice bug.
- *A price-history table with valid-from dates.* More normalised and more
  powerful, but it turns every order read into a temporal join for a benefit this
  application does not need.

**Consequences.** Deliberate denormalisation: the flower name is stored twice.
`CHECK` constraints (`line_total_snapshot = unit_price_snapshot * quantity`) keep
the copies internally consistent.

**What it makes cheap later.** Reprinting a six-month-old order, or auditing
revenue against what was actually charged, is a single-table read. `ReportRepository`
already relies on this.

---

## Part B — Design patterns

Each pattern below was introduced because a specific problem in the application
demanded it. Where a pattern was considered and rejected, that is recorded in
Part C.

### B1. Builder — assembling a bouquet

*Diagram: `docs/diagrams/builder.svg`*

**Context.** A bouquet has five optional parts (customer, occasion, wrapping,
message, flower list) and it is only valid as a whole. Stock cannot be checked
until the *total* quantity of each flower is known, because the user may add the
same flower twice in separate clicks.

**Decision.** `BouquetBuilder` accumulates state through chained `forX`/`withX`/
`addFlower` calls, merges repeated flower selections into one line, and validates
against stock on every merge. `build()` is the single gate that produces an
immutable `Bouquet`.

**Alternatives considered.**

- *A constructor with six parameters.* Cannot represent a half-built bouquet, and
  the UI needs exactly that while the user is still choosing.
- *A mutable `Bouquet` with setters.* The product would then be able to exist in an
  invalid state, and every consumer would have to defend against it.

**Consequences.** Two classes where one might do, and validation logic split
between the builder (per-step) and `build()` (whole-object).

**What it makes cheap later.** A new optional part is one more `withX()` method.
No existing call site changes, because every other caller simply never calls it.
`BouquetTemplate.copyToBuilder()` already reuses the same validation gate to load a
saved template.

---

### B2. Decorator — priced add-ons

*Diagram: `docs/diagrams/decorator.svg`*

**Context.** Extras (a card, a vase, chocolates, ribbon) each change a bouquet's
price *and* its printed description, and customers pick any combination.

**Decision.** `BouquetItem` declares `getDescription()` and `getSubtotal()`.
`BaseBouquet` wraps the flowers; each `ExtraDecorator` wraps whatever came before
and adds its own price and text. Decorators nest, so *n* extras cost *n* wrappers.

**Alternatives considered.**

- *A subclass per combination.* 2<sup>n</sup> classes for *n* extras.
- *A `List<Extra>` field inside `Bouquet`.* Workable, but it puts the pricing rule
  inside the product and forces every consumer of the total to re-implement the sum.

**Consequences.** A stack of small wrapper objects, and a debugger view of a
decorated bouquet is several levels deep.

**What it makes cheap later.** Every consumer of the total — `PricingService`,
`BouquetArrangement` — sees only `getSubtotal()`. An add-on that is *not* a flat fee
(a percentage gift-wrap surcharge, a per-stem ribbon) is a new decorator class and
nothing else changes.

---

### B3. Composite — event packages

*Diagram: `docs/diagrams/composite.svg`*

**Context.** A wedding order is a tree: named groups (Ceremony, Reception) holding
other groups and individual arrangements. It has no fixed depth.

**Decision.** `EventPackageComponent` is implemented by both `ArrangementGroup`
(composite) and `BouquetArrangement` (leaf). `getTotalPrice()` and `getSummary()`
recurse; the caller never learns the depth.

Persistence mirrors this: `arrangements` carries a nullable
`parent_arrangement_id` self-foreign-key (an adjacency list), and
`OrderRepository.findEventPackage` rebuilds the tree by the same recursion.

**Alternatives considered.**

- *Instance-of checks while walking.* Every price and summary routine would repeat
  the recursion, and all of them break the day a group nests one level deeper.
- *A fixed two-level model (package → arrangements).* Simpler, but it hard-codes a
  depth the domain does not actually have.
- *Nested sets / materialised path in the database.* Faster subtree reads, far more
  expensive writes, and unnecessary at the scale of one order.

**Consequences.** Reading a package is *n* + 1 queries in the worst case, and
`ArrangementGroup` exposes both `add()` and the leaf operations, so a leaf could in
principle be asked for children it does not have.

**What it makes cheap later.** `OrderService` prices a whole package with one call
on the root. A new node type — a rental item, a labour line — only has to implement
`EventPackageComponent`.

---

### B4. Strategy — pricing policies

*Diagram: `docs/diagrams/strategy.svg`*

**Context.** Standard and loyalty pricing differ only in the arithmetic applied to
a subtotal, but both need the same validation and the same discount reporting.

**Decision.** `PricingStrategy` declares `getName()` and `calculateTotal(subtotal)`.
`PricingService` holds no pricing rule at all — it receives a strategy as a *method
parameter* and applies it to the subtotal the decorator chain reports. The policy
is chosen in the bouquet builder screen.

**Alternatives considered.**

- *A `switch` on a policy string inside `PricingService`.* That class would have to
  be edited, retested and re-reviewed for every new policy.
- *A field on `PricingService` holding the current strategy.* Would make the
  service stateful, and it is shared across screens.

**Consequences.** The caller must pass a strategy on every call, which makes the
signatures slightly wider.

**What it makes cheap later.** A seasonal or bulk-discount policy is a new class
implementing two methods, plus one entry in the policy combo box.
`PricingServiceTest` keeps passing untouched, because the existing strategies are
not edited.

---

### B5. State — the order fulfilment lifecycle

*Diagram: `docs/diagrams/state.svg`*

**Context.** An order moves ORDERED → PREPARING → ARRANGING → READY → DELIVERED.
The legal transitions are a business rule, not a display string, and a delivered
order must not move anywhere.

**Decision.** `OrderState` declares `getStatus()` and `advance(order)`. Each of the
five concrete states knows only its own successor; `DeliveredState.advance()`
throws. `Order.advanceStatus()` delegates to its current state, so the transition
table *is* the set of state classes.

**Alternatives considered.**

- *A `String status` plus a `switch`.* The rule then leaks into every screen that
  advances an order, and an illegal jump is caught only where someone remembered to
  write the check.
- *A Java `enum` with an `advance()` method.* Genuinely close, and lighter. We chose
  classes because a state is likely to gain behaviour beyond the transition (which
  actions a screen may offer, what notification text to raise), and enum constants
  with growing bodies become awkward.

**Consequences.** Five small classes, and a state object is created on every
`setStatus` call when an order is read from the database.

**What it makes cheap later.** Inserting a QUALITY_CHECK stage means adding one
class and repointing one `advance()`. `Order`, `OrderService` and every controller
are untouched.

---

### B6. Observer — order status fan-out

*Diagram: `docs/diagrams/observer.svg`*

**Context.** Advancing an order has two consequences that have nothing to do with
each other: a notification row is written, and the orders table redraws.

**Decision.** `OrderService` keeps a `List<OrderObserver>` and calls
`onOrderStatusChanged(order)` after a successful status update.
`NotificationObserver` writes the row; `OrderViewRefreshObserver` wraps a
`Runnable` supplied by the controller. `OrdersController` registers both at
start-up.

**Alternatives considered.**

- *`OrderService` calling `NotificationService` and the table directly.* The service
  layer would then depend on JavaFX and could not be tested headlessly.
- *A JavaFX `Property` binding.* Fine for the UI refresh alone, but it would not
  carry the notification write, and it would put a UI type in the service layer.

**Consequences.** The order of observer execution is the registration order and is
not otherwise guaranteed, and one observer throwing will stop the ones after it.

**What it makes cheap later.** `OrderObserverTest` registers a fake observer and
asserts the fan-out with no window open. An SMS or e-mail reaction is one more
implementation and one more registration line.

---

### B7. Command — undo and redo in the bouquet builder

*Diagram: `docs/diagrams/command.svg`*

**Context.** Building a bouquet is trial and error. Adding twelve roses and then
changing your mind has to be reversible.

**Decision.** Every reversible edit is a `Command` with `execute()` and `undo()`.
`CommandHistory` keeps an undo stack and a redo stack and never learns what the
commands actually change. Each command stores the minimum needed to reverse itself
— `RemoveExtraCommand` keeps the list *index* so that redo restores the original
order.

**Alternatives considered.**

- *Snapshotting the whole builder after each click (Memento).* Simpler to write,
  but it copies the entire flower list on every keystroke-scale action and gets
  worse as the builder grows fields.
- *A plain "clear and rebuild" button.* Cheaper, and genuinely worse for the user.

**Consequences.** Four command classes for four actions, and the receiver
(`BouquetBuilder` or the selected-extras list) has to expose an operation that
exactly inverts each one.

**What it makes cheap later.** The invoker is generic, so a new reversible action
(change wrapping, reorder flowers) is a new `Command` class. Macro-commands and an
action-history panel become straightforward.

---

### B8. Prototype — partially realised, and knowingly so

*Shown on the Builder diagram.*

**Context.** Saved bouquet templates ("Classic Romance") must be loadable into the
builder and then customised, without editing the stored template.

**Decision.** `BouquetTemplate.copyToBuilder()` returns a fresh `BouquetBuilder`
pre-loaded with *new* `Flower` instances copied from the template's lines. The user
then edits the copy; the template is untouched.

This is Prototype in intent — copy a configured object, then customise — but it is
**not** the textbook form: there is no `Cloneable` interface, no `prototype()`
operation, and no registry of prototypes. It is a single copy method on one class.

**Why we did not formalise it.** There is exactly one prototype type in the system.
Introducing a `Prototype` interface that only `BouquetTemplate` implements would add
an abstraction with a single implementer and no polymorphic call site — the kind of
pattern-for-its-own-sake the brief warns against. If a second copyable design object
appears (a saved event package, for instance), extracting the interface at that
point is a small, mechanical refactor.

> **Note.** The pattern table in `README.md` lists Prototype as a primary pattern and
> Command and Composite as *optional*. That table records the original plan; both
> Command and Composite are now fully implemented, and Prototype is in the reduced
> form described here. The README should be updated to match.

---

## Part C — Patterns considered and deliberately not used

The brief asks for patterns that solve a real problem, not a count. These were
weighed and left out.

| Pattern | Where it would have gone | Why we left it out |
|---|---|---|
| **Singleton** | `DatabaseConnection` | The obvious candidate, and the wrong call. A singleton would hard-code the database path into a static field, and every one of the 18 test classes would then be fighting it. Instead `DatabaseConnection` has a no-arg constructor for the app and a `Path` constructor for tests, and every test points it at a JUnit `@TempDir`. Cheap injection beat global access. |
| **Abstract Factory** | Creating repositories/services per screen | Each controller wires the two or three services it needs in `initialize()`. A factory would centralise that, but there is only one product family and no variation to abstract over. |
| **Facade** | A single entry point over the service layer | The services *are* the facade over the repositories. A second layer of pass-through would add indirection without removing any. |
| **Memento** | Bouquet builder undo | Considered and rejected in favour of Command — see B7. Command stores per-action deltas instead of whole-object snapshots. |
| **Repository interfaces (DIP)** | `interface OrderRepository` + JDBC impl | Would let services be tested against in-memory fakes. Not needed yet: SQLite in a `@TempDir` is fast enough that the real repository *is* the test double. This is the most likely next refactor, and it changes no service method signature. |
| **Template Method** | Shared `create/update/delete` skeleton across repositories | See A2 — the eight repositories diverge more than they share. |

---

## Part D — Known trade-offs

1. **Each controller constructs its own `DatabaseConnection`.** Six controllers call
   `new DatabaseConnection()` independently. SQLite tolerates this (each opens its
   own connection to the same file), but a single connection passed down from `App`
   would be tidier and would make connection settings configurable in one place.
2. **No schema versioning.** See A3. A `schema_version` table would be the first
   thing to add if the schema changes after data exists.
3. **Observer failures are not isolated.** One throwing observer stops later ones.
   Wrapping each notification in a try/catch would make the fan-out resilient.
4. **`ArrangementGroup` exposes `add()` on the component interface's implementer.**
   The classic Composite trade-off: uniformity versus type safety. We chose
   uniformity, and `BouquetArrangement` simply has no children to add.
