---
name: Core Focus
colors:
  surface: '#f9f9fe'
  surface-dim: '#d9dade'
  surface-bright: '#f9f9fe'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f3f8'
  surface-container: '#ededf2'
  surface-container-high: '#e8e8ed'
  surface-container-highest: '#e2e2e7'
  on-surface: '#1a1c1f'
  on-surface-variant: '#484556'
  inverse-surface: '#2e3034'
  inverse-on-surface: '#f0f0f5'
  outline: '#787588'
  outline-variant: '#c9c4d9'
  surface-tint: '#5b33f5'
  primary: '#4200d6'
  on-primary: '#ffffff'
  primary-container: '#5a31f4'
  on-primary-container: '#d9d1ff'
  inverse-primary: '#c8bfff'
  secondary: '#006e1c'
  on-secondary: '#ffffff'
  secondary-container: '#91f78e'
  on-secondary-container: '#00731e'
  tertiary: '#7d2a00'
  on-tertiary: '#ffffff'
  tertiary-container: '#a43a00'
  on-tertiary-container: '#ffcbb8'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e5deff'
  primary-fixed-dim: '#c8bfff'
  on-primary-fixed: '#1a0064'
  on-primary-fixed-variant: '#4300da'
  secondary-fixed: '#94f990'
  secondary-fixed-dim: '#78dc77'
  on-secondary-fixed: '#002204'
  on-secondary-fixed-variant: '#005313'
  tertiary-fixed: '#ffdbce'
  tertiary-fixed-dim: '#ffb599'
  on-tertiary-fixed: '#370e00'
  on-tertiary-fixed-variant: '#7f2b00'
  background: '#f9f9fe'
  on-background: '#1a1c1f'
  surface-variant: '#e2e2e7'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 34px
    fontWeight: '700'
    lineHeight: 42px
    letterSpacing: -0.5px
  display-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 34px
  headline-md:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
  section-header:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.5px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 26px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  grid_unit: 4px
  base_padding: 24px
  item_gap: 12px
  touch_target_min: 48px
  safe_margin: 16px
---

## Brand & Style
The design system is built for high-stakes certification prep, prioritizing mental clarity, focus, and ergonomic ease. It adopts a **Corporate Modern** aesthetic heavily influenced by Samsung One UI principles, emphasizing reachability and information hierarchy. 

The personality is professional yet encouraging. By utilizing vast amounts of whitespace and a "content-first" approach, the UI recedes to let the study material lead. The experience should feel native to high-end Android devices, utilizing familiar interaction patterns like oversized headers and bottom-weighted controls to reduce cognitive load during intense study sessions.

## Colors
The palette is centered around a vibrant violet primary accent, symbolizing wisdom and focus. 

- **Primary:** Used for active states, primary buttons, and progress indicators.
- **Surface Strategy:** In light mode, surfaces use pure white (`#FFFFFF`) for the main content area and a light gray (`#F2F2F7`) for grouped background containers. In dark mode, the system shifts to a deep charcoal (`#121212`) to reduce eye strain.
- **Semantic:** Green and Red are reserved strictly for quiz feedback—marking correct and incorrect answers. They should be used with sufficient contrast against background surfaces.

## Typography
The system uses **Inter** for its exceptional legibility and neutral, geometric tone. 

- **One UI Scaling:** Large titles (`display-lg`) are positioned in the top-third of the screen to allow for easy one-handed reach. As the user scrolls, these titles transition to a standard centered header in the top app bar.
- **Hierarchy:** Section headers use uppercase styling with increased tracking to clearly delineate between different question categories or settings groups.
- **Readability:** Body text for exam questions (`body-lg`) utilizes a generous line height to prevent fatigue during long reading passages.

## Layout & Spacing
This design system follows a strict **4px grid** for all micro-measurements and a **fluid grid** for structural layouts.

- **One UI Layout:** Screens are divided into a "Viewing Area" (top 30-40% for titles) and an "Interaction Area" (bottom 60-70% for quiz options and buttons).
- **Safe Zones:** A standard 24px horizontal margin is applied to all main containers.
- **Ergonomics:** All interactive elements maintain a minimum 48px height to ensure high hit success on mobile devices. Navigation and primary actions are anchored to the bottom of the screen.

## Elevation & Depth
Depth is communicated through **Tonal Layers** rather than heavy shadows. 

- **Level 0:** The main background.
- **Level 1:** Cards and input fields use a subtle 1px border or a slightly different tonal shade (e.g., White on Gray background) to appear "placed" on the surface.
- **Modals:** Use a dimming backdrop and a high-radius top-only corner treatment, sliding up from the bottom to maintain the reachability focus.
- **Feedback:** Success/Failure states use subtle background tints of the semantic colors rather than high-elevation shadows.

## Shapes
The design system employs **Large Rounded Corners** to create a friendly, accessible feel. 

- **Containers:** Main content cards and bottom sheets use a 24px radius (`rounded-xl`).
- **Interactive Elements:** Buttons and selection chips use a 16px radius (`rounded-lg`).
- **Visual Style:** Avoid sharp 90-degree angles entirely to maintain the soft, modern Samsung-inspired aesthetic.

## Components
- **Buttons:** Primary actions are full-width, bottom-anchored, with 16px corner radii. Use the Primary 500 color for active states.
- **Quiz Cards:** Options are presented as large, tappable tiles with 48px minimum height. Upon selection, the border thickens and changes to the Primary color.
- **Progress Bars:** Thin, rounded tracks used at the very top of the screen or just below the collapsing header to indicate quiz completion.
- **Chips:** Used for "Domain" tags (e.g., Process, People, Business Environment). These are low-contrast gray capsules with `label-sm` typography.
- **Input Fields:** Search or text inputs use a light gray fill with no border, becoming outlined only when focused.
- **Bottom Navigation:** Icons are clean, geometric line art, with labels appearing only for the active tab to reduce visual clutter.