#!/usr/local/bin/gnuplot --persist

# Titel und Achsenbeschriftung
set title "Aufgabe 6.4 Abschätzung der Bilddefektwahrscheinlichkeit"
set xlabel "P_e (Kanalverlustrate)"
set ylabel "P_Bilddefekt"
set xrange [0:0.2]    # Fokus auf niedrige Kanalfehlerraten
set yrange [0:1]
set grid
set key right bottom

# Funktion für Restfehlerwahrscheinlichkeit (z. B. für k = 6 als Beispiel)
P_Rest(x) = 1 - ((1 - x)**7 + 7*x*(1 - x)**6)

# Funktion für Bilddefektwahrscheinlichkeit
P_Bilddefekt(x, n) = 1 - (1 - P_Rest(x))**n

# Plot für verschiedene Werte von n (1, 5, 20 RTPs pro Bild)
plot P_Bilddefekt(x, 1) title "n = 1 RTP/Bild" lw 2, \
     P_Bilddefekt(x, 5) title "n = 5 RTPs/Bild" lw 2, \
     P_Bilddefekt(x, 20) title "n = 20 RTPs/Bild" lw 2
