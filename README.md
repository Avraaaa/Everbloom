# EverBloom — Custom Bouquet & Flower Shop Manager

EverBloom is a desktop flower-shop management application built with **JavaFX**, **Maven**, and **SQLite**.

The application helps flower-shop staff create highly customizable bouquets, reuse bouquet templates, manage customer orders, apply different pricing policies, track orders through preparation and delivery, and build larger wedding or event flower packages. The main focus of the system is the bouquet-ordering workflow, where customers can select flowers, wrapping, and optional extras while the application keeps the design, pricing, and order process organized.

---

## Project Description

EverBloom is designed for small and medium flower shops that handle both ready-made and custom flower orders. Instead of recording an order as only a few text fields, the application treats a bouquet as a configurable product that can be built step-by-step.

A staff member can start a bouquet from scratch or copy an existing template such as a birthday, anniversary, wedding, or sympathy arrangement. The bouquet can then be customized by changing flowers and quantities, selecting wrapping, and adding extras such as chocolates, greeting cards, ribbons, balloons, or premium packaging. The application calculates the price using the selected pricing policy and saves the completed order to SQLite.

After an order is placed, staff can move it through the workflow **Ordered → Preparing → Arranging → Ready → Delivered**. Different actions are allowed depending on the current order state, and other parts of the application can react automatically when the state changes. EverBloom also supports larger wedding or event orders made up of several smaller arrangements.

---

## Problem Statement

Custom bouquet orders are difficult to manage using simple paper records or basic CRUD-style software because a single order may contain many flowers, quantities, optional decorations, pricing rules, customer instructions, and preparation stages. As the number of possible combinations increases, handling all of these variations with large constructors, repeated conditional statements, or separate classes for every bouquet combination quickly becomes difficult to maintain.

Flower shops also need to deal with changes during order creation. A customer may add or remove flowers, change wrapping, choose a different discount, or start from an existing bouquet design and customize it. After the order is confirmed, staff need a clear workflow for preparation and delivery, while the dashboard and notification system need to stay synchronized with order-status changes.

EverBloom addresses these problems by separating bouquet construction, optional enhancements, pricing behavior, editing actions, reusable templates, order states, notifications, and event-package composition into focused components. This keeps the application easier to extend when new flowers, extras, pricing policies, bouquet templates, order states, or event-package types are introduced later.

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | JavaFX |
| Language | Java |
| Build / Dependency Management | Maven |
| Database | SQLite |
| Database Access | JDBC |
| Testing | JUnit |

---

## Core Features

- **Custom bouquet builder** — create bouquets step-by-step by selecting flowers, quantities, wrapping, and other options.
- **Bouquet templates** — start from reusable birthday, anniversary, wedding, sympathy, or other saved designs and customize a separate copy.
- **Bouquet extras** — add chocolates, greeting cards, ribbons, balloons, premium wrapping, and similar enhancements.
- **Undo / redo** — reverse or restore bouquet-editing actions while designing an order.
- **Pricing policies** — apply the standard policy or the loyalty discount policy at checkout.
- **Customer management** — create, edit, delete, and search customer records.
- **Order management** — place orders, then search and filter them by order number, customer, status, and fulfilment type.
- **Order workflow** — process orders through **Ordered → Preparing → Arranging → Ready → Delivered**.
- **Notifications** — create in-app notifications when important order-status changes occur.
- **Pickup and delivery** — record whether an order is collected in store or delivered to an address.
- **Wedding and event packages** — build larger orders containing multiple flower arrangements.
- **Catalogue management** — manage flowers, extras, availability, prices, and bouquet templates.
- **Search and filtering** — search orders by customer or order number, and filter by status and fulfilment type.
- **Reports and analytics** — sales by date, most popular flowers, and most popular extras across a chosen date range.

---

## Main Workflows

1. **Custom Bouquet Order** — Select a template or start from scratch → add flowers → choose wrapping → add extras → adjust the bouquet with undo/redo if needed → calculate price → enter customer/order details → place order.
2. **Order Processing** — Place order → start preparation → arrange bouquet → mark ready → notify relevant parts of the application → complete pickup/delivery.
3. **Event Package Creation** — Create event package → add bridal/table/entrance or other arrangements → configure each arrangement → calculate the total package price → place the event order.

---

## Planned Design Patterns

EverBloom uses **six primary design patterns** that directly support the core bouquet-order workflow, plus **two optional patterns** that were kept because the undo/redo editor and the nested event package are both genuinely part of the final scope. Each pattern is justified in [`docs/PATTERN-JUSTIFICATION.md`](docs/PATTERN-JUSTIFICATION.md).

| Pattern | Status | Where it is used | Why |
|---|---|---|---|
| **Builder** | Primary | Custom bouquet construction | A bouquet contains many optional and configurable parts, so it is built step-by-step instead of using a large constructor. |
| **Strategy** | Primary | Pricing and discount policies | Different pricing algorithms can be swapped without changing the checkout workflow. |
| **State** | Primary | Order lifecycle | Available actions and valid transitions depend on whether an order is Ordered, Preparing, Arranging, Ready, or Delivered. |
| **Observer** | Primary | Order-status reactions | Notifications, dashboard information, and other independent components can react when an order status changes without being tightly coupled to the order logic. |
| **Prototype** | Primary | Reusable bouquet templates | Saved bouquet designs can be copied into independent bouquets and then customized without changing the original template. |
| **Decorator** | Primary | Bouquet extras such as chocolates, ribbons, cards, balloons, and premium wrapping | Optional additions can be layered onto a bouquet dynamically without creating a separate subclass for every possible combination. |
| **Command** | Implemented | Bouquet editing and undo/redo | Adding and removing flowers and extras are real reversible editor actions, so each one is represented as a command with `execute()` and `undo()`. |
| **Composite** | Implemented | Wedding/event packages | An event package is a tree of groups and arrangements, and the package total is a recursive sum over that tree. |

---

## Database Schema

| Table | Purpose / Main columns |
|---|---|
| `customers` | `customer_id`, `full_name`, `phone` (unique), `email`, `address`, `created_at`, `updated_at` |
| `flowers` | `flower_id`, `name` (unique), `color`, `current_unit_price`, `stock_quantity`, `is_active` |
| `extras` | `extra_id`, `name` (unique), `description`, `current_unit_price`, `is_active` |
| `bouquet_templates` | `template_id`, `name` (unique), `occasion`, `description`, `wrapping_style`, `message_text`, `is_active` |
| `bouquet_template_flowers` | `template_id`, `flower_id`, `quantity` — flowers included in a reusable template |
| `orders` | `order_id`, `order_number` (unique), `customer_id`, `occasion_snapshot`, `fulfillment_type`, `delivery_address_snapshot`, `status`, `pricing_policy_snapshot`, `subtotal_snapshot`, `discount_snapshot`, `total_snapshot`, `placed_at`, `status_updated_at` |
| `arrangements` | `arrangement_id`, `order_id`, `parent_arrangement_id`, `name_snapshot`, `occasion_snapshot`, `wrapping_style_snapshot`, `message_text_snapshot`, price snapshots — one row per bouquet, or a tree of rows for a nested event package |
| `arrangement_flowers` | `arrangement_flower_id`, `arrangement_id`, `flower_id`, `flower_name_snapshot`, `unit_price_snapshot`, `quantity`, `line_total_snapshot` |
| `arrangement_extras` | `arrangement_extra_id`, `arrangement_id`, `extra_id`, `extra_name_snapshot`, `unit_price_snapshot`, `quantity`, `line_total_snapshot` |
| `notifications` | `notification_id`, `order_id`, `status_snapshot`, `message`, `is_read`, `created_at` |

Money is stored as integer minor units. Columns ending in `_snapshot` keep the values that were true when the order was placed, so historical orders do not change when catalogue prices change.

Primary keys, foreign keys, and appropriate constraints are used to maintain relationships between the tables. Seeder code creates the database tables and inserts initial flower, extra, template, and sample data when required.

---

## Application Areas

### Dashboard
Shows current orders, order-status summaries, recent activity, and notifications.

### Bouquet Builder
Lets staff select a template or start from scratch, add flowers, choose wrapping, add extras, undo/redo edits, preview pricing, and place the order.

### Orders
Displays existing orders with search and filtering. The order-details view shows customer information, bouquet contents, pricing, fulfilment details, and the current order state.

### Catalogue
Manages flowers, extras, availability, prices, and reusable bouquet templates.

### Customers
Stores customer information with search, and shows the details of the selected customer.

### Reports
Provides sales-by-date summaries and popularity reports for flowers and extras over a chosen date range.

---

## Documentation

| Document | Contents |
|---|---|
| [`docs/PATTERN-JUSTIFICATION.md`](docs/PATTERN-JUSTIFICATION.md) | Why each design pattern was used, the simpler alternative, where it lives in the code, and the benefit |
| [`docs/MANUAL-TEST-CHECKLIST.md`](docs/MANUAL-TEST-CHECKLIST.md) | Manual checks for both core workflows, persistence after restart, search/report operations, and validation |
| [`docs/VIVA-CHECKLIST.md`](docs/VIVA-CHECKLIST.md) | Demonstration order and the questions each team member should be able to answer |
| [`docs/SCREENSHOTS.md`](docs/SCREENSHOTS.md) | Screenshots of the main screens and workflows |
| [`docs/diagrams/uml-class-diagrams.pdf`](docs/diagrams/uml-class-diagrams.pdf) | UML class diagrams for the pattern-heavy areas and the layering |
| [`docs/diagrams/er-diagram.pdf`](docs/diagrams/er-diagram.pdf) | ER diagram for the actual SQLite schema |
| [`docs/domain-database-design.md`](docs/domain-database-design.md) | Schema baseline |
| [`docs/ui-behavior-specification.md`](docs/ui-behavior-specification.md) | Screen behavior baseline |
| [`docs/DESIGN-DECISIONS.md`](docs/DESIGN-DECISIONS.md) | Longer record of architecture and pattern decisions |

---

## Architecture

```text
JavaFX Views / Controllers
          ↓
    Application Services
          ↓
 Domain Model & Design Patterns
          ↓
    Repository / DAO Layer
          ↓
         SQLite
```

---

## Running the Application

### Requirements

- JDK 21
- Maven

### Run

```bash
mvn clean javafx:run
```

### Run Tests

```bash
mvn test
```

---

## Team

- **Member 1:** [Fahmida Hossain] — Roll: [1603]
- **Member 2:** [Arithro Choudhury] — Roll: [1647]
