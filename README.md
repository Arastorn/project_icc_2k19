# project_icc_2k19

# Docker
    - Dans `src/main/scala/serveur/ApplicationServer.scala` :
        - Modifier `val host = "localhost"` par `val host = "0.0.0.0"`
    - À la racine du projet :
        - `sbt stage`
        - `sbt docker:publishLocal`
    - `docker run -dit -p 9001:9001 --name projet_icc_2k19 projet_icc_2k19:0.1.0-SNAPSHOT`


# Informations de l'API

## Description

* /hiking/:id :
  - **GET** Récupère les texte d'une randonnée sur l'api choucas avec l'id correspondant.
* /extract :
  - **POST** Récupère des entitées sémantiques via un texte via le service DBPEDIA
* /geolocalize :
  - **POST** Géolocalise des entitées sémantique
* /utm :
  - **POST** Récupère une bounding box à partir de coordonées
* /images :
  - **POST** Télécharge des images qui appartiennent à une bounding box et d'une date
* /tiles :
  - **POST** Tuile une image à partir de son nom
  * /status/:name :
    - **GET** Récupère le status actuel du tuilage à partir de son nom
* /save :
  - **POST** Enregistre une image et ses tuiles sur hdfs
* /metadata :
  - **POST** Insère des meta-données dans un elastic search
* /elasticSearch :
  - **POST** Récupère une image qui est inclue dans une bounding box donnée

## Exemples

### Hiking

```bash
curl  --request GET \
      --url http://localhost:9001/hiking/72314 \
      --header 'cache-control: no-cache'
```

### Extract

```bash
curl  --request POST \
    --url http://localhost:9001/extract \
    --header 'Content-Type: application/json' \
    --header 'cache-control: no-cache' \
    --data '{\n    "description": "Stationnement à Pré Richard, à proximité de l'\''aire d'\''arrivée du télésiège. A partir de Bernex, on atteint Pré Richard en passant par le hameau de Trossy et en dépassant Malpasset et la Fétuère (route généralement fermée en hiver). (D/A) Partir au Sud et, à proximité de cabanes de départ de téléskis, prendre la piste sur la gauche (Est) qui s'\''élève d'\''abord en pente douce. Après le virage en épingle à cheveux sur la droite et avant les chalets, quitter la piste et prendre à gauche un sentier qui aboutit à un carrefour. (1) Suivre à gauche une piste qui passe sous la ligne du télésiège du petit Combet puis vire à droite et devient raide. Elle fait un net virage à gauche et la pente s'\''adoucit jusqu'\''au départ du télésiège de Pelluaz (La Combe). (2) Dépasser les installations du télésiège et poursuivre Est-Nord-Est. La piste contourne une mare par la gauche puis devient raide et s'\''oriente au Sud. Passer sous la ligne du télésiège de Pelluaz et aboutir à un collet. (3) Du collet, prendre un sentier sur la droite (Ouest). Passer à la table d'\''orientation tout de suite sur la droite puis rejoindre la crête. Ignorer un sentier qui descend sur la droite, poursuivre le cheminement très agréable en crête vers l'\''Ouest et atteindre la Tête des Fieux. (4) Prendre sur la droite un sentier raide qui descend en forêt (panneau). Aboutir au sommet d'\''une combe, à proximité de l'\''arrivée du téléski de l'\''Arête. (5) Prendre sur la gauche le sentier qui descend vers le Col des Boeufs (panneau) et laisser l'\''arrivée du téléski à main droite. (6) Du Col, s'\''engager dans une pente herbeuse en direction du Mont Baron (panneau). Atteindre le sommet après plusieurs lacets. (7) Entamer la descente par le même chemin et, après quelques dizaines de mètres, s'\''engager dans un sentier qui part sur la droite et conduit à un petit couloir. Descendre ce dernier sans aucune difficulté et poursuivre le sentier vers la droite (Ouest). Déboucher dans des alpages. (8) A la croisée de sentiers, prendre sur la droite à angle aigu un sentier en direction de l'\''aplomb du Mont Baron (panneau). Le sentier pénètre en forêt. Un balisage Jaune se perd et le sentier est très peu marqué. Suivre un cheminement plein Est en forêt jusqu'\''à déboucher à nouveau en prairie, à l'\''arrivée du téléski du Baron. (9) Descendant en demeurant sous la ligne de téléski ou légèrement sur sa droite. S'\''écarter ensuite du téléski par la droite et rejoindre en sous-bois une piste plus large. Suivre celle-ci à l'\''Est jusqu'\''au départ de téléskis. Retrouver à gauche le parking de départ (D/A).Points de passage : D/A : km 0 - alt. 1339m - Pré Richard 1 : km 0.77 - alt. 1432m - Bifurcation 2 : km 1.58 - alt. 1543m - La Combe 3 : km 2.57 - alt. 1745m - Collet entre Pointe de Pelluaz et Tête des Fieux 4 : km 3.29 - alt. 1737m - Tête des Fieux 5 : km 4.14 - alt. 1513m - Sommet d'\''une combe - Téléski de l'\''Arête 6 : km 5.08 - alt. 1434m - Col des Boeufs 7 : km 5.62 - alt. 1529m - Mont Baron 8 : km 6.3 - alt. 1434m - Croisée de chemins dans les pâturages 9 : km 6.94 - alt. 1430m - Arrivée du téléski du Baron D/A : km 8.02 - alt. 1339m - Pré Richard",\n    "name": "Tête des Fieux et Mont Baron"\n}'
```

### Geolocalize

```bash
curl  --request POST \
      --url http://localhost:9001/geolocalize \
      --header 'Content-Type: application/json' \
      --header 'cache-control: no-cache' \
      --data '{\n    "entities": [\n        "http://fr.dbpedia.org/resource/Richard_Ier_d'\''Angleterre",\n        "http://fr.dbpedia.org/resource/Télésiège",\n        "http://fr.dbpedia.org/resource/Bernex_(Genève)",\n        "http://fr.dbpedia.org/resource/Richard_Ier_d'\''Angleterre",\n        "http://fr.dbpedia.org/resource/Hameau",\n        "http://fr.dbpedia.org/resource/Barrage_de_Malpasset",\n        "http://fr.dbpedia.org/resource/Téléski",\n        "http://fr.dbpedia.org/resource/Chalet",\n        "http://fr.dbpedia.org/resource/Télésiège",\n        "http://fr.dbpedia.org/resource/Télésiège",\n        "http://fr.dbpedia.org/resource/Télésiège",\n        "http://fr.dbpedia.org/resource/Télésiège",\n        "http://fr.dbpedia.org/resource/Table_d'\''orientation",\n        "http://fr.dbpedia.org/resource/Forêt",\n        "http://fr.dbpedia.org/resource/Téléski",\n        "http://fr.dbpedia.org/resource/Téléski",\n        "http://fr.dbpedia.org/resource/Alpage",\n        "http://fr.dbpedia.org/resource/Sentier_de_grande_randonnée",\n        "http://fr.dbpedia.org/resource/Forêt",\n        "http://fr.dbpedia.org/resource/Balisage",\n        "http://fr.dbpedia.org/resource/Forêt",\n        "http://fr.dbpedia.org/resource/Téléski",\n        "http://fr.dbpedia.org/resource/Téléski",\n        "http://fr.dbpedia.org/resource/Téléski",\n        "http://fr.dbpedia.org/resource/Téléski",\n        "http://fr.dbpedia.org/resource/Point_(baseball)",\n        "http://fr.dbpedia.org/resource/Richard_Ier_d'\''Angleterre",\n        "http://fr.dbpedia.org/resource/Téléski",\n        "http://fr.dbpedia.org/resource/Alpage",\n        "http://fr.dbpedia.org/resource/Téléski",\n        "http://fr.dbpedia.org/resource/Richard_Ier_d'\''Angleterre"\n    ]\n}'
```

### Utm

```bash
curl  --request POST \
      --url http://localhost:9001/utm \
      --header 'Accept: application/json' \
      --header 'Content-Type: application/json' \
      --header 'cache-control: no-cache' \
      --data '{\n    "coords": [\n        {\n            "latitude": "46.166688",\n            "longitude": "6.066644",\n            "uri": "http://fr.dbpedia.org/resource/Bernex_(Genève)"\n        },\n        {\n            "latitude": "43.51224",\n            "longitude": "6.757",\n            "uri": "http://fr.dbpedia.org/resource/Barrage_de_Malpasset"\n        }\n    ]\n}'
```


### Images

```bash
curl  --request POST \
      --url http://localhost:9001/images \
      --header 'Content-Type: application/json' \
      --header 'cache-control: no-cache' \
      --data '{\n    "boundingbox": {\n        "ne": {\n            "lng": 6.757,\n            "lat": 46.166688\n        },\n        "sw": {\n            "lng": 6.066644,\n            "lat": 43.51224\n        }\n    },\n    "date": {\n        "start": "2019-03-11",\n        "end": "2019-04-01"\n    }\n}'
```

### Tiles

```bash
curl  --request POST \
      --url http://localhost:9001/tiles \
      --header 'Content-Type: application/json' \
      --header 'cache-control: no-cache' \
      --data '{\n    "name": "03d771d9-eb6f-5439-8b31-18cd9a547718"\n}'
```

```bash
curl  --request GET \
      --url http://localhost:9001/tiles/status/7febbd3b-98f3-5699-8c12-d82f173680ac \
      --header 'cache-control: no-cache'
```

### Saves

```bash
curl  --request POST \
      --url http://localhost:9001/save \
      --header 'Content-Type: application/json' \
      --header 'Postman-Token: 40129199-ed13-4936-8980-db357eee97ee' \
      --header 'cache-control: no-cache' \
      --data '{\n	"imgName": "be65e6ec-3118-5601-9b28-858f0bc6ea96"\n}'
```

### MetaData

```bash
```

### ElasticSearch

```bash
```

# Authors

Antoine Bourgeois
Lucas Pauzies
Laurine Sorel
