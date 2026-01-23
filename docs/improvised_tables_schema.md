### PCA improvised tables JSON format

This file documents the **display-only** JSON format for improvised weapon tables.

- **Top-level**: an array of `LocationTable`
- **Weighted items**: each `ImprovisedItem.weight` is a relative weight (total does **not** need to equal 100)
- **Duplicates across tables**: allowed by repeating the same item `id` (or full object) in multiple tables
- **No rule enforcement**: values are treated as text for display; the app does not interpret rules from them

The formal JSON Schema is in `docs/improvised_tables_schema.json` (each field includes a `description`).

### Example (minimal)

```json
[
  {
    "id": 12,
    "name": "Tavern",
    "items": [
      {
        "id": "common_bottle",
        "name": "Broken Bottle",
        "description": "A broken glass bottle.",
        "weight": 40,
        "rarity": "COMMON",
        "damage": "1d4",
        "damageType": "piercing",
        "handedness": "ONE_HANDED",
        "notes": "Fragile"
      }
    ]
  }
]
```

