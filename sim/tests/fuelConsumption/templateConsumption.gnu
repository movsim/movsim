
set style line 99 lt 7 lw 3 linecolor rgb "#000000" # beliebige Farben:Schwarz


#################################
# Definition of Contour lines   (line style=line style indicated at splot)
#################################

unset contour              # no contour lines
set contour surface      # Aktiviert Kontourlinien auf 3D-Flaeche
set cntrparam bspline 
unset clabel  # dann lauter gleiche Kontourlinien; 
                     # Farbe und Typ mit "w l ls" beim splot-Kommando
                     # Achtung bug! explizit ("w l lt 3 lw 1" etc) gibt DOS!


unset key
#################################
# Definition of used color-coding
#################################

set pm3d; set pm3d map # gnuplot bug; set pm3d  map alone => DOS error

set palette defined ( 0 "#dd00ff",  13 "#9933ff", 20 "#6666ff", 30 "#00eeee",\
      40 "green", 45 "#aaff00", 60 "yellow", 80 "orange", 100 "red") 

#########################################

set term post eps enhanced color solid "Helvetica" 20

set xlabel "V [km/h]"
set xrange [10:140]

set ylabel "acc [m/s^{2}]"
set yrange [-1:1]

set title "C100 [liters/100 km] for diesel vehicle"
set zrange [0:20]
unset ztics 

set cntrparam levels discrete 2, 4, 6, 8, 10 # freely set lines

set out "consumption100km-Diesel.eps"
print "plotting  consumption100km-Diesel.eps"
splot  "templateConsumption-Diesel.jante_carConsumption.csv" u 1:2:6  w l ls 99

####

set title "C100 [liters/100 km] for gasoline vehicle"

set out "consumption100km-Benzin.eps"
print "plotting  consumption100km-Benzin.eps"
splot  "templateConsumption-Benzin.jante_carConsumption.csv" u 1:2:6  w l ls 99


#########################################
set palette defined ( 0 "#dd00ff",  5 "#9933ff", 12 "#6666ff", 20 "#00eeee",\
      25 "green", 30 "#aaff00", 40 "yellow", 70 "orange", 100 "red") 

set xlabel "Frequency [rpm]"
set xrange [800:4500]

set ylabel "Torque [Nm]"
set yrange [0:230]

set title "Specific consumption(f,pressure) [g/kWh] for diesel vehicle"
set zrange [200:500]
unset ztics 

set cntrparam levels discrete 200,210,220,230,240,250,260,270,280,290,300 # freely set lines

set out "specConsumptionPressure-Diesel.eps"
print "plotting  specConsumptionPressure-Diesel.eps"
splot  "templateConsumption-Diesel.specCons_carConsumption.csv" u 1:4:5  w l ls 99

####

set title "specific consumption(f,pressure) [g/kWh] for gasoline vehicle"
set xrange [900:6000]

set out "specConsumptionPressure-Benzin.eps"
print "plotting  specConsumptionPressure-Benzin.eps"
splot  "templateConsumption-Benzin.specCons_carConsumption.csv" u 1:4:5  w l ls 99


#########################################

set title "specific consumption (f,power) [g/kWh] for diesel vehicle"
set xrange [800:4500]
set ylabel "Power [kW]"
set yrange [0:95]

set out "specConsumptionPower-Diesel.eps"
print "plotting  specConsumptionPower-Diesel.eps"
splot  "templateConsumption-Diesel.specCons_carConsumption.csv" u 1:2:5  w l ls 99

### 

set title "specific consumption (f,power) [g/kWh] for gasoline Benzin vehicle"
set xrange [900:6000]

set out "specConsumptionPower-Benzin.eps"
print "plotting  specConsumptionPower-Benzin.eps"
splot  "templateConsumption-Benzin.specCons_carConsumption.csv" u 1:2:5  w l ls 99
 
