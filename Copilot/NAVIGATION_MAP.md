# 🌸 Navigation Map - Allergen Information System

## Overview
All HTML pages have been standardized with consistent layouts and seamless navigation between pages.

## Route Mappings

### Home Routes
- **`/`** → Redirects to `index.html` (Home page)
- **`/home`** → Redirects to `index.html` (Home page)

### Food Routes
| Route | Method | Purpose | Navigation Buttons |
|-------|--------|---------|-------------------|
| `/allFood` | GET | List all foods | ➕ New Food, 🏠 Home |
| `/newFood` | GET | New food form | 🔙 Back to Foods, 🏠 Home |
| `/newFood` | POST | Create new food | → Redirects to `/allFood` |
| `/getFood?foodId={id}` | GET | View food details | 🔙 Back to Foods, 🏠 Home |
| `/modifyFood?foodId={id}` | GET | Edit food form | 🔙 Back to Foods, 🏠 Home |
| `/modifyFood` | PUT | Update food | → Redirects to `/allFood` |
| `/deleteFood?foodId={id}` | GET | Delete confirmation | ❌ Cancel, ✅ Confirm Delete |
| `/deleteFood` | DELETE | Delete food | → Redirects to `/allFood` or Error page |

### Ingredient Routes
| Route | Method | Purpose | Navigation Buttons |
|-------|--------|---------|-------------------|
| `/allIngredients` | GET | List all ingredients | ➕ New Ingredient, 🏠 Home |
| `/newIngredient` | GET | New ingredient form | 🔙 Back to Ingredients, 🏠 Home |
| `/newIngredient` | POST | Create new ingredient | → Redirects to `/allIngredients` |
| `/getIngredient?ingredientId={id}` | GET | View ingredient details | 🔙 Back to Ingredients, 🏠 Home |
| `/modifyIngredient?ingredientId={id}` | GET | Edit ingredient form | 🔙 Back to Ingredients, 🏠 Home |
| `/modifyIngredient` | PUT | Update ingredient | → Redirects to `/allIngredients` |
| `/deleteIngredient?ingredientId={id}` | GET | Delete confirmation | ❌ Cancel, ✅ Confirm Delete |
| `/deleteIngredient` | DELETE | Delete ingredient | → Redirects to `/allIngredients` or Error page |

### Recipe Routes
| Route | Method | Purpose | Navigation Buttons |
|-------|--------|---------|-------------------|
| `/allRecipes` | GET | List all recipes | ➕ New Recipe, 🏠 Home |
| `/newRecipe` | GET | New recipe form | 🔙 Back to Recipes, 🏠 Home |
| `/newRecipe` | POST | Create new recipe | → Redirects to `/allRecipes` |
| `/getRecipe?index={id}` | GET | View recipe details | 🔙 Back to Recipes, 🏠 Home |
| `/modifyRecipe?index={id}` | GET | Edit recipe form | 🔙 Back to Recipes, 🏠 Home |
| `/modifyRecipe` | PUT | Update recipe | → Redirects to `/allRecipes` |
| `/deleteRecipe?index={id}` | GET | Delete confirmation | ❌ Cancel, ✅ Confirm Delete |
| `/deleteRecipe` | DELETE | Delete recipe | → Redirects to `/allRecipes` |

### Error Pages
| Page | Triggered By | Navigation Buttons |
|------|-------------|-------------------|
| `DeleteForeignKeyF.html` | Deleting food with recipes | 🔙 Back to Foods, 🏠 Home |
| `DeleteForeignKeyI.html` | Deleting ingredient in recipes | 🔙 Back to Ingredients, 🏠 Home |

---

## Navigation Flow Diagram

```
                           🏠 HOME (index.html)
                                    |
                    +---------------+---------------+
                    |               |               |
              🍱 FOODS        🥬 INGREDIENTS    📝 RECIPES
                    |               |               |
            +-------+-------+       +-------+       +-------+
            |       |       |       |       |       |       |
          List    New    Edit    List    New    List    New
            |       |       |       |       |       |       |
         Detail  Create  Update  Detail Create  Detail  Create
            |                       |               |
         Delete                  Delete         Delete
```

---

## Page Layout Consistency

All pages follow this consistent structure:

### 1. **Header Section**
```html
<head>
    <meta charset="UTF-8">
    <title>[Page Title] - Allergen System 🌸</title>
    <link rel="stylesheet" type="text/css" href="/css/mainmenu.css">
    <link href="https://fonts.googleapis.com/css2?family=Quicksand..." rel="stylesheet">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
```

### 2. **Container Wrapper**
```html
<div class="container">
    <!-- All content wrapped in container -->
</div>
```

### 3. **Page Header**
```html
<div class="page-header">
    <h1>🌸 [Page Title]</h1>
</div>
```

### 4. **Content Area**
- Tables for listing items
- `<div class="form-wrapper">` for forms
- Styled tables and forms with cherry blossom theme

### 5. **Navigation Links**
```html
<div class="nav-links">
    <form action="#" th:action="@{...}" method="get">
        <input type="submit" value="[Button]" />
    </form>
</div>
```

---

## Form Method Handling

### GET Forms
Used for navigation:
```html
<form action="#" th:action="@{/route}" method="get">
    <input type="submit" value="Button Text" />
</form>
```

### POST Forms
Used for creating new items:
```html
<form action="#" th:action="@{/route}" method="post">
    <!-- form fields -->
    <input type="submit" value="Submit" />
</form>
```

### PUT Forms (Update)
POST with hidden method override:
```html
<form action="#" th:action="@{/route}" method="post">
    <input type="hidden" name="_method" value="put" />
    <!-- form fields -->
    <input type="submit" value="Save Changes" />
</form>
```

### DELETE Forms
POST with hidden method override:
```html
<form th:action="@{/route}" method="post">
    <input type="hidden" name="_method" value="delete" />
    <input type="hidden" name="id" th:value="${item.id}" />
    <input type="submit" value="Confirm Delete" />
</form>
```

---

## Configuration

### Spring Boot Configuration
**File:** `application.properties`
```properties
spring.mvc.hiddenmethod.filter.enabled=true
```
This enables HTTP method override filter for PUT and DELETE operations.

---

## Navigation Button Standards

### Primary Buttons
- **🏠 Home** - Returns to main index page (on all pages)
- **🔙 Back to [Section]** - Returns to list view (on detail/form pages)

### Action Buttons
- **➕ New [Item]** - Create new item (on list pages)
- **✏️ Edit** - Opens edit form (on list pages)
- **🗑️ Delete** - Opens delete confirmation (on list pages)
- **✅ Submit/Create/Save Changes** - Confirms action (on forms)
- **❌ Cancel** - Cancels delete operation (on delete pages)
- **✅ Confirm Delete** - Confirms deletion (on delete pages)

---

## Seamless Navigation Features

1. **Consistent Button Placement**
   - Navigation buttons always at bottom in `.nav-links` div
   - Multiple buttons displayed horizontally

2. **Clear Visual Hierarchy**
   - Page headers with emojis
   - Form wrappers for visual separation
   - Styled tables with hover effects

3. **Back Navigation**
   - Every page can return to its parent list
   - Every page can return to home

4. **Error Handling**
   - Foreign key errors redirect to error pages
   - Error pages provide navigation back to list or home

5. **Form Validation**
   - Required fields marked
   - Dropdowns with default "Choose..." options

---

## Testing Navigation

### Complete User Flow Example: Adding a Recipe

1. Start at **Home** (`/` or `/home`)
2. Click "📝 View All Recipes" → **All Recipes** (`/allRecipes`)
3. Click "➕ New Recipe" → **New Recipe Form** (`/newRecipe`)
4. Select food and ingredient, click "✅ Create Recipe" → **Back to All Recipes** (`/allRecipes`)
5. Click "🏠 Home" → **Home** (`/`)

### Complete User Flow Example: Deleting Food (with error)

1. Start at **All Foods** (`/allFood`)
2. Click "Delete" on a food with recipes → **Delete Confirmation** (`/deleteFood?foodId=X`)
3. Click "✅ Confirm Delete" → **Error Page** (`DeleteForeignKeyF.html`)
4. Click "🔙 Back to Foods" → **All Foods** (`/allFood`)
5. Go to **All Recipes**, delete related recipes
6. Return to **All Foods**, delete the food successfully

---

## Files Updated for Consistency

### HTML Templates (18 files)
✅ Food: `allFood.html`, `newFood.html`, `modifyFood.html`, `deleteFood.html`, `getFood.html`
✅ Ingredient: `allIngredients.html`, `newIngredient.html`, `modifyIngredient.html`, `deleteIngredient.html`, `getIngredient.html`
✅ Recipe: `allRecipes.html`, `newRecipe.html`, `modifyRecipe.html`, `deleteRecipe.html`, `getRecipe.html`
✅ Error: `DeleteForeignKeyF.html`, `DeleteForeignKeyI.html`
✅ Index: `index.html`

### Controller
✅ `MainController.java` - Added root path mapping for home route

### Styling
✅ `mainmenu.css` - Cherry blossom theme with consistent button and form styling

---

## Troubleshooting

### Issue: PUT/DELETE not working
**Solution:** Verify `spring.mvc.hiddenmethod.filter.enabled=true` in `application.properties`

### Issue: Home button not working
**Solution:** Both `/` and `/home` routes are mapped to redirect to `index.html`

### Issue: Navigation between sections
**Solution:** All list pages have Home button, all detail/form pages have Back + Home buttons

---

**Last Updated:** June 19, 2026
**Theme:** Cherry Blossom 🌸
**Status:** All navigation tested and verified ✅

