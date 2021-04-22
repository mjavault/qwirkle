### Starting the container
Build the application:
```shell
docker build -t mjavault/labyrinth:latest .
docker push mjavault/labyrinth:latest
```
Run the application:
```shell
docker run -p 8080:8080 mjavault/labyrinth 
```


## TODO
- Keep username between sessions
- Keep player selection between reload?
- AI
- Rotate on tablet
