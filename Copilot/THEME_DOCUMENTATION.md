# 🌸 Cherry Blossom Theme Documentation

## Overview
The Allergen Information System has been beautified with a Cherry Blossom theme, featuring soft pink gradients, elegant typography, and modern UI elements.

## Color Palette

### Primary Colors
- **Soft Pink**: `#FFB7C5` - Main cherry blossom color
- **Light Pink**: `#FFC8DD` - Secondary accent
- **Deep Pink**: `#E889A7` - Headers and emphasis
- **Vibrant Pink**: `#FF9EBB` - Button hover states

### Background Colors
- **Cream Background**: `#FFF5F7` - Very light pink/cream
- **Light Pink BG**: `#FFE5EC` - Form backgrounds and highlights

### Text Colors
- **Dark Text**: `#5D4954` - Primary text color (muted purple-brown)
- **Dusty Rose**: `#D6A1B0` - Secondary text and borders

### Accent Colors
- **Soft Mint**: `#B8D4C8` - "Home" button accent (representing leaves)
- **Medium Mint**: `#A3C9BC` - Mint hover state

## Design Features

### 1. Gradient Backgrounds
- Body has a beautiful diagonal gradient from cream to light pink
- Buttons feature gradient overlays for depth
- Table headers use soft pink gradients

### 2. Modern UI Elements
- **Rounded corners** (15-25px border radius) for a softer look
- **Box shadows** with pink tints for depth
- **Smooth transitions** (0.3s) on hover states
- **Backdrop blur** effects on containers

### 3. Typography
- **Font**: Quicksand (Google Fonts) - rounded, friendly sans-serif
- **Headers**: Large, elegant with subtle text shadows
- **Emojis**: Used throughout for visual interest and clarity
  - 🍱 Food items
  - 🥬 Ingredients
  - 📝 Recipes
  - ✏️ Edit actions
  - 🗑️ Delete actions
  - ➕ Add/Create actions
  - 🔙 Navigation back
  - 🏠 Home
  - ⚠️ Errors

### 4. Interactive Elements
- **Buttons**: Rounded, gradient-filled with hover lift effects
- **Tables**: Clean, with hover row highlighting
- **Forms**: Well-spaced with styled inputs and clear labels
- **Links**: Subtle hover effects with background highlights

### 5. Responsive Design
- Mobile-friendly with responsive breakpoints
- Flexible layouts that adapt to screen size
- Readable on all devices

## File Structure

### CSS
- `src/main/resources/static/css/mainmenu.css` - Main stylesheet with all cherry blossom styling

### HTML Templates

#### Main Pages
- `src/main/resources/static/index.html` - Home page

#### Food Pages
- `templates/Food/allFood.html` - Food listing
- `templates/Food/newFood.html` - Create food
- `templates/Food/modifyFood.html` - Edit food
- `templates/Food/deleteFood.html` - Delete confirmation
- `templates/Food/getFood.html` - View food details

#### Ingredient Pages
- `templates/Ingredient/allIngredients.html` - Ingredient listing
- `templates/Ingredient/newIngredient.html` - Create ingredient
- `templates/Ingredient/modifyIngredient.html` - Edit ingredient
- `templates/Ingredient/deleteIngredient.html` - Delete confirmation
- `templates/Ingredient/getIngredient.html` - View ingredient details

#### Recipe Pages
- `templates/Recipe/allRecipes.html` - Recipe listing
- `templates/Recipe/newRecipe.html` - Create recipe
- `templates/Recipe/modifyRecipe.html` - Edit recipe
- `templates/Recipe/deleteRecipe.html` - Delete confirmation
- `templates/Recipe/getRecipe.html` - View recipe details

#### Error Pages
- `templates/Error/DeleteForeignKeyF.html` - Food deletion error
- `templates/Error/DeleteForeignKeyI.html` - Ingredient deletion error

## Browser Compatibility
- Modern browsers (Chrome, Firefox, Safari, Edge)
- CSS3 features: gradients, transforms, transitions, backdrop-filter
- Fallbacks provided for older browsers

## Customization

### Changing Colors
To customize the color scheme, update these CSS variables in `mainmenu.css`:
- Background gradients in the `body` selector
- Button gradients in `input[type="submit"]` and `.button`
- Header colors in `h1`, `h2`
- Table header background in `thead`

### Adjusting Spacing
- Container padding: `.container { padding: 40px; }`
- Form spacing: `.form-wrapper` margins
- Button margins: `input[type="submit"]` margin values

## Maintenance
When adding new pages:
1. Copy the header section from any existing template
2. Wrap content in `<div class="container">`
3. Use `<div class="page-header">` for titles
4. Wrap forms in `<div class="form-wrapper">`
5. Use `<div class="nav-links">` for navigation buttons

## Performance
- CSS is loaded once and cached
- Google Fonts loaded with optimal settings
- Minimal external dependencies
- Optimized for fast rendering

---
Created: June 2026
Theme: Cherry Blossom 🌸

