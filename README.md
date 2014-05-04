gp438 Senior Design: Exploratory Seismic Data Analysis in the Field
=====

Workflow
---

1.  GPS Tools > Get Handheld GPS

    Import a CSV, TSV or GPX file of GPS points.

    Allows user to use: 
      - Export GPS to CSV

2.  SEGD Tools > Import SEGD Directory or Import SEGD File(s)

    Imports all SEGD data from a directory or a specific set of files.

    Allows user to use: 
      - No GPS Mode
      - Channel Mode
      - Plot Controls

3.  GPS Tools > Import NED Files

    Imports the NED files for reading the elevations.

    Allows user to use:
      - Read Elevation from NED

4.  Once the GPS and the SEGD data is imported, the user can use any of the modes.

    Allows user to use:
      - Roam Mode
      - Circle Mode

    Assumption: GPS Station numbers are the same as SEGD Shot numbers.

Actions avaliable at all times:
  - Save as PNG
  - Clear Data
  - Download NED zip Archive
  - Zoom Mode

Dependencies
---

1. Required:
  - Mines Java Toolkit edu.mines.jtk (https://github.com/dhale/jtk)

2. Optional:
   - For Downloading NED files:
     - com.github.axet.wget (https://github.com/axet/wget)
     - com.github.axet.threads (https://github.com/axet/threads)
     - com.thoughtworks.xstream (http://xstream.codehaus.org/)
     - org.apache.commons.io (http://commons.apache.org/)

Compile and Run
---

    cd src
    ./go
