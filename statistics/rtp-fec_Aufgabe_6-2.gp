#!/usr/local/bin/gnuplot --persist

# Titel und Achsenbeschriftung
set title "Aufgabe 6.2 Bestimmung der Verlustraten mittels Simulation"
set xlabel "P_e (Kanalfehlerrate)"
set ylabel "P_Rest (Restfehlerwahrscheinlichkeit)"
set xrange [0:0.2]  # Wertebereich der Kanalfehlerrate
set yrange [0:0.25] # Wertebereich der Restfehlerwahrscheinlichkeit
set grid
set key left top

# Datei mit Messwerten
datafile = "result_aufgabe_6-2.txt"

# Daten plotten
plot datafile using 1:2 title "k = 2" with linespoints lw 2 pt 7, \
     datafile using 1:3 title "k = 6" with linespoints lw 2 pt 7, \
     datafile using 1:4 title "k = 12" with linespoints lw 2 pt 7, \
     datafile using 1:5 title "k = 48" with linespoints lw 2 pt 7
