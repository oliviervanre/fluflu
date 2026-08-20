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
- profil personnel identifié par un prénom modifiable ;
- saisie horodatée des aliments et boissons ;
- saisie des reflux et de leur intensité ;
- contexte à trois états : oui, non ou non renseigné ;
- observations sur des fenêtres de 1, 3 et 6 heures ;
- historique quotidien ;
- profil de démonstration séparé des données personnelles ;
- stockage exclusivement local sur l'appareil.

## Vérifications en ligne de commande

Depuis le dossier `android` :

```bash
./gradlew test
./gradlew assembleDebug
```

L'APK de développement est alors produit dans `app/build/outputs/apk/debug/`.

## Choix provisoire de stockage

Les profils et les entrées sont sérialisés en JSON dans les préférences privées de l'application. Chaque entrée porte l'identifiant UUID de son profil. Ce choix limite les dépendances pendant la validation du modèle fonctionnel. La migration vers Room/SQLite sera pertinente avant l'ajout du référentiel personnel et de la fusion d'aliments.

La version `0.2.0` inaugure ce modèle. Les anciennes données du prototype `0.1.0` ne sont pas reprises, car elles étaient exclusivement constituées de données d'essai.
