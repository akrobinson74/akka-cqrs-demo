MAKE=/usr/bin/make
service-up-compose = docker-compose -f docker/base-services.yml

clean:
	@rm -rf docker/app/build/ build/
	sbt clean

build:
	sbt compile

compose.service.up: build
	$(service-up-compose) build
	$(service-up-compose) up
	$(MAKE) -f $(CURRENT_FILE) compose.service.down

compose.service.down:
	$(service-up-compose) down

##########################################################
# Handle deployment
##########################################################

deploy:
	echo "NOT IMPLEMENTED!!!!"