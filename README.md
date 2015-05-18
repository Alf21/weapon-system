# weapon-system

[![Build Status](http://ci.gtaun.net/job/weapon-system/badge/icon)](http://ci.gtaun.net/job/weapon-system/)

Was ist das besondere an diesem Waffensystem?

Dieses Waffensystem ist sehr speziell, da man bei jeder Pistole 5 Munitionsarten zur Auswahl hat.
Munitionsarten:
Munitionsname
Effekt
Normale Munition	kein Effekt
Brandmunition	entzündet den Gegner und lässt ihn für 7sek brennen
Explosivmunition	explodiert beim Aufprall und schadet dem Gegner beim Treffer
Panzerbrechende Munition	ignoriert die Rüstung / den Armour des Gegners
Spezialmunition	mehr Schaden


Kleines Intro:
Man kann die Munition in jeder Pistole mit der Taste [N] (kurz gedrückt) wechseln.
Sobald man die Taste [N] ungefähr 1sek gedrückt hält, gelangt man in das Waffeninventar bzw. den integrierten Waffenshop.
Dort kann man alle Waffen auswählen bzw. kaufen und auch die Munition nachladen / kaufen.
Die gekauften Waffen muss man nicht mehr bezahlen, nur noch die Munition.
Zudem gibt es Animationen beim Nachladen oder wechseln der Waffen, sodass man eine kleine Nachladedauer hat und nicht iwie SAMP Bugs ausnutzen kann.


https://youtu.be/v36qQqOUJIE
by BloodAngelHD


Andere Infos:
Speicherart: MySQL (wird selbst erstellt)
Programmiersprache: Java (Shoebill)
Art des Systems: Plugin für das Shoebill Plugin
-> Warum Shoebill / Java?
Shoebill bzw. Java hat bessere Performance, da es Daten schneller verarbeitet als Pawn. Das war mir sehr wichtig, da nach / bei jedem Schuss etwas berechnet wird!
Java ist "kommende" Programmiersprache, zB Android Applikationen (Apps) sind auf Java geschrieben.
Java bietet für mich mehr Möglichkeiten und besseren Support
Und ich kann meine Lieblings-IDE / meinen Lieblings-Editor Eclipse nutzen, schöne Autovervollständigung ^^

Der Schaden der Explosionen, die durch Waffen mit Explosivmunition ausgelöst wurden, wurde auch angepasst.
Zudem ist der Schaden / Preis jeder Munitionsart unterschiedlich
 Schaden und Preise
Munitionsart
Schaden
Preis
Normal	50%*	100%*
Brand	1 (+Zeitschaden (2))	10%*
Explosiv	10%* (+Flächenschaden / Explosionsschaden**)	250%*
Panzerbrechend	65%* (+ignoriert den Armour des Gegners)	120%*
Spezial	120%*	350%*

* des normalen Schadens / Preises
** Entfernung von: 1 - 10, 2 - 5, 3 - 2, >4 - 0


GitHub: https://github.com/Alf21/weapon-system.git
Ein Dank an @123marvin123 (für die Hilfe, einen bösen Fehler zu finden, für bessere Struktur und Performance und für die Bereitsstellung der repo) und an BloodAngelHD (für das Video)!  :thumbsup: 

Installation:
Entweder von GitHub (https://github.com/Alf21/weapon-system.git) selbst downloaden und mit Maven builden oder hier (http://ci.gtaun.net/job/weapon-system/) downloaden und danach das Plugin in den Ordner shoebill/plugins im Serverordner verschieben und in der Datei shoebill/resources.yml unter "plugins:"
  - me.alf21:weapon-system:1.0-SNAPSHOT
einfügen

Zum Schluss noch folgendes in eure pom.xml im Gamemode oder Plugin einfügen (in den dependencies):

<dependency> 
    <groupId>me.alf21</groupId>
    <artifactId>weapon-system</artifactId>
    <version>1.0-SNAPSHOT</version>
    <type>jar</type>           
    <scope>compile</scope>
</dependency>

Was ihr also braucht: Shoebill ([0.3.7] Project Shoebill 1.1 - Schreibe Gamemodes in Java) + MySQL Datenbank -> localhost oder einen vServer / Root für Shoebill

Achtung!:
Keine Neuveröffentlichungen oder ändern der Credits.
Ausnahme: Falls jemand den Code braucht um das als Filterscript / Include zu schreiben (Verweis auf diesen Beitrag)

Callbacks:
 
//Um Waffen zu geben
givePlayerWeapon(player, weaponId, ammo) //Um eine Waffe komplett neu zu laden
givePlayerNewWeapon(player, weaponId, ammo) //Um eine Waffe mit den alten Munitionsdaten usw. zu laden

//Um Muni zu bekommen:
getWeaponAmmo(player, weaponId)
getFireWeaponAmmo(player, weaponId)
getExplosiveWeaponAmmo(player, weaponId)
getHeavyWeaponAmmo(player, weaponId)
getSpecialWeaponAmmo(player, weaponId)

//Um Muni zu setzen
setWeaponAmmo(player, weaponId, ammo)
setFireWeaponAmmo(player, weaponId, ammo)
setExplosiveWeaponAmmo(player, weaponId, ammo)
setHeavyWeaponAmmo(player, weaponId, ammo)
setSpecialWeaponAmmo(player, weaponId, ammo)

//Um die geladene Munitionsart zu bekommen:
getAmmoState(player, weaponId)

//Um die geladene Munitionsart zu setzen (Achtung, sicher sein, dass man auch die jeweilige Munition besitzt)
setAmmoState(player, weaponId, ammoState)

//Um zu checken, ob eine Waffe ausgewählt ist / getragen wird
isSelectedWeapon(player, weaponId)

//Um Waffe ausgewählt / getragen zu setzen
setSelectedWeapon(player, weaponId, bool)

//Um zu checken, ob eine Waffe verfügbar / gekauft wurde
isAbleWeapon(player, weaponId)

//Um Waffe gekauft / verfügbar zu setzen
setAbleWeapon(player, weaponId, bool)

//Um alle Waffen eines Slots als nicht selected bzw nicht getragen / ausgewählt zu markieren
unselectWeapons(player, slot)


Es tut mir leid, falls welche meckern, dass es kein professioneller Code ist. Es ist mein erstes Plugin in Java und ich "lerne" noch nicht so lange Java, aber ich hatte eine Idee und wollte diese verwirklichen.
Updates werden kommen. 
Tipps oder Verbesserungen sind immer erwünscht.

 existierende Bugs:
Es gibt noch einige Bugs, die werden aber alle behoben:
die Berechnung des Schadens bei einer Explosion ist nur auf die x-Koordinaten belegt -> man schießt in die Höhe und bekommt Schaden, egal welche Distanz (es muss ein Aufprall geben, zB an einem Objekt, sonst gibts keine Explosion ^^)
manchmal wird die Animation nicht komplett gecleared

 Vorhaben / Kommende Updates:
- bessere Performance
- neues Video -> Weaponshop + Schadensdemonstration
- Anticheat
- mehr Animationen
- kleines Fixes, falls es noch Bugs gibt, bitte reporten !
- vllt. als Filterscript für Pawn
- Spezialmunition mit bestimmten Effekt
- Eventsystem
- andere Ideen
- Updater


MfG Alf21
