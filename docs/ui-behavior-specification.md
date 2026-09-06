# EverBloom UI Behavior Specification

## Scope and Shared Behavior

EverBloom uses the existing application shell with a persistent sidebar and a replaceable main content area. The six major areas are Dashboard, New Order, Orders, Catalogue, Customers, and Reports. Order Details is a detail view within Orders rather than a separate sidebar destination.

The active sidebar item is always visually selected. Page changes update only the main content area. Loading work shows a progress indicator without freezing the window. Recoverable failures show a clear message and Retry action; validation messages appear beside the relevant control. Destructive or draft-discarding actions require confirmation. Empty states explain why no content is shown and provide the most useful next action.

If the user attempts to leave New Order with unsaved changes, offer Continue Editing or Discard Draft. Successful creation or editing shows brief confirmation and refreshes affected views.

## Dashboard

### Important controls and information

- Summary cards for Today's Orders, Active Orders, Ready Orders, and Total Customers.
- Recent Orders table with order number, customer, occasion, status, and amount.
- New Order primary action.
- Refresh action for reloading dashboard information.

### User actions and navigation

- Selecting a summary card opens Orders with the matching filter when applicable; Total Customers opens Customers.
- Selecting a recent order opens its Order Details view.
- New Order opens the New Order workflow.
- Dashboard is the default page after application startup.

### Validation, error, and empty states

- No input validation is required on this screen.
- If dashboard data cannot load, keep the shell usable and show an error panel with Retry.
- With no orders, summary counts show zero and Recent Orders shows “No orders yet” with a New Order action.

## New Order / Bouquet Builder

Use a visible step indicator and keep an order summary with the current subtotal available throughout the workflow. Back and Continue move between steps without losing the draft. Place Order is available only on the final review step.

### Core workflow

#### 1. Choose customer

Controls include customer search, a selected-customer summary, and an Add Customer action that opens the Customers area. One customer is required before continuing. A missing selection shows an inline message; a customer-load failure shows Retry without clearing the draft.

#### 2. Choose starting point

Controls include Start from Scratch and a searchable/filterable template gallery with template name, occasion, composition summary, and starting price. Starting from scratch creates an empty draft. Choosing a template creates an independent editable copy so later changes never alter the saved template or another order draft. If no templates exist, Start from Scratch remains available. If a template contains unavailable catalogue items, show the affected items and require removal or replacement before continuing.

#### 3. Build bouquet

Controls include flower search/filtering, Add Flower, selected-flower rows, quantity controls, wrapping selection, occasion, and optional message text. The UI builds the bouquet incrementally as selections change. At least one flower is required; quantities must be positive whole numbers and must not exceed available stock. Unavailable flowers cannot be newly selected. Invalid rows remain visible with specific correction messages.

#### 4. Add extras

Controls show available extras with price, quantity where meaningful, and Add/Remove actions. Chosen extras appear in the live summary and may be combined independently. No extra is required. Inactive extras cannot be added, quantities must be positive, and a load failure must not remove already selected extras.

#### 5. Pricing and fulfilment

Controls include a pricing-policy selector, subtotal/discount/total breakdown, pickup or delivery choice, required date/time, delivery address when needed, and order notes if retained in the final schema. Changing the policy recalculates the displayed breakdown through the selected pricing behavior. The screen must explain an ineligible policy instead of silently changing it.

Required date/time cannot be in the past. Delivery requires a nonblank address; pickup hides and clears the delivery-address input after confirmation if it already contains text. Prices cannot be negative, and the final total must agree with the displayed breakdown.

#### 6. Review and place order

The review shows customer, bouquet composition, extras, pricing policy, price breakdown, fulfilment details, and required date/time. Edit links return to the corresponding step. Place Order performs final validation once, prevents duplicate submission while saving, and preserves the draft if saving fails. Success opens the new Order Details view and clears the draft.

### Pattern-ready interaction boundaries

| Workflow concern | UI behavior supported |
|---|---|
| Builder | Flower, quantity, wrapping, occasion, and message choices accumulate step by step into one valid bouquet draft. |
| Prototype | Selecting a template starts from an independent copy that can be edited without changing the template. |
| Decorator | Extras are added or removed independently and appear as separate contributions to description and price. |
| Strategy | The pricing-policy selector changes the discount calculation while the checkout flow remains the same. |

## Orders and Order Details

### Orders controls and actions

- Search by order number or customer.
- Filters for status, occasion, fulfilment type, and date range, with Clear Filters.
- Results table showing order number, customer, required date/time, occasion, status, and total.
- New Order action and row action to open details.

Search may update after a short pause or explicit Search action. Filters combine and remain visible while viewing results. No orders shows a New Order empty state; no matches shows Clear Filters. Load failures show Retry and do not display stale results as current.

### Order Details controls and actions

- Read-only customer and order information.
- Bouquet flower, wrapping, message, and extra snapshots.
- Pricing-policy and subtotal/discount/total snapshots.
- Fulfilment details and current status.
- Lifecycle area showing Ordered, Preparing, Arranging, Ready, and Delivered.
- Only the valid next status action is enabled, with confirmation before changing status.

Invalid or outdated transitions show an explanatory error and refresh the current order. A status-save failure keeps the previous visible status. Historical names and prices are displayed from order snapshots, even if Catalogue values later change. Back returns to the Orders list with its previous search/filter state; customer information can link to the relevant Customer detail.

## Catalogue

### Important controls and actions

- Tabs or segmented controls for Flowers, Extras, and Bouquet Templates.
- Search and Active/Inactive filter for each catalogue type.
- Add, Edit, and Deactivate/Reactivate actions.
- Flower editor: name, color, unit price, stock quantity, and active state.
- Extra editor: name, description, unit price, and active state.
- Template editor: name, occasion, description, wrapping, optional default message, and flower composition with quantities.

### Validation, error, and empty states

- Names are required and unique without case sensitivity.
- Prices are nonnegative monetary values; stock and composition quantities are whole numbers, with template quantities greater than zero.
- A template requires at least one flower and cannot contain the same flower twice.
- Validation stays in the editor and preserves entered values.
- Records referenced by existing data are deactivated rather than silently deleted; confirmation explains the effect on future selection.
- Empty catalogue sections offer the matching Add action. No search matches offers Clear Search. Load/save failures show a specific message and Retry where appropriate.

Selecting a template may offer Use in New Order, which opens New Order with an independent template copy. Catalogue remains one major screen rather than separate screens for each item type.

## Customers

### Important controls and actions

- Search by name, phone, or email.
- Customer list with Add Customer and row selection.
- Customer editor for full name, phone, optional email, and optional default address.
- Customer detail panel with contact information and order history.
- Edit Customer and Create Order actions.

### Validation, error, and empty states

- Full name and phone are required.
- Phone input is normalized before duplicate checking and must not duplicate another customer.
- A supplied email must have a valid basic format.
- Validation errors remain beside the fields and do not clear input.
- Customers with order history are retained; the UI must not offer destructive deletion that would break attribution.
- With no customers, show Add Customer. With no search matches, show Clear Search. With no history, show “No orders for this customer yet” and Create Order.

Create Order opens New Order with the customer preselected. Selecting an order-history row opens Order Details. Returning from those views restores the customer selection when practical.

## Reports

### Important controls and actions

- Date-range controls with Apply and Reset.
- Report selector for Sales Summary, Popular Flowers, Popular Extras, and Popular Occasions.
- Summary values such as order count, gross sales, discounts, and net sales where relevant.
- Ranked table or simple chart for popularity reports, with exact values also shown in text/table form.

### Validation, error, and empty states

- Both dates are required and the start date cannot be after the end date.
- Invalid ranges show inline messages and do not run a report.
- A query failure shows an error with Retry while retaining the selected report and dates.
- A valid period with no data shows “No order data for this period” rather than zero-filled misleading rankings.

Reports are read-only. Selecting an order count or ranked item may apply a corresponding filter in Orders only when that navigation is unambiguous; otherwise the user remains on Reports.

## Optional Future Enhancements

- Undo/redo may later appear inside the bouquet-editing step if several real reversible edits make it useful. The core New Order workflow does not depend on it, and no Command design is assumed here.
- Event packages may later extend New Order. If packages remain a flat list of arrangements, ordinary list behavior is sufficient. Nested package controls and Composite are considered only if real recursive arrangements become a confirmed requirement.
