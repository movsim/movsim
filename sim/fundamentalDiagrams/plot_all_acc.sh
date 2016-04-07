#!bin/sh

models="ACC IDM IDMM GIPPS OVM_FVDM KRAUSS NEWELL NSM KKW CCS"
#models="IDM"

gnuplot="acc_function.gpl"

project="fund_diagrams"

for ldm in ${models}
do
 echo "gnuplot -e \"ldm='$ldm'\" -e \"project='$project'\"" $gnuplot
 gnuplot -e "ldm='$ldm'" -e "project='$project'" $gnuplot
done;
