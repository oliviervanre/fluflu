# FluFlu

Prototype web mobile d'un journal personnel destiné à rapprocher les aliments consommés et les épisodes de reflux gastrique.

L'application est un outil d'observation et d'aide-mémoire. Elle ne pose pas de diagnostic et ne remplace pas le suivi médical.

## Fonctions du premier prototype

- saisie horodatée d'un aliment, d'une boisson ou d'un repas ;
- signalement d'un reflux avec une intensité de 1 à 4 ;
- contexte à trois états : oui, non ou non renseigné ;
- journal chronologique par journée ;
- analyse des reflux survenus dans les 1, 3 ou 6 heures suivant un aliment ;
- historique des journées renseignées ;
- stockage exclusivement local dans le navigateur ;
- données de démonstration accessibles depuis le menu `•••`.

## Lancer l'application

Après clonage du dépôt, ouvrir un terminal dans le dossier :

```bash
python -m http.server 8080
```

Puis ouvrir <http://localhost:8080>.

Il est déconseillé d'ouvrir directement `index.html` : le petit serveur local permet au fonctionnement hors ligne de s'initialiser correctement.

## Tester dans un écran de téléphone avec Chrome

1. Ouvrir <http://localhost:8080>.
2. Appuyer sur `F12`.
3. Cliquer sur l'icône représentant un téléphone et une tablette, ou utiliser `Ctrl+Maj+M`.
4. Choisir un appareil dans la liste, par exemple `Pixel 7`.
5. Actualiser la page après avoir sélectionné l'appareil.

## Tester depuis un smartphone sur le même réseau Wi-Fi

Lancer le serveur en l'autorisant à écouter sur le réseau local :

```bash
python -m http.server 8080 --bind 0.0.0.0
```

Relever l'adresse IP locale du PC puis ouvrir `http://ADRESSE_IP:8080` dans le navigateur du smartphone. Le pare-feu du PC doit autoriser Python sur le réseau privé.

Le mode hors ligne et l'installation PWA nécessitent HTTPS sur un smartphone. Ils fonctionnent en revanche sur `localhost` depuis le PC.

## Limites actuelles

- aucune synchronisation entre appareils ;
- aucune sauvegarde ou exportation ;
- analyse temporelle volontairement simple, sans interprétation médicale des facteurs ;
- aucune authentification ;
- pas encore d'icône d'installation.
