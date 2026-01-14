import serial
import mysql.connector
import time

# --- CONFIGURATIE ---
# Zoek de juiste poort (bijv. COM3 op Windows of /dev/ttyACM0 op Mac/Linux)
SERIAL_PORT = 'COM3' 
BAUD_RATE = 9600

# Database instellingen (moet matchen met wat je team heeft gemaakt)
db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'irrigo_db'
}

def start_bridge():
    try:
        # Verbinding met Micro:bit
        ser = serial.Serial(SERIAL_PORT, BAUD_RATE, timeout=1)
        print(f"Verbonden met Micro:bit op {SERIAL_PORT}")

        # Verbinding met Database
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor()
        print("Verbonden met de database")

        while True:
            if ser.in_waiting > 0:
                # Lees de regel van de Micro:bit
                line = ser.readline().decode('utf-8').strip()
                print(f"Ontvangen: {line}")

                # Check of de regel de data bevat (bijv: "Moisture: 1023 | Float: 0 | Pump: 0")
                if "Moisture:" in line:
                    try:
                        # Haal de getallen uit de tekst
                        parts = line.split('|')
                        moisture = int(parts[0].split(':')[1].strip())
                        float_val = int(parts[1].split(':')[1].strip())
                        pump = int(parts[2].split(':')[1].strip())

                        # Voer de INSERT uit
                        sql = "INSERT INTO metingen (vochtigheid, waterniveau, pomp_status) VALUES (%s, %s, %s)"
                        cursor.execute(sql, (moisture, float_val, pump))
                        conn.commit()
                        print("Data succesvol opgeslagen in DB")
                    except Exception as e:
                        print(f"Fout bij verwerken data: {e}")

            time.sleep(0.1)

    except KeyboardInterrupt:
        print("Bridge gestopt door gebruiker")
    finally:
        if 'conn' in locals() and conn.is_connected():
            conn.close()

if __name__ == "__main__":
    start_bridge()