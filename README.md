# project_icc_2k19

# Docker
    - Dans `src/main/scala/serveur/ApplicationServer.scala` :
        - Modifier `val host = "localhost"` par `val host = "0.0.0.0"`
    - À la racine du projet :
        - `sbt stage`
        - `sbt docker:publishLocal`
    - `docker run -dit -p 9001:9001 --name projet_icc_2k19 projet_icc_2k19:0.1.0-SNAPSHOT`

