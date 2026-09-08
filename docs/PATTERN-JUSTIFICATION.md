# EverBloom — Design Pattern Justification

Eight GoF patterns are used. Six were planned from the start because the core
bouquet-order workflow contains the problems they solve. Two (Command and
Composite) were added late, only after the editor and the event-package
requirement made them necessary.

Each entry answers five questions: the real problem, why the pattern fits, the
simpler alternative we rejected, where it lives in the code, and one concrete
benefit.

Diagrams: [`diagrams/uml-class-diagrams.pdf`](diagrams/uml-class-diagrams.pdf).

---

## 1. Builder — custom bouquet construction

**Owner:** Fahmida Hossain

**Problem.** A bouquet is assembled over several screen interactions: a customer,
an occasion, several flower lines added one at a time, optional wrapping, and an
optional card message. Nothing is complete until the staff member stops editing,
and stock and quantity rules must hold for every line.

**Why Builder fits.** Construction is genuinely step-by-step and partially
optional, and validity is only checkable at the end. `BouquetBuilder` accumulates
choices and `build()` refuses to produce an invalid `Bouquet`, so a `Bouquet`
object can never exist in a half-configured state.

**Simpler alternative rejected.** A single `Bouquet` constructor with six
parameters, or setters on a mutable `Bouquet`. The constructor cannot be called
until the user has finished, so the controller would have to keep six loose
fields; setters would let an invalid bouquet be priced and saved.

**Where it is.** `model/BouquetBuilder.java`, `model/Bouquet.java`;
used by `controller/BouquetBuilderController.java`.

**Benefit.** Adding another optional part (a vase, a preferred delivery slot) is
one builder method plus one rule in `validateBouquet()`; no existing caller
changes and `Bouquet` stays immutable.

---

## 2. Prototype — reusable bouquet templates

**Owner:** Arithro Choudhury

**Problem.** Staff start most orders from a saved design such as "Classic
Romance", then change it for this customer. The saved template must not change
when the copy is edited, and two orders started from the same template must not
affect each other.

**Why Prototype fits.** The requirement is literally "copy this configured object
and edit the copy". `BouquetTemplate.copyToBuilder()` returns a fresh
`BouquetBuilder` holding new `Flower` objects, so the copy shares no mutable
state with the template.

**Simpler alternative rejected.** Handing the template's own flower list to the
builder. It is one line shorter and silently corrupts the template — editing one
order would rewrite the shared design for every future order.

**Where it is.** `model/BouquetTemplate.copyToBuilder()`,
`service/BouquetTemplateService.copyToBuilder()`; proved by
`BouquetTemplateServiceTest.keepsTemplateAndOtherClonesUnchangedWhenOneCloneIsEdited`.

**Benefit.** New template fields (a default extra, a default ribbon colour) are
copied in one place, and every consumer of a template keeps its independence for
free.

---

## 3. Decorator — optional bouquet extras

**Owner:** Fahmida Hossain

**Problem.** A bouquet can carry any combination of premium wrapping, a greeting
card, a ribbon, chocolates, and balloons. Each extra adds to the price and to the
printed description, and the combinations are chosen at run time.

**Why Decorator fits.** Each extra is a wrapper that answers the same two
questions as the thing it wraps — `getDescription()` and `getSubtotal()` — and
delegates the rest. Wrapping composes, so five extras give thirty-two possible
bouquets from two classes.

**Simpler alternative rejected.** A `List<Extra>` on `Bouquet` with the pricing
code summing it. That works for price alone, but pricing and the summary would
then each need their own loop, and `PricingService` would have to know about
extras instead of depending on one `BouquetItem` interface.

**Where it is.** `model/BouquetItem.java` (component), `model/BaseBouquet.java`
(leaf), `model/BouquetDecorator.java` (abstract decorator),
`model/ExtraDecorator.java`; composed in
`BouquetBuilderController.decorateBouquet()`.

**Benefit.** An extra with different arithmetic — a percentage gift-wrap fee, a
per-stem preservative — is a new subclass of `BouquetDecorator`. Neither
`Bouquet`, nor `PricingService`, nor the controller changes.

---

## 4. Strategy — pricing policies

**Owner:** Arithro Choudhury

**Problem.** The shop charges list price to walk-in customers and a 10% loyalty
discount to regulars. More policies are expected. Checkout must not become a
chain of `if (policy.equals(...))` blocks.

**Why Strategy fits.** The policies differ only in one interchangeable
calculation over a subtotal. `PricingStrategy` names that calculation, and
`PricingService` depends on the interface rather than on any policy.

**Simpler alternative rejected.** A `switch` on a policy string inside
`PricingService`. With two policies the switch is smaller, but every new policy
edits a method that already works, and the discount arithmetic cannot be
unit-tested independently of checkout.

**Where it is.** `pricing/PricingStrategy.java`,
`pricing/StandardPricingStrategy.java`, `pricing/LoyaltyPricingStrategy.java`;
consumed by `service/PricingService.java`. The chosen policy name is stored on the
order as `pricing_policy_snapshot`.

**Benefit.** A seasonal or bulk-event policy is one new class plus one entry in
the policy combo box. `PricingService`, `OrderService`, and the orders schema are
untouched.

---

## 5. State — order lifecycle

**Owner:** Arithro Choudhury

**Problem.** An order moves `ORDERED → PREPARING → ARRANGING → READY →
DELIVERED`. Each status allows exactly one next step, and a delivered order must
refuse to move at all. Left in the controller, that rule becomes a status
conditional repeated on every screen that can advance an order.

**Why State fits.** The allowed transition *is* the behaviour that varies with
status. Each state class knows only its own successor, and `Order.advanceStatus()`
delegates. `DeliveredState` enforces the terminal rule by throwing rather than by
a guard clause somewhere else.

**Simpler alternative rejected.** A status `String` plus a `switch` in
`OrderService.advanceOrder`. It is fewer files, but the transition table then
lives in a service that also does persistence and observer notification, and
adding a state means editing that method instead of adding a class.

**Where it is.** `state/OrderState.java` and the five state classes;
`model/Order.java` holds the current state; `service/OrderService.advanceOrder`
persists the result.

**Benefit.** Inserting a `CANCELLED` state, or letting `READY` branch to either
`DELIVERED` or `COLLECTED`, is a new class and one changed `advance()` method.
The controller and the repository are unaffected.

---

## 6. Observer — reactions to a status change

**Owner:** Fahmida Hossain

**Problem.** When an order successfully advances, two unrelated things must
happen: a notification row is written, and the orders table on screen refreshes.
More reactions (SMS, a printed ticket) are plausible. Failed transitions must
trigger nothing.

**Why Observer fits.** The reactions are independent of each other and of the
transition itself. `OrderService` publishes the event and knows nothing about
notifications or JavaFX; each observer knows only its own job. Because
`notifyObservers` runs after the state change and the database write, a rejected
transition throws before any observer is called.

**Simpler alternative rejected.** `OrderService` calling
`NotificationService.create(...)` and then a controller callback directly. That
couples the service to persistence it does not own and to the UI, and every new
reaction edits `advanceOrder`.

**Where it is.** `observer/OrderObserver.java`,
`observer/NotificationObserver.java`, `observer/OrderViewRefreshObserver.java`;
subject is `service/OrderService` (`addObserver`, `notifyObservers`); wired in
`OrdersController.initialize()`. Proved by `OrderObserverTest`.

**Benefit.** A third reaction is one small class and one `addObserver` call in the
screen that wants it. `OrderService` never changes.

---

## 7. Command — bouquet undo/redo *(optional, kept)*

**Owner:** Fahmida Hossain

**Problem.** While designing a bouquet the staff member adds a flower, removes a
flower, adds an extra, removes an extra — often against the customer's spoken
changes of mind. Without undo, a mistaken removal means rebuilding the bouquet.

**Why Command fits.** Each of those four actions is genuinely reversible, and the
information needed to reverse it (which flower, what quantity, which list
position) is exactly what the action already holds. Storing the action objects
gives undo and redo from one small history class.

**Simpler alternative rejected.** Snapshotting the whole builder before every
edit and restoring it (a Memento-style stack). It works, but it copies the entire
bouquet on each click, and it cannot describe *what* was undone if the UI ever
needs to say so.

**Where it is.** `command/Command.java`, `AddFlowerCommand`,
`RemoveFlowerCommand`, `AddExtraCommand`, `RemoveExtraCommand`,
`command/CommandHistory.java`; driven by the Undo/Redo buttons in
`BouquetBuilderController`.

**Why it was not skipped.** Command was planned as optional and only to be kept
if the finished editor turned out to have real reversible actions. It has four,
not one, so it stayed.

**Benefit.** A new reversible edit — change wrapping, change a quantity in place —
is one class implementing two methods; `CommandHistory` and the buttons already
work.

---

## 8. Composite — nested event packages *(optional, kept)*

**Owner:** Arithro Choudhury

**Problem.** A wedding order is not a flat list of bouquets. It is "Wedding
Package → Ceremony → {Bridal Bouquet, Altar Arrangement}" and "Wedding Package →
Reception → Tables → {Head Table, Guest Tables}". The package total is the sum of
everything underneath it, at any depth.

**Why Composite fits.** Groups and single arrangements are asked the same three
questions — name, total price, summary — and a group answers by asking its
children. The recursion in `ArrangementGroup.getTotalPrice()` is three lines and
handles any nesting depth. The tree is stored with the self-referencing
`arrangements.parent_arrangement_id` foreign key and rebuilt on load.

**Simpler alternative rejected.** A flat `List<Bouquet>` per order. It cannot
express "the reception's flowers cost X" without the caller re-deriving the
grouping, and pricing or renaming a subtree would mean tracking parent
relationships outside the model anyway.

**Where it is.** `composite/EventPackageComponent.java` (component),
`composite/BouquetArrangement.java` (leaf), `composite/ArrangementGroup.java`
(composite); persisted by `OrderRepository.createEventPackage` /
`findEventPackage`; used in the Event Package mode of the Bouquet Builder. Proved
by `ArrangementGroupTest` and `EventPackagePersistenceTest`.

**Why it was not skipped.** Composite was planned as optional and only to be kept
if event packages turned out to need nesting. They do — groups sit inside groups —
so a flat `List` would not have been enough.

**Benefit.** A new component type — a rented vase set, a delivery charge line —
implements `EventPackageComponent` and immediately participates in the totals, the
summary, and the saved tree.

---

## Patterns deliberately not used

Singleton, Factory Method, Abstract Factory, Adapter, Template Method, Visitor,
Chain of Responsibility, Memento, and Proxy were considered and left out. With two
pricing policies a factory adds indirection without removing a decision; a
single `DatabaseConnection` instance is passed explicitly to each repository,
which is clearer to test than a global Singleton; and Memento would duplicate what
Command already provides. Each of these would have raised the pattern count
without solving a problem the application actually has.
