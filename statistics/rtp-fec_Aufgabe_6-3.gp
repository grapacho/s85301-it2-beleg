#!/usr/local/bin/gnuplot --persist

# Titel und Achsenbeschriftung
set title "Aufgabe 6.3 Abschätzung der zu erwartenden Verlustraten mittels theoretischer Betrachtung"
set xlabel "P_e (Kanalverlustrate)"
set ylabel "P_Rest (Restfehlerwahrscheinlichkeit)"
set xrange [0:1]
set yrange [0:1]
set grid
set key right bottom

# Formel für die Berechnung der Restfehlerwahrscheinlichkeit
PRestfehler(x, k) = 1 - ((1 - x)**(k+1) + (k+1)*x*(1 - x)**k)

# Plot für verschiedene Werte von n
plot PRestfehler(x, 2) title "k = 2" lw 2, \
     PRestfehler(x, 6) title "K = 6" lw 2, \
     PRestfehler(x, 12) title "k = 12" lw 2, \
     PRestfehler(x, 48) title "k = 48" lw 2