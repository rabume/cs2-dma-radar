import json
import requests

def fetch_offsets():
    try:
        offsets = requests.get('https://raw.githubusercontent.com/a2x/cs2-dumper/main/output/offsets.json').json()
        client = requests.get('https://raw.githubusercontent.com/a2x/cs2-dumper/main/output/client_dll.json').json()
        
        new_offsets = {
            "dwLocalPlayerPawn": hex(offsets["client.dll"]["dwLocalPlayerPawn"]),
            "dwEntityList": hex(offsets["client.dll"]["dwEntityList"]),
            "dwGameTypes": hex(offsets["matchmaking.dll"]["dwGameTypes"]),
            "dwGameTypes_mapName": hex(offsets["matchmaking.dll"]["dwGameTypes_mapName"]),
            "m_iHealth": hex(client["client.dll"]["classes"]["C_BaseEntity"]["fields"]["m_iHealth"]),
            "m_iPawnArmor": hex(client["client.dll"]["classes"]["CCSPlayerController"]["fields"]["m_iPawnArmor"]),
            "m_lifeState": hex(client["client.dll"]["classes"]["C_BaseEntity"]["fields"]["m_lifeState"]),
            "m_angEyeAngles": hex(client["client.dll"]["classes"]["C_CSPlayerPawnBase"]["fields"]["m_angEyeAngles"]),
            "m_iTeamNum": hex(client["client.dll"]["classes"]["C_BaseEntity"]["fields"]["m_iTeamNum"]),
            "m_hPlayerPawn": hex(client["client.dll"]["classes"]["CCSPlayerController"]["fields"]["m_hPlayerPawn"]),
            "m_vOldOrigin": hex(client["client.dll"]["classes"]["C_BasePlayerPawn"]["fields"]["m_vOldOrigin"]),
            "m_iCompTeammateColor": hex(client["client.dll"]["classes"]["CCSPlayerController"]["fields"]["m_iCompTeammateColor"])
        }
        
        return new_offsets
    except Exception as e:
        print(f"Error fetching offsets: {e}")
        return None

def update_offsets_file():
    new_offsets = fetch_offsets()
    if not new_offsets:
        return
    
    offsets_path = "release/offsets.json"
    
    try:
        with open(offsets_path, 'r') as f:
            current_offsets = json.load(f)
        
        if current_offsets != new_offsets:
            with open(offsets_path, 'w') as f:
                json.dump(new_offsets, f, indent=2)
            print("Offsets updated successfully")
        else:
            print("Offsets are already up to date")
    except Exception as e:
        print(f"Error updating offsets file: {e}")

if __name__ == "__main__":
    update_offsets_file()