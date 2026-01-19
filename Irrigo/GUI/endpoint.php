<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *'); // Zorgt dat je website de data mag ophalen

$host = "localhost";
$user = "root";
$pass = "user";
$db   = "irrigo_db";

$conn = new mysqli($host, $user, $pass, $db);

// Haal de allerlaatste meting op uit de tabel 'metingen'
$sql = "SELECT vochtigheid, waterniveau, pomp_status FROM metingen ORDER BY tijdstip DESC LIMIT 1";
$result = $conn->query($sql);

if ($row = $result->fetch_assoc()) {
    // Stuur de data terug als JSON
    echo json_encode($row);
} else {
    echo json_encode(["error" => "Geen data gevonden"]);
}

$conn->close();
?>