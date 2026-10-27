# YT320 — Samsung S24

Eenvoudige Android-app: deel een YouTube-link met **YT320** of plak de link, en sla audio op als **MP3 met doelbitrate 320 kbps** in de gewone **Downloads**-map.

- Android 10+, gebouwd voor Samsung Galaxy S24 (ARM64)
- Geen Termux, advertenties, account of brede opslagpermissies
- Gebruikt [youtubedl-android](https://github.com/yausername/youtubedl-android) en FFmpeg
- De MP3-conversie verhoogt de audiokwaliteit van de bron **niet**
- Download uitsluitend materiaal dat je mag downloaden
- Houd de app open tijdens een download

## APK bouwen

Elke push naar `main` start **Actions → Bouw YT320 APK**. Als de workflow volledig slaagt, wordt de debug-APK beschikbaar onder **Releases** als `YT320-S24.apk`, zonder gebruik van GitHub Actions-artifactopslag.

Dit is een testbuild; hij wordt automatisch ondertekend met een tijdelijke debugsleutel. Niet geschikt als officiële Play Store-release. De app moet nog op een echte Samsung S24 worden getest.

## Licentie

Broncode is vrijgegeven onder GPL-3.0; de gebruikte downloaderbibliotheek is eveneens GPL-3.0. [Volledige licentietekst](https://www.gnu.org/licenses/gpl-3.0.txt). Niet gelieerd aan YouTube of Google.

Dit project is volledig onafhankelijk van TSOB Network Lab.
