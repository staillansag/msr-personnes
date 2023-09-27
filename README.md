# webMethods package template

L'objectif de ce template est de faciliter la création de nouveaux packages de microservices webMethods.

Mode opératoire:

Cliquez sur le bouton vert "use this template" pour créer un nouveal repo Github à partir de ce template.
Le nouveau repo sera rattaché à votre ID Github.

Clonez le repo en local.
Si votre MSR est installé directement sur votre machive de travail, clonez simplement votre repo dans le répertoire packages.
Si votre MSR est situé dans un conteneur Docker, clonez votre repo où bon vous semble et montez le répertoire cloné en tant que volume dans ce conteneur.
Redémarrez ensuite votre MSR pour faire apparaître le nouveau package au niveau du Designer, vous aurez probablement besoin de faire un "refresh" au préalable.

Le template intégre un fichier .gitignore préconfiguré.
Il y a également un exemple de Dockerfile avec le fichier .dockerignore qui va avec.