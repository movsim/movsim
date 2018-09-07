# Viewer with javafx

## Start Main class with project parameter e.g.:

-f sim/bookScenarioStartStop/startStop_ACC

## Experimental viewer with javafx instead of swing/awt.

Lacking some features compared with the swing version.

Performance seems to be slower than Swing based canvas?!? Check vasa scenario. So maybe the effort was not worth it.

On the plus side: less ui code and better structured. Everything resides in the this package.

## TODO

### Background
- use buffer image if there are no changes as in the swing based viewer
- RoadMappingPolyLine remove usage of FXGraphics2D facade
- RoadMappingBezier remove usage of FXGraphics2D facade
- RoadMappingPolyBezier remove usage of FXGraphics2D facade
- road lanes remove usage of FXGraphics2D facade
- road edges remove usage of FXGraphics2D facade
- remove FXGraphics2D facade if all of the above is converted

### hover information
- vehicles
- sinks
- sources

### interactive
- traffic light change
- variable message sign

### keyboard support

### remove everything awt related
- e.g. colors

### review PaintRoadMapping class