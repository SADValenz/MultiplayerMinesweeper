# Server

## Output

Todos los mensajes que el server enviara al Cliente con los parámetros que este pueda contener

| Comando       | Parámetros                     | Descripción                                                  |
| ------------- | ------------------------------ | ------------------------------------------------------------ |
| SUBMITNAME    |                                | Manda petición de Nombre                                     |
| NAMEACCEPTED  | [name,id]                      | El Nombre fue aceptado, Regresa el nombre y la id del Usuario. |
| LOBBYSETTINGS |                                | Manda petición de los ajustes para el lobby                  |
| MESSAGE       | string del mensaje             | mensaje de chat                                              |
| CELLREVEAL    | [x,y,value,id_game]            | Manda que casilla se a revelado y que valor contenía         |
| FLAG          | [x,y,id_game]                  | Manda que casilla a sido marcada con una bandera             |
| UNFLAG        | [x,y]                          | Regresa una casilla con bandera a la normalidad              |
| GAMESTART     | [size, mine_count]             | Inicia el juego y manda el tamaño que se usara para el tablero y las minas |
| GAMEEND       |                                | Termina el juego                                             |
| URDEAD        |                                | Te dice si moriste (cuando el cliente revela una casilla con bomba) |
| USERINFO      | [id,name,id_game]              |                                                              |
| ~~MYGAMEID~~  | ~~[id_game]~~                  | ~~Preguntar por nuestra ID~~ (*obsoleto*)                    |
| USERINFO      | [id,name,id_game,alive, flags] | Informacion de un Usuario                                    |
| MOUSEPOS      | [x,y,id_game]                  | Comando de la posición del mouse (Solo GAMEMAKER)            |
| SIZE          | [size]                         | Tamaño del tablero                                           |

## Input

Todos los comando que el server espera recibir empezaran con ` / ` , un ejemplo seria:

```java
YourPrinterHere.out.println("/cell_reveal 0 0");
```

En el cual el servidor buscara el comando `field_reveal` y utilizara `0 0` como parámetro.

| Comando          | Parámetro | Descripción                                                  |
| ---------------- | --------- | ------------------------------------------------------------ |
| game_start       |           | Inicia el juego del lobby                                    |
| restart          |           | Reinicia la partida (Solo DEBUG)                             |
| board_get        |           | Petición para recibir el tablero por el chat                 |
| board_get_dev    |           | Petición para recibe el tablero revelado por el chat(Solo DEBUG) |
| cell_reveal      | [x,y]     | Revela una casilla con las coordenadas mandadas              |
| cell_set_flag    | [x,y]     | Coloca una Bandera en las coordenadas mandadas               |
| cell_remove_flag | [x,y]     | Recupera una Bandera en las coordenadas mandadas             |
| global           | mensaje   | Manda un mensaje a todos los lobbies disponibles             |
| lobby_info       |           | Petición para recibir la información del lobby               |
| lobby_list       |           | Petición para recibir la lista de todos los lobbies          |
| user_list        |           | Petición para recibir la lista de todos los jugadores del lobby |
| user_data        |           | Recupera todos los datos de todos los usuarios del lobby actual |
| my_flags         |           | Recupera el numero de banderas que a usado el usuario        |
| set_size         | size      | Colocar el tamaño del grid a usar en el tablero              |
| set_minecount    | mines     | Cuantas minas hay en el tablero                              |
| mouse_pos        | [x,y]     | Manda la posicion actual del mouse (GAMEMAKER)               |

### Cosas que faltan.

1. Como pasar las calificaciones finales.
2. ~~Que hacer al final de la partida.~~
4. ~~Limite de banderas~~
5. ~~Limitar el jugador de que lado puede empezar~~
