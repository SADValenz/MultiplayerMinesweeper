# Server

## Output

Todos los mensajes que el server enviara al Cliente con los parámetros que este pueda contener

| Comando       | Parámetros         | Descripción                                                  |
| ------------- | ------------------ | ------------------------------------------------------------ |
| SUBMITNAME    |                    | Manda petición de Nombre                                     |
| NAMEACCEPTED  |                    | El Nombre fue aceptado                                       |
| LOBBYSETTINGS |                    | Manda petición de los ajustes para el lobby                  |
| MESSAGE       | string del mensaje | mensaje de chat                                              |
| CELLREVEAL    | [x,y,value]        | Manda que casilla se a revelado y que valor contenía         |
| FLAG          | [x,y]              | Manda que casilla a sido marcada con una bandera             |
| GAMESTART     | [size]             | Inicia el juego y manda el tamaño que se usara para el tablero |
| GAMEEND       |                    | Termina el juego                                             |
| URDEAD        |                    | Te dice si moriste (cuando el cliente revela una casilla con bomba) |



### Cosas que faltan.

1. Como pasar las calificaciones finales.
2. Que hacer al final de la partida.

## Input

Todos los comando que el server espera recibir empezaran con ` / ` , un ejemplo seria:

```java
YourPrinterHere.out.println("/cell_reveal 0 0");
```

En el cual el servidor buscara el comando `field_reveal` y utilizara `0 0` como parámetro.

| Comando       | Parámetro | Descripción                                                  |
| ------------- | --------- | ------------------------------------------------------------ |
| game_start    |           | Inicia el juego del lobby                                    |
| restart       |           | Reinicia la partida (Solo DEBUG)                             |
| board_get     |           | Petición para recibir el tablero por el chat                 |
| board_get_dev |           | Petición para recibe el tablero revelado por el chat(Solo DEBUG) |
| cell_reveal   | [x,y]     | Revela una casilla con las coordenadas mandadas              |
| flag          | [x,y]     | Coloca una Bandera en las coordenadas mandadas               |
| global        | mensaje   | Manda un mensaje a todos los lobbies disponibles             |
| lobby_info    |           | Petición para recibir la información del lobby               |
| lobby_list    |           | Petición para recibir la lista de todos los lobbies          |
| user_list     |           | Petición para recibir la lista de todos los jugadores del lobby |

