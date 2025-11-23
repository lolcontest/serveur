# Serveur ATLANTA

Ce dépôt contient un émulateur de serveur de jeu écrit en Kotlin. Il fournit un point d'entrée unique, une pile réseau basée sur Apache MINA et un ensemble riche de fonctionnalités (commandes administrateur, synchronisation, gestion de la base de données) destinées à faire tourner un shard OLDFUS.

## Points d'entrée et cycle de vie
- **Boot** : `AtlantaMain.main` installe un hook d’arrêt, charge la configuration, ouvre la connexion SQL, instancie le monde de jeu (`Mundo.crearServidor`) puis démarre l’acceptor réseau. Des tâches périodiques nettoient les sessions inactives, libèrent la mémoire et réinitialisent certains compteurs.【F:src/estaticos/AtlantaMain.kt†L1101-L1200】
- **Chargement de la configuration** : `AtlantaMain.cargarConfiguracion` lit `config_Servidor.txt`, contrôle les doublons et alimente les drapeaux d’exécution (mode debug, ports, systèmes de rareté, etc.).【F:src/estaticos/AtlantaMain.kt†L1253-L1303】

## Accès aux données
- **Connexion SQL** : `GestorSQL.iniciarConexion` ouvre trois connexions (dynamique, statique et comptes) avec `autoCommit` configurable, valide les sockets et programme un commit différé pour limiter les écritures massives.【F:src/estaticos/database/GestorSQL.kt†L166-L195】

## Réseau
- **Acceptor de jeu** : `ServidorServer.start` installe un `NioSocketAcceptor`, attache un codec texte et écoute sur le port publié dans la configuration. Il expose des utilitaires pour fermer proprement l’acceptor ou publier l’état au service d’échange.【F:src/servidor/ServidorServer.kt†L276-L327】

## Commandes et outils de modération
- **Traitement des commandes** : `Comandos.consolaComando` route chaque commande selon son rang requis, vérifie le niveau d’accès du compte, notifie l’utilisateur dans sa langue et journalise l’ordre via `GestorSQL.INSERT_COMANDO_GM`. Les commandes de niveau 1 couvrent par exemple le gel/dégel de joueurs, l’inspection de PNJ ou l’affichage des taux de drop.【F:src/estaticos/Comandos.kt†L38-L200】

## Démarrer le serveur
1. Vérifiez `config_Servidor.txt` (hôte SQL, ports, modes de jeu) puis ajustez les paramètres si nécessaire.
2. Assurez-vous que les bases dynamiques/statique/comptes sont accessibles.
3. Exécutez `java -jar jar/server.jar` depuis la racine du dépôt pour lancer `AtlantaMain.main`.

## Structure du code
- `src/estaticos/` : constantes, utilitaires généraux, point d’entrée et orchestrateurs.
- `src/servidor/` : pile réseau (acceptor, sockets et handler MINA).
- `src/sincronizador/` : échanges inter-services et synchronisation (ExchangeClient, etc.).
- `src/variables/` : modèles du domaine (personnages, monstres, objets, cartes, combats…).
- `libs/` et `jar/` : dépendances et binaire exécutable prêt à l’emploi.

## Journalisation et maintenance
- Les logs serveur sont écrits dans `Logs_Servidor_<nom>/Log_Servidor_<date>.txt` lorsque l’exécution n’est pas en mode localhost.【F:src/estaticos/AtlantaMain.kt†L1109-L1130】
- Des tâches planifiées exécutées dans `AtlantaMain.main` nettoient les sessions inactives (`ServidorServer.cleanAFKS`) et libèrent la mémoire pour conserver des performances stables.【F:src/estaticos/AtlantaMain.kt†L1145-L1182】
