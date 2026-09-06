# EverBloom Domain and Database Design Baseline

## Scope

This document defines the planned persistent domain for the core EverBloom workflow. It is a schema direction for the later SQLite initializer and repositories, not an implementation. SQLite foreign-key enforcement must be enabled for every connection.

All money values are stored as integer minor units, such as paisa or cents, rather than floating-point values. Catalogue prices are current selling prices. Arrangement and order prices are immutable snapshots captured when an order is placed.

## Entity Relationships

```text
customers 1 ---- * orders 1 ---- * arrangements
                                  |
                                  + ---- * arrangement_flowers * ---- 1 flowers
                                  |
                                  + ---- * arrangement_extras  * ---- 1 extras
                                  |
                                  + ---- 0..1 parent arrangement

bouquet_templates 1 ---- * bouquet_template_flowers * ---- 1 flowers

orders 1 ---- * notifications
```

An order belongs to one customer and contains one or more arrangements. Current core orders will create a single root arrangement. `parent_arrangement_id` only reserves a possible future parent-child relationship; it does not introduce event-package behavior or Composite code.

## Tables

### `customers`

| Column | Definition |
|---|---|
| `customer_id` | `INTEGER` primary key |
| `full_name` | `TEXT NOT NULL CHECK (length(trim(full_name)) > 0)` |
| `phone` | `TEXT NOT NULL UNIQUE CHECK (length(trim(phone)) > 0)` |
| `email` | `TEXT CHECK (email IS NULL OR length(trim(email)) > 0)`, optional |
| `address` | `TEXT CHECK (address IS NULL OR length(trim(address)) > 0)`, optional default address |
| `created_at` | `TEXT NOT NULL` ISO-8601 timestamp |
| `updated_at` | `TEXT NOT NULL` ISO-8601 timestamp |

`phone` is the initial duplicate-prevention key. A later service may normalize phone input before persistence so equivalent formats do not become separate customers.

### `flowers`

| Column | Definition |
|---|---|
| `flower_id` | `INTEGER` primary key |
| `name` | `TEXT NOT NULL UNIQUE COLLATE NOCASE CHECK (length(trim(name)) > 0)` |
| `color` | `TEXT`, optional |
| `current_unit_price` | `INTEGER NOT NULL CHECK (current_unit_price >= 0)` |
| `stock_quantity` | `INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0)` |
| `is_active` | `INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1))` |
| `created_at` | `TEXT NOT NULL` ISO-8601 timestamp |
| `updated_at` | `TEXT NOT NULL` ISO-8601 timestamp |

`is_active` supports retiring a flower from new selections while preserving data that references it.

### `extras`

| Column | Definition |
|---|---|
| `extra_id` | `INTEGER` primary key |
| `name` | `TEXT NOT NULL UNIQUE COLLATE NOCASE CHECK (length(trim(name)) > 0)` |
| `description` | `TEXT`, optional |
| `current_unit_price` | `INTEGER NOT NULL CHECK (current_unit_price >= 0)` |
| `is_active` | `INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1))` |
| `created_at` | `TEXT NOT NULL` ISO-8601 timestamp |
| `updated_at` | `TEXT NOT NULL` ISO-8601 timestamp |

Extras are optional sellable additions, such as wrapping, a card, ribbon, chocolate, or balloons. They are catalogue data; pricing combinations will be handled later by the planned Decorator and Strategy work, not by this schema.

### `bouquet_templates`

| Column | Definition |
|---|---|
| `template_id` | `INTEGER` primary key |
| `name` | `TEXT NOT NULL UNIQUE COLLATE NOCASE CHECK (length(trim(name)) > 0)` |
| `occasion` | `TEXT NOT NULL CHECK (length(trim(occasion)) > 0)` |
| `description` | `TEXT`, optional |
| `wrapping_style` | `TEXT`, optional |
| `message_text` | `TEXT`, optional default card/message text |
| `is_active` | `INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1))` |
| `created_at` | `TEXT NOT NULL` ISO-8601 timestamp |
| `updated_at` | `TEXT NOT NULL` ISO-8601 timestamp |

Templates define reusable catalogue compositions only. They are not historical orders and therefore do not hold price snapshots.

### `bouquet_template_flowers`

| Column | Definition |
|---|---|
| `template_id` | `INTEGER NOT NULL`, foreign key to `bouquet_templates(template_id)` |
| `flower_id` | `INTEGER NOT NULL`, foreign key to `flowers(flower_id)` |
| `quantity` | `INTEGER NOT NULL CHECK (quantity > 0)` |

Primary key: (`template_id`, `flower_id`). This prevents the same flower from appearing twice in one template; its quantity is updated instead.

### `orders`

| Column | Definition |
|---|---|
| `order_id` | `INTEGER` primary key |
| `order_number` | `TEXT NOT NULL UNIQUE CHECK (length(trim(order_number)) > 0)` |
| `customer_id` | `INTEGER NOT NULL`, foreign key to `customers(customer_id)` |
| `occasion_snapshot` | `TEXT NOT NULL CHECK (length(trim(occasion_snapshot)) > 0)` |
| `fulfillment_type` | `TEXT NOT NULL CHECK (fulfillment_type IN ('PICKUP', 'DELIVERY'))` |
| `delivery_address_snapshot` | `TEXT`, required for `DELIVERY` and absent for `PICKUP` by a table check |
| `status` | `TEXT NOT NULL CHECK (status IN ('ORDERED', 'PREPARING', 'ARRANGING', 'READY', 'DELIVERED'))` |
| `pricing_policy_snapshot` | `TEXT NOT NULL CHECK (length(trim(pricing_policy_snapshot)) > 0)` |
| `subtotal_snapshot` | `INTEGER NOT NULL CHECK (subtotal_snapshot >= 0)` |
| `discount_snapshot` | `INTEGER NOT NULL DEFAULT 0 CHECK (discount_snapshot >= 0 AND discount_snapshot <= subtotal_snapshot)` |
| `total_snapshot` | `INTEGER NOT NULL CHECK (total_snapshot >= 0 AND total_snapshot = subtotal_snapshot - discount_snapshot)` |
| `placed_at` | `TEXT NOT NULL` ISO-8601 timestamp |
| `status_updated_at` | `TEXT NOT NULL` ISO-8601 timestamp |

The delivery rule is `fulfillment_type = 'DELIVERY' AND length(trim(delivery_address_snapshot)) > 0`, or `fulfillment_type = 'PICKUP' AND delivery_address_snapshot IS NULL`.

`status` is deliberately a simple persisted value at this stage. The future State implementation will restore the matching state object and enforce valid transitions; it will not add a second source of truth. Cancellation is not represented until it becomes an approved core requirement.

### `arrangements`

| Column | Definition |
|---|---|
| `arrangement_id` | `INTEGER` primary key |
| `order_id` | `INTEGER NOT NULL`, foreign key to `orders(order_id)` |
| `parent_arrangement_id` | `INTEGER CHECK (parent_arrangement_id IS NULL OR parent_arrangement_id <> arrangement_id)`, nullable foreign key to `arrangements(arrangement_id)` |
| `name_snapshot` | `TEXT NOT NULL CHECK (length(trim(name_snapshot)) > 0)` |
| `occasion_snapshot` | `TEXT NOT NULL CHECK (length(trim(occasion_snapshot)) > 0)` |
| `wrapping_style_snapshot` | `TEXT`, optional |
| `message_text_snapshot` | `TEXT`, optional |
| `base_price_snapshot` | `INTEGER NOT NULL CHECK (base_price_snapshot >= 0)` |
| `extras_price_snapshot` | `INTEGER NOT NULL DEFAULT 0 CHECK (extras_price_snapshot >= 0)` |
| `arrangement_total_snapshot` | `INTEGER NOT NULL CHECK (arrangement_total_snapshot >= 0 AND arrangement_total_snapshot = base_price_snapshot + extras_price_snapshot)` |

An application-level rule will initially require `parent_arrangement_id` to be null and exactly one root arrangement for an ordinary order. If nesting is implemented later, parent and child arrangements must belong to the same order; SQLite cannot express that cross-row rule with a simple foreign key, so the later service/repository must validate it. `parent_arrangement_id` must never equal `arrangement_id`.

### `arrangement_flowers`

| Column | Definition |
|---|---|
| `arrangement_flower_id` | `INTEGER` primary key |
| `arrangement_id` | `INTEGER NOT NULL`, foreign key to `arrangements(arrangement_id)` |
| `flower_id` | `INTEGER NOT NULL`, foreign key to `flowers(flower_id)` |
| `flower_name_snapshot` | `TEXT NOT NULL CHECK (length(trim(flower_name_snapshot)) > 0)` |
| `unit_price_snapshot` | `INTEGER NOT NULL CHECK (unit_price_snapshot >= 0)` |
| `quantity` | `INTEGER NOT NULL CHECK (quantity > 0)` |
| `line_total_snapshot` | `INTEGER NOT NULL CHECK (line_total_snapshot = unit_price_snapshot * quantity)` |

Unique key: (`arrangement_id`, `flower_id`). Each line carries its own name and unit-price snapshot so flower renames or repricing do not alter historical reconstruction.

### `arrangement_extras`

| Column | Definition |
|---|---|
| `arrangement_extra_id` | `INTEGER` primary key |
| `arrangement_id` | `INTEGER NOT NULL`, foreign key to `arrangements(arrangement_id)` |
| `extra_id` | `INTEGER NOT NULL`, foreign key to `extras(extra_id)` |
| `extra_name_snapshot` | `TEXT NOT NULL CHECK (length(trim(extra_name_snapshot)) > 0)` |
| `unit_price_snapshot` | `INTEGER NOT NULL CHECK (unit_price_snapshot >= 0)` |
| `quantity` | `INTEGER NOT NULL CHECK (quantity > 0)` |
| `line_total_snapshot` | `INTEGER NOT NULL CHECK (line_total_snapshot = unit_price_snapshot * quantity)` |

Unique key: (`arrangement_id`, `extra_id`). These snapshots preserve both the chosen extra and its then-current price.

### `notifications`

| Column | Definition |
|---|---|
| `notification_id` | `INTEGER` primary key |
| `order_id` | `INTEGER NOT NULL`, foreign key to `orders(order_id)` |
| `status_snapshot` | `TEXT NOT NULL CHECK (status_snapshot IN ('ORDERED', 'PREPARING', 'ARRANGING', 'READY', 'DELIVERED'))` |
| `message` | `TEXT NOT NULL CHECK (length(trim(message)) > 0)` |
| `is_read` | `INTEGER NOT NULL DEFAULT 0 CHECK (is_read IN (0, 1))` |
| `created_at` | `TEXT NOT NULL` ISO-8601 timestamp |

Notifications are stored results of order-status reactions. Their creation is planned for the later Observer feature; this table does not itself implement Observer behavior.

## Foreign Keys and Deletion Behavior

| Child reference | Delete behavior | Reason |
|---|---|---|
| `orders.customer_id` | `ON DELETE RESTRICT` | Customer order history must remain attributable to an existing customer. |
| `bouquet_template_flowers.template_id` | `ON DELETE CASCADE` | A deleted template has no useful composition rows. |
| `bouquet_template_flowers.flower_id` | `ON DELETE RESTRICT` | A referenced flower cannot be physically removed; retire it with `is_active`. |
| `arrangements.order_id` | `ON DELETE CASCADE` | Arrangement snapshots are dependent on their order if an exceptional administrative order deletion is ever permitted. Normal business use does not physically delete orders. |
| `arrangements.parent_arrangement_id` | `ON DELETE RESTRICT` | A parent cannot be removed while child arrangements exist; future nesting needs an explicit deletion workflow. |
| `arrangement_flowers.arrangement_id` | `ON DELETE CASCADE` | A line item has no meaning without its arrangement. |
| `arrangement_flowers.flower_id` | `ON DELETE RESTRICT` | Historical lines retain their catalogue reference; use `is_active` instead of deleting used flowers. |
| `arrangement_extras.arrangement_id` | `ON DELETE CASCADE` | A line item has no meaning without its arrangement. |
| `arrangement_extras.extra_id` | `ON DELETE RESTRICT` | Historical lines retain their catalogue reference; use `is_active` instead of deleting used extras. |
| `notifications.order_id` | `ON DELETE CASCADE` | Notifications are dependent on the order and are removed only with an exceptional order deletion. |

Templates, flowers, extras, and customers may be edited subject to their validation rules. Catalogue records referenced by templates or orders should normally be deactivated rather than physically deleted. Orders and their snapshots are immutable after placement except for the persisted lifecycle status, its timestamp, and notification read state.

## Snapshot and Integrity Rules

1. On order placement, copy each selected flower and extra name, unit price, quantity, and line total into the arrangement line tables.
2. Copy bouquet/arrangement display choices into `arrangements` and checkout values into `orders`, including the chosen pricing-policy label, subtotal, discount, and final total.
3. Later changes to `flowers.current_unit_price`, `extras.current_unit_price`, names, template composition, customer address, or a customer's contact details must not change order reconstruction, reporting totals, or receipt values.
4. New orders select only active flowers and extras. Existing orders may continue to reference inactive catalogue records.
5. The order total is a checkout snapshot. The later placement service must validate that arrangement totals and order subtotal agree before inserting the order; cross-table sum validation is not portable as a SQLite `CHECK` constraint.
6. No status-history table is introduced in this baseline. The persisted `orders.status` and `status_updated_at` are sufficient for the planned core lifecycle. Notifications provide user-facing reaction records, not a complete audit log.

## Implementation Boundary

This baseline intentionally creates no Java domain classes, SQLite initializer, SQL migration, repository, UI, seed data, or design-pattern implementation. Those belong to later numbered tasks.
