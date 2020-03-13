runSB_Dev.sh
	sudo docker run -v "$(pwd):/usr/src/myapp" --net test-network  -p 8080:8080 --name spring-boot1 spring-boot
	{
		This runs the container without removing it after exit.
		It creates a conainer named spring-boot1.
		It mounts the directory $(pwd) to the container's directory /usr/src/myapp.
		This is useful so that the image can be reused without building, but the content will use the study-spring-boot-1.0.jar in the $(pwd) folder.
	}
restartSB_dev.sh
	sudo docker restart spring-boot1
	{
		restarts the spring-boot1 container.
		Use this when the study-spring-boot-1.0.jar is updated
	}