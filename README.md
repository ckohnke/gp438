gp438 Senior Design: Exploratory Seismic Data Analysis in the Field
=====

Workflow Process

1a. GPS Tools -> Get Handheld GPS
    Import a CSV, TSV file that is Station, Lat, Lon
    Allows user to use: 
      - Export GPS to CSV

1c. SEGD Tools -> Import SEGD Directory or Import SEGD File(s)
    Imports all SEGD data from a directory or a specific set of files.
    Allows user to use: 
      - No GPS Mode
      - Channel Mode
      - Plot Controls

2.  Import either GPS or SEGD data based on what is missing.
    Allows user to use:
      - Roam Mode
      - Circle Mode
    Assumption: GPS Station numbers are the same as SEGD Shot numbers.

3.  GPS Tools -> Import NED Files
    Imports the NED files for future use.
    Allows user to use:
      - Read Elevation from NED

Actions avaliable at all times:
  - Save as PNG
  - Clear Data
  - Download NED zip Archive
  - Zoom Mode

Dependencies

1. Required:
  - Mines Java Toolkit edu.mines.jtk (https://github.com/dhale/jtk)

2. Optional:
   For Downloading NED files:
     - com.github.axet.wget (https://github.com/axet/wget)
     - com.github.axet.threads (https://github.com/axet/threads)
     - com.thoughtworks.xstream (http://xstream.codehaus.org/)
     - org.apache.commons.io (http://commons.apache.org/)

To Compile and Run:

    cd src
    ./go
