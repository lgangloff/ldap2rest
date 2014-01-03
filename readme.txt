Compiler le projet pour obtenir le war: mvn clean package -Denv=XXX
	où XXX peut prendre comme valeur
		DEVPDL, DEV, INT, PREPROD ou PROD

En fonction de l'environnement choisit, le filtre s'appliquera:
	DEV => src/main/filters/DEV.properties
	INT => src/main/filters/INT.properties
	PREPROD => src/main/filters/PREPROD.properties
	PROD => src/main/filters/PROD.properties