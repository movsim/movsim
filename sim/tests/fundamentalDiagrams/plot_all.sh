#!bin/sh

models="ACC IDM IDMM GIPPS OVM_FVDM NSM KKW"

for ldm in ${models}
do
 echo "gnuplot -e \"ldm='$ldm'\" fund_diagrams.gpl"
 gnuplot -e "ldm='$ldm'" fund_diagrams.gpl
 gv fund_diagrams.fund_${ldm}.eps &
done;

