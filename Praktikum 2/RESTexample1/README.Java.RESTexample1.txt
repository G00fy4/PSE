
			RESTful Beispiel (mit Jersey and JSON)
					Verteilte Systeme
					Summer Semester 2016

INSTALLATION

0) Zip-Datei auspacken.

1) In Net-Beans (!):
    a) Tools -> Plugins -> Installed: "Java Web and EE" selektieren, aktivieren.
       (Streng genommen nicht notwendig, wird später automatisch gemacht,
       aber etwas schneller so).

    b) File -> Open Project -> <Verzeichnis, wo Sie die Zip-Datei auspackte>.

    c) Server laufen lassen mit "Run" (Grüne Dreieck oder rechte Mausklick).
       Dabei aufpassen, dass das richtige Projekt gestartet wird).
       Hier auch etwas Geduld haben (erstes mal).
       Neue Tab im Browser wird (automatisch) geöffnet mit URL 
		http://localhost:8080/RESTexample1/.
       Link "Version" ausprobieren (URL bemerken!).

    d) (Zurück in NetBeans): Client finden (Source Packages -> client).
       Mit rechte Klick Client laufen lassen ("Datei laufen lassen")

    e) Hinweis: Um mehr Features zum Server zu addieren, ist folgender Trick
       nützlich:
       Rechtemausklick an dem Projekt
     -> New -> "RESTful Web Services from Patterns"
     -> Simple Root Resource ("Next >")
     -> Unter "Specify Resource Classes":
        "Resource Package" == neue oder existierende Package
        Class Name == neue Name
        MIME Type == application/json (zum Beispiel). "Finish"
     -> "ApplicationConfig.java" und "...Resource.java" erscheinen.
        Nur "...Resource.java" muss geändert werden (normalerweise).

2) Source Code studieren, verstehen, erweitern, genießen...

QUELLEN

Fast alle Material hier stammt ursprünglich von https://docs.oracle.com/cd/E19776-01/820-4867/index.html
(es gibt neuere Versionen, die allerdings nur Probleme für unsere VM machen).

Ausnahme: JSON-Verabeitung stammt von
sehe http://www.java2s.com/Tutorials/Java/JSON/0100__JSON_Java.htm 

Alle Beispiele sind geändert, bis sie mit der virtuelle Maschine vom Labor funktionierten. Sie sind allerdings so weit wie möglich im Sinne des
Erfinders geblieben.

Problemen oder Vorschläge bitte an <ronald.moore@h-da.de> bzw. im Moodle-Forum posten.

VERSION HISTORY:
9 Mai 2016: Ur-Version






