# EverBloom — Manual Test Checklist

Run before the demonstration. `mvn clean test` covers the business logic; this
checklist covers the parts that only a running window can show.

Start with `mvn clean javafx:run`. The database file is `data/everbloom.db`; it is
created and seeded on first start.

---

## A. Core Workflow A — build and place a custom bouquet order

| # | Step | Expected result |
|---|---|---|
| A1 | Open **Bouquet Builder** | Order mode is *Single Bouquet*; the summary panel reads "Select a customer and occasion…"; all totals are BDT 0.00 |
| A2 | Choose a customer and an occasion | No change to totals yet (no flowers) |
| A3 | Choose a flower, set quantity 6, press **Add** | The flower appears in the table with its line subtotal; the summary and *Order subtotal* update |
| A4 | Add a second flower | Both lines are listed; the subtotal is the sum |
| A5 | Add the **same** flower again | The existing row's quantity increases; no duplicate row appears |
| A6 | Press **Undo** twice, then **Redo** once | The last two edits are reversed, then the first is reapplied; totals follow each step |
| A7 | Add **Greeting Card** and **Chocolate Box** under *Optional extras* | The summary line reads `… bouquet + Greeting Card + Chocolate Box`; the subtotal grows by both prices |
| A8 | Switch *Pricing policy* to **Loyalty Pricing** | *Discount* becomes 10% of the subtotal; *Final total* drops accordingly; the summary names the policy |
| A9 | Set fulfilment to **DELIVERY** and type an address | The address box becomes editable only after DELIVERY is chosen |
| A10 | Press **Place Order** | Green confirmation `Order EB-… placed successfully.`; the form resets |
| A11 | Open **Orders** | The new order is listed with status `ORDERED` and the discounted total |

### Prototype route through the same workflow

| # | Step | Expected result |
|---|---|---|
| A12 | In **Bouquet Builder**, pick a template and press **Use Template** | Occasion, wrapping, message, and flower lines are filled in from the template |
| A13 | Remove one flower from the copied bouquet, then open **Catalogue → Bouquet Templates** | The template still lists its original flowers — the copy was independent |

---

## B. Core Workflow B — process an order through its lifecycle

| # | Step | Expected result |
|---|---|---|
| B1 | Open **Orders** and select the new order | Order Details shows customer, flowers, extras, pricing policy, subtotal, discount, total |
| B2 | Press **Advance Status** | Status becomes `PREPARING`; a green message confirms it; the row stays selected |
| B3 | Press **Advance Status** three more times | `ARRANGING` → `READY` → `DELIVERED` |
| B4 | Press **Advance Status** once more | Red message: *Delivered orders cannot move to another status.* The status does not change |
| B5 | Open **Dashboard** | *Recent Notifications* lists one notification per successful change, newest first, with a status badge |

---

## C. Persistence after restart

| # | Step | Expected result |
|---|---|---|
| C1 | Close the window and run `mvn javafx:run` again | The application starts without recreating or clearing data |
| C2 | Open **Orders** | Every order placed before the restart is still listed with its last status |
| C3 | Open the order placed in section A | Flower names, quantities, unit prices, extras, and totals are unchanged |
| C4 | Open **Catalogue** and change a flower's price, then reopen the old order | The old order still shows the **original** price — historical values are stored as snapshots |
| C5 | Open **Customers** | A customer added before the restart is still present |

---

## D. Required search / report operations

| # | Step | Expected result |
|---|---|---|
| D1 | **Orders** → type part of a customer name in the search box | The list narrows as you type |
| D2 | **Orders** → set the status filter to `DELIVERED` | Only delivered orders remain; combining it with the search text narrows further |
| D3 | **Orders** → set the fulfilment filter to `DELIVERY` | Only delivery orders remain |
| D4 | **Customers** → search by phone number | The matching customer is shown; clearing the box restores the full list |
| D5 | **Catalogue** → search flowers by colour, and extras by description | Both searches match on more than the name |
| D6 | **Reports** → keep the default range and press **Run Reports** | *Sales by Date* shows one row per day with order count and revenue |
| D7 | **Reports** → check *Popular Flowers* and *Popular Extras* | Both are ordered by quantity, highest first, and match the orders placed today |
| D8 | **Reports** → set the start date after the end date | Red message: *End date must be on or after the start date.* Tables are cleared |
| D9 | **Reports** → choose a range with no orders | Each table shows its own empty-state message |

---

## E. Optional workflow — nested event package (Composite)

| # | Step | Expected result |
|---|---|---|
| E1 | **Bouquet Builder** → set *Order mode* to **Event Package** | The event-package panel appears |
| E2 | Type a package name and press **Create Package** | Confirmation; the package is selectable as a parent group |
| E3 | Add a group `Ceremony`, then with `Ceremony` selected add a subgroup `Altar` | The parent selector lists the package, `Ceremony`, and `Altar` |
| E4 | Select `Altar`, build a bouquet below, name the arrangement, press **Add Current Bouquet** | The summary shows the arrangement indented under `Ceremony → Altar`; the package total includes it |
| E5 | Add a second arrangement under `Ceremony` | The package total is the sum of both, at both levels |
| E6 | Choose a customer, set fulfilment, press **Place Order** | Confirmation naming the order; the package is reloaded from the database with the same tree and total |
| E7 | Open **Orders**, select the event order | Order Details shows the indented package summary instead of a single bouquet |

---

## F. Validation and error conditions

| # | Action | Expected message |
|---|---|---|
| F1 | Bouquet Builder → **Place Order** with no customer chosen | *Select a customer for the bouquet.* |
| F2 | Bouquet Builder → choose a customer but no occasion, then **Build Bouquet** | *Select an occasion for the bouquet.* |
| F3 | Bouquet Builder → **Build Bouquet** with no flowers | *Add at least one flower to the bouquet.* |
| F4 | Bouquet Builder → **Add** with no flower selected | *Select a flower to add.* |
| F5 | Bouquet Builder → add the same extra twice | *This extra is already part of the bouquet.* |
| F6 | Bouquet Builder → **Remove Selected** with nothing selected | *Select a flower from the bouquet to remove.* |
| F7 | Bouquet Builder → DELIVERY with an empty address → **Place Order** | *Enter a delivery address.* |
| F8 | Event Package mode → **Create Package** with an empty name | *Group name is required.* |
| F9 | Event Package mode → **Place Order** before creating a package | *Create an event package first.* |
| F10 | Customers → **Add Customer**, leave the name blank | *Customer name is required.* (dialog stays open) |
| F11 | Customers → save a phone number that already exists | *A customer with this phone number already exists.* |
| F12 | Customers → enter `abc` as the email | *Enter a valid email address.* |
| F13 | Customers → **Edit** / **Delete** with no row selected | *Select a customer to edit / delete.* |
| F14 | Customers → delete a customer who has orders | *This customer cannot be deleted because existing orders use this record.* |
| F15 | Catalogue → add a flower with price `12.999` | *Enter a valid price with up to two decimal places.* |
| F16 | Catalogue → add a flower with stock `-3` or `two` | *Stock quantity cannot be negative.* / *Enter a whole stock quantity.* |
| F17 | Catalogue → add a flower whose name already exists | *A flower with this name already exists.* |
| F18 | Catalogue → delete a flower used by a template or an order | *This flower cannot be deleted because it is used by another record.* |
| F19 | Catalogue → add a template with no flowers | *Enter a name and occasion, then add at least one flower.* |
| F20 | Orders → **Advance Status** with no row selected | *Select an order first.* |

---

## G. Presentation checks

| # | Check | Expected result |
|---|---|---|
| G1 | Resize the window from its minimum to full screen on every screen | No column, card, or button is cut off; tables stretch with the window |
| G2 | Every screen with no data | A readable empty-state message appears instead of a blank table |
| G3 | Success versus failure messages | Success messages are green, failures are red |
| G4 | Status values in Dashboard tables | Each status has its own badge colour, including `ORDERED` |
| G5 | Every dialog | Uses the application's colours and fonts, not the default JavaFX theme |
