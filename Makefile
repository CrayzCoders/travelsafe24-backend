run:
	mvn spring-boot:run

import-geometry:
	mvn spring-boot:run -Dspring-boot.run.profiles=import-geometry

import-amenities:
	mvn spring-boot:run -Dspring-boot.run.profiles=import-amenities