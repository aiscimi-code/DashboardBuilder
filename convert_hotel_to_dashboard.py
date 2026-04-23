import json

# Input file (the hotel JSON that J sent)
input_path = '/root/.openclaw/media/inbound/dc170b71-a99a-4fc2-8b5d-ba0f9738a806---f68b8ba8-9fab-43bd-bdd6-5c77b1568199.txt'
output_path = '/root/.openclaw/workspace/DashboardBuilder/hotel_dashboard.json'

with open(input_path, 'r') as f:
    hotel = json.load(f)

rooms = hotel['tab_a_rooms']['rooms']

def make_box(box_id, box_type, text, pos_y, actions=None):
    if box_type == "BUTTON":
        cfg = {"text": text}
    else:  # TEXT
        cfg = {"value": text, "editable": False}
    style_bg = "#4CAF50" if box_type == "BUTTON" else "#FFFFFF"
    return {
        "id": box_id,
        "type": box_type,
        "label": "",
        "position": {"x": 0, "y": pos_y},
        "size": {"w": 10, "h": 1},
        "locked": False,
        "floating": False,
        "style": {"backgroundColor": style_bg},
        "config": cfg,
        "actions": actions or []
    }

def switch_action(target_tab_id):
    return [{
        "event": "ON_CLICK",
        "type": "SWITCH_TAB",
        "targetBoxId": "",
        "dataSource": {"value": target_tab_id}
    }]

# Index tab (A)
index_boxes = []
pos_y = 0
for room in rooms:
    btn_id = f"roomBtn_{room['room_number']}"
    index_boxes.append(make_box(btn_id, "BUTTON", f"Room {room['room_number']}", pos_y, switch_action(room['room_number'])))
    pos_y += 1
index_tab = {
    "id": "A",
    "name": "Index",
    "backgroundColor": "#F5F5F5",
    "boxes": index_boxes
}

# Room tabs
room_tabs = []
for room in rooms:
    boxes = []
    row = 0
    # Header
    boxes.append(make_box(f"header_{room['room_number']}", "TEXT", f"Room {room['room_number']}", row))
    row += 1
    # Helper to add detail rows (no nonlocal)
    def add_detail(label, value, current_row):
        if value is None:
            return current_row
        boxes.append(make_box(f"{label.replace(' ', '_')}_{room['room_number']}", "TEXT", f"{label}: {value}", current_row))
        return current_row + 1
    # Details
    row = add_detail("Status", room.get('status'), row)
    row = add_detail("Type", room.get('room_type'), row)
    row = add_detail("Priority", room.get('priority'), row)
    row = add_detail("Estimated Minutes", room.get('estimated_minutes'), row)
    if room.get('notes'):
        row = add_detail("Notes", room.get('notes'), row)
    # Back button
    boxes.append(make_box(f"back_{room['room_number']}", "BUTTON", "Back to Index", row, switch_action('A')))
    room_tabs.append({
        "id": room['room_number'],
        "name": f"Room {room['room_number']}",
        "backgroundColor": "#FFFFFF",
        "boxes": boxes
    })

app_state = {
    "appVersion": "1.0",
    "tabs": [index_tab] + room_tabs
}

with open(output_path, 'w') as f:
    json.dump(app_state, f, indent=2)

print(f"Dashboard JSON written to {output_path}")
