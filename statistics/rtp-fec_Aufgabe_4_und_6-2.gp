#!/usr/local/bin/gnuplot --persist

# Titel und Achsenbeschriftung
set title "Aufgabe 4  Auswertung der Fehlerstatistiken ohne Fehlerkorrektur + 6.2 Bestimmung der Verlustraten mittels Simulation"
set xlabel "P_e (Kanalverlustrate)"
set ylabel "Wahrscheinlichkeit"
set xrange [0:1]
set yrange [0:1]
set grid
set key right bottom

# Funktion für Bildverlust
Pbildverlust(x, n) = 1 - (1 - x)**n

# Datenfile für gemessene Werte
datafile = "result_aufgabe_6-2.txt"

# Plot für Bildverlustwahrscheinlichkeiten und gemessene Restfehlerwahrscheinlichkeiten
plot Pbildverlust(x, 1) title "n = 1 (Bildverlust)" lw 2, \
     Pbildverlust(x, 2) title "n = 2 (Bildverlust)" lw 2, \
     Pbildverlust(x, 5) title "n = 5 (Bildverlust)" lw 2, \
     Pbildverlust(x, 10) title "n = 10 (Bildverlust)" lw 2, \
     Pbildverlust(x, 20) title "n = 20 (Bildverlust)" lw 2, \
     datafile using 1:2 title "k = 2 (Restfehler, gemessen)" with linespoints lw 2 pt 7, \
     datafile using 1:3 title "k = 6 (Restfehler, gemessen)" with linespoints lw 2 pt 7, \
     datafile using 1:4 title "k = 12 (Restfehler, gemessen)" with linespoints lw 2 pt 7, \
     datafile using 1:5 title "k = 48 (Restfehler, gemessen)" with linespoints lw 2 pt 7
