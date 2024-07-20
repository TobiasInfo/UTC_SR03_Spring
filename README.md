# Projet chat

## Objectif du projet

L'objectif du projet est de créer un service de chat en ligne. L'application est décomposée en deux parties :
- Un projet Spring : Cette partie implémente l'interface d'administration afin de pouvoir administrer les utilisateurs de l'application.
- Un projet React : Cette partie correspond à l'interface de chat : elle est la même pour les utilisateurs et les administrateurs. C'est ici qu'on choisit un chat et où l'on discute avec les autres utilisateurs du chat.

**Ici, on s'intéressera uniquement à la partie Spring du projet**

## Fonctionnement

- Pour lancer le projet, il faut **IMPERATIVEMENT** se connecter en VPN au réseau de l'UTC, sinon le serveur plantera car il n'arrivera pas à se connecter à la base de données.
- Une fois cela fait, il faut ouvrir un navigateur et aller à l'URL suivante :
  - _http://localhost:8080/_
- Une fois sur l'URL : on peut se connecter avec le compte suivant :
  - **User** : _john.smith@example.com_
  - **Mot de passe** : _Password1_

## Architecture de l'interface d'administration

L'architecture est de type Model View Controller version 2.

## Sécurité

- Pour cette partie, nous avons implémenté la gestion de session. Lorsqu'on s'authentifie avec un compte administrateur, la session se voit ajouter une valeur "ADMIN" à son attribut "role" au scope. Dans le cas contraire, le rôle pour la session prend une valeur "NotAuthenticated" ou "USER".  
- Pour accéder aux autres parties de l'interface d'administration (sauf inscription), il est nécessaire que le contexte de la session comporte la valeur "ADMIN" pour l'attribut "role", sinon on ne peut accéder à rien.
