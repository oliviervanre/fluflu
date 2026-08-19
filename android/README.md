# Application Android FluFlu

Première version native en Kotlin et Jetpack Compose. Elle reprend les parcours validés dans la maquette web.

## Ouvrir le projet

1. Installer une version récente d'Android Studio.
2. Ouvrir le dossier `fluflu/android` avec **Open**.
3. Attendre la synchronisation Gradle et l'installation éventuelle du SDK Android 36.
4. Choisir un téléphone ou un émulateur puis cliquer sur **Run app**.

L'application demande Android 8.0 ou une version ultérieure (`minSdk 26`).

## Fonctions intégrées

- journal quotidien ;
- saisie horodatée des aliments et boissons ;
- saisie des reflux et de leur intensité ;
- contexte à trois états : oui, non ou non renseigné ;
- observations sur des fenêtres de 1, 3 et 6 heures ;
- historique quotidien ;
- données de démonstration ;
- stockage exclusivement local sur l'appareil.

## Vérifications en ligne de commande

Depuis le dossier `android` :

```bash
./gradlew test
./gradlew assembleDebug
```

L'APK de développement est alors produit dans `app/build/outputs/apk/debug/`.

## Choix provisoire de stockage

Les entrées sont sérialisées en JSON dans les préférences privées de l'application. Ce choix limite les dépendances pendant la validation du modèle fonctionnel. La migration vers Room/SQLite sera pertinente avant l'ajout du référentiel personnel et de la fusion d'aliments.
