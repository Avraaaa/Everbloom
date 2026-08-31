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
- **Pricing policies** — apply standard, loyalty, seasonal, or bulk/event pricing rules.
- **Customer management** — store customer information and view previous orders.
- **Order management** — create, edit, cancel, search, and view flower-shop orders.
- **Order workflow** — process orders through **Ordered → Preparing → Arranging → Ready → Delivered**.
- **Notifications** — create in-app notifications when important order-status changes occur.
- **Pickup and delivery** — record requested date/time, pickup information, or delivery address.
- **Wedding and event packages** — build larger orders containing multiple flower arrangements.
- **Catalogue management** — manage flowers, extras, availability, prices, and bouquet templates.
- **Search and filtering** — search orders by customer, order number, occasion, date, or status.
- **Reports and analytics** — view sales summaries, popular flowers, popular extras, and popular occasions.

---

## Main Workflows

1. **Custom Bouquet Order** — Select a template or start from scratch → add flowers → choose wrapping → add extras → adjust the bouquet with undo/redo if needed → calculate price → enter customer/order details → place order.
2. **Order Processing** — Place order → start preparation → arrange bouquet → mark ready → notify relevant parts of the application → complete pickup/delivery.
3. **Event Package Creation** — Create event package → add bridal/table/entrance or other arrangements → configure each arrangement → calculate the total package price → place the event order.

---

## Planned Design Patterns

EverBloom is planned around **six primary design patterns** that directly support the core bouquet-order workflow. Two additional patterns are **optional** and will only be included if their related features remain in the final scope.

| Pattern | Status | Where it is used | Why |
|---|---|---|---|
| **Builder** | Primary | Custom bouquet construction | A bouquet contains many optional and configurable parts, so it is built step-by-step instead of using a large constructor. |
| **Strategy** | Primary | Pricing and discount policies | Different pricing algorithms can be swapped without changing the checkout workflow. |
| **State** | Primary | Order lifecycle | Available actions and valid transitions depend on whether an order is Ordered, Preparing, Arranging, Ready, or Delivered. |
| **Observer** | Primary | Order-status reactions | Notifications, dashboard information, and other independent components can react when an order status changes without being tightly coupled to the order logic. |
| **Prototype** | Primary | Reusable bouquet templates | Saved bouquet designs can be copied into independent bouquets and then customized without changing the original template. |
| **Decorator** | Primary | Bouquet extras such as chocolates, ribbons, cards, balloons, and premium wrapping | Optional additions can be layered onto a bouquet dynamically without creating a separate subclass for every possible combination. |
| **Command** | Optional | Bouquet editing and undo/redo | Included only if undo/redo is kept in the final scope; each reversible editing action can then be represented as a command. |
| **Composite** | Optional | Wedding/event packages | Included only if event packages support multiple or nested arrangements that need to be handled uniformly. |

---

## Database Schema

| Table | Purpose / Main Fields |
|---|---|
| `Customers` | `id`, `name`, `phone`, `email`, `loyalty_status` |
| `Flowers` | `id`, `name`, `category`, `color`, `unit_price`, `stock_quantity`, `is_available` |
| `Extras` | `id`, `name`, `type`, `unit_price`, `is_available` |
| `BouquetTemplates` | `id`, `name`, `occasion`, `description` |
| `BouquetTemplateFlowers` | `template_id`, `flower_id`, `quantity` — flowers included in a reusable template |
| `Orders` | `id`, `customer_id`, `order_date`, `required_date`, `occasion`, `status`, `pricing_policy`, `fulfilment_type`, `delivery_address`, `subtotal`, `discount_amount`, `total_amount`, `greeting_message` |
| `Arrangements` | `id`, `order_id`, `parent_arrangement_id`, `template_id`, `name`, `arrangement_type`, `wrapping_type` — supports normal bouquets and nested event-package arrangements |
| `ArrangementFlowers` | `arrangement_id`, `flower_id`, `quantity`, `unit_price` |
| `ArrangementExtras` | `arrangement_id`, `extra_id`, `quantity`, `unit_price` |
| `Notifications` | `id`, `order_id`, `message`, `is_read`, `created_at` |

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
Stores customer information and displays previous order history.

### Reports
Provides basic sales summaries and popularity reports for flowers, extras, and occasions.

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

- JDK 17 or later
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
