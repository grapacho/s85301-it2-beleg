#!/usr/local/bin/gnuplot --persist

# Kanalverlustrate
set title "Wahrscheinlichkeit fuer Bildverlust in Abhaengigkeit von P_e"
set xlabel "P_e (Kanalverlustrate)"
set ylabel "P_Bildverlust"
set xrange [0:1]
set yrange [0:1]
set grid
set key right bottom

# Funktion für Bildverlust
Pbildverlust(x, n) = 1 - (1 - x)**n

# Plot für verschiedene Werte von n
plot Pbildverlust(x, 1) title "n = 1" lw 2, \
     Pbildverlust(x, 2) title "n = 2" lw 2, \
     Pbildverlust(x, 5) title "n = 5" lw 2, \
     Pbildverlust(x, 10) title "n = 10" lw 2, \
     Pbildverlust(x, 20) title "n = 20" lw 2

pause -1
