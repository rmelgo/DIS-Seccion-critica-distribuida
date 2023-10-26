# Implementación de una sección crítica distribuida a través del algortimo de Ricart y Agrawala

![Inicial](https://github.com/rmelgo/DIS-Seccion-critica-distribuida/assets/145989723/0efba7f9-a8e4-4a5e-b7a2-7f6d2f9e2a7c)

# - Introducción

Proyecto realizado en la asignatura de Sistemas Distribuidos del grado de Ingenieria Informática de la Universidad de Salamanca. El enunciado del proyecto se encuentra subido en el repositorio en un archivo PDF llamado <a href="https://github.com/rmelgo/ANIM-Juego-plataformas-Unity/blob/main/Enunciado%20Unity.pdf" target="_blank">*PracticaObligatoria.pdf*</a>.

El principal objetivo de este proyecto es la realización de un programa en Java que implemente el ***algoritmo de Ricart y Agrawala*** para regular el acceso a una zona de exclusión mutua la cual sera compartida por varios procesos ubicados en máquinas distintas.
Para implementar dicho algoritmo, será necesario implementar un mecanismo ***NTP*** para calcular el desplazamiento de los relojes respecto a una maquina de referencia.
Esta máquina de referencia se encarga registrar los accesos a la sección crítica y comprobar que nos se han producido violaciones en la sección crítica distribuida.


Para realizar la comunicación de las distintas máquinas se utilizará la API de ***REST***, el cual es una interfaz de comunicación.

# - Comentarios sobre el entorno de ejecución

Para ejecutar este programa, se puede hacer uso de cualquier Sistema Operativo, pero será necesario la utilización de la herramienta ***Eclipse***, en concreto la versión *Enterprise Edition*.    

# - Descripción general del proyecto

## Procesos

En este proyecto, se busca implementar una sección crítica distribuida en la que se crearán 6 procesos los cuales se ejecutarán en 3 máquinas diferentes (cada máquina contará con 2 procesos).

Los 6 procesos deben seguir el siguiente esquema:

- Mediante una llamada a sleep, simulan la realización de un cálculo de duración aleatoria, distribuida uniformemente entre 0.3 y 0.5s
- Luego entrarán en una sección crítica común de gestión distribuida (SC)
- Permanecerán en ella de 0.1 a 0.3s
- Repetirán el cálculo + estancia en SC 100 veces
- Terminarán la ejecución de manera ordenada

Para controlar el acceso a la sección crítica distribuida, se utilizará el algoritmo de Ricart y Agrawala.

![Teoria 1](https://github.com/rmelgo/DIS-Seccion-critica-distribuida/assets/145989723/5e7a499d-15da-4185-a537-de982a6ae1d2)

## Logs

Cada vez que un proceso entre en la sección crítica, escribirá un mensaje en un log con la siguiente sintaxis:

```Px E tiempo```

Cada vez que un proceso salga en la sección crítica, escribirá un mensaje en un log con la siguiente sintaxis:

```Px S tiempo```

Significado de los parámetros:
  - x: Identificador del proceso (existiran identificadores del 1 al 6)
  - tiempo: Número de milisegundos transcurridos desde el 1 de enero de 1970.

## Comprobación

Al finalizar la ejecución, deben unirse los ficheros de log y verificar que no ha habido violación de la sección crítica.

Como los relojes de cada máquina no están sincronizados, debemos calcular mediante un algoritmo similar a NTP los desvíos de las máquinas respecto a una de ellas, que se tomará como referencia, y el error cometido en la medida del desvío.

La comprobación y estimación de los desvíos lo realizará uno de los 6 procesos (*proceso supervisor*), que se ejecutará en una de las máquinas (*máquina de referencia*). De esta manera, se calcularan la desviacion de los relojes respecto a una máquina de referencia.

## NTP

Se ejecutará 10 veces antes de realizar los accesos a la sección crítica y 10 veces despues de realizar los accesos a la sección crítica del proceso supervisor, para estimar el desplazamiento (offset) y retardo (delay) de cada máquina con respecto a la máquina de referencia.

Se tomará un par <o1,d1> al principio y otro par <o2,d2> al final, eligiendo en cada uno de ellos el par con menor delay. Mas adelante, se calculará la media de estos dos pares para obtener el par <o,d> definitivo.

La comprobación sólo admitirá la simultaneidad dentro de la sección crítica si el tiempo de colisión es inferior al error obtenido en la estimación del desvío de los relojes. Es decir, si tras aplicar las correciones de los desvios, no se obtienen colisiones en la sección crítica, no se habrá producido ninguna violación de la sección crítica.

## Resumen

El resumen de los requisitos que debe cumplir la práctica se resumen en la siguiente imagen:

![Teoria 2](https://github.com/rmelgo/DIS-Seccion-critica-distribuida/assets/145989723/01ec31ea-fe10-406b-b3f7-60aa2dcbdaec)

# - Estructura de los procesos

Cada uno de los 6 procesos tendrá aspectos tanto como de **servidor** como de **cliente** ya que enviará mensajes a otros procesos y recibirá mensajes de otros procesos (a traves de REST).

Para cumplir con los requisitos de la práctica, se ha planteado la siguiente estructura:

- Los procesos contarán una serie de métodos REST los cuales podrán ser accedidos por el resto de procesos (carácter de servicio).
- Un método run desde el cual se realizan distintas peticiones REST de forma que podrán crearse múltiples instancias o hilos (carácter de cliente).
  
Con esto se consigue otorgar a los procesos un carácter de servicio y un carácter de servidor.

Por una parte, en cada máquina se desplegará el proceso en 2 servidores Tomcat de manera que cada servidor Tomcat escuchará por un número de puerto diferente (del 8080 al 8085). 

Por otra parte, desde una máquina central o de referencia se ejecutarán 6 hilos del proceso, los cuales se conectarán con los 6 procesos servidores a través de las peticiones REST.

A través de este planteamiento, se tienen 6 procesos ejecutándose en 3 máquinas distintas. Además, todos los procesos pueden ser ejecutados desde una misma máquina siempre y cuando los procesos servidores estén correctamente desplegados en los servidores Tomcat de las distintas máquinas. 

En la siguiente imagen, se adjunta un esquema de la estructura de los procesos:

![Teoria 3](https://github.com/rmelgo/DIS-Seccion-critica-distribuida/assets/145989723/9950e3a0-cf6e-418d-b13f-12c2cdff0fc2)

# - Comentarios sobre el material adjuntado

El proyecto cuenta con los siguientes ficheros:

- Una carpeta llamada ***PracticaObligatoria*** que contiene un proyecto Java con la implementación de la sección crítica distribuida. Esta carpeta cuenta con los siguentes ficheros .java:

  - Un fichero llamado ***Servidor.java*** que incluye todo el codigo necesario para implementar el algortimo de Ricart y Agrawala así como la escritura en logs al entrar en la sección crítica y la fusión de los logs y corrección de tiempos por parte de la máquina principal o de referencia.
  - Un fichero llamado ***ServicioArranque.java*** que incluye el código necesario para crear los 6 hilos (clientes) los cuales se conectarán con los servidores a través de las peticiones REST.
  - Un fichero llamado ***Par.java*** que actua como modelo para almacenar un par de valores delay y offset.
  - Un fichero llamado ***Peticion.java*** que actua como modelo para almacenar los valores del tiempo e identificador de proceso, los cuales conforman una petición en el algoritmo de Ricart y Agrawala.
  - Un fichero llamado ***Comprobador.java*** que incluye el código necesario para recorrer el log fusionado de los accesos a la sección crítica distribuida y determinar si se han producido violaciones de la sección crítica.

- Un documento llamado ***Informe Práctica Obligatoria.pdf*** que contiene un documento en el que se detallan algunos aspectos de la estructura, la implementación y el funcionamiento del proyecto.

# - Configuración del proyecto

Aunque en el repositorio se adjunta una carpeta con el proyecto completo, generalmente no es posible abrir el proyecto desde Eclipse y ejecutarlo directamente. Debido a esto, se explicará en esta sección como crear el proyecto y configurarlo para poder ejecutarlo correctamente. Se deben seguir los siguientes pasos:

- **Paso 1**: Crear un Web Dynamic Proyect.

<p align="center">
  <img src="https://github.com/rmelgo/DIS-Seccion-critica-distribuida/assets/145989723/ae58b4fd-6ca2-4bfe-94db-b25417b852b4">
</p>

- **Paso 2**: Dentro del proyecto, acceder a "src/webapp/WEB-INF". Una vez se acceda a esta ruta, se debe incluir en esta carpeta un fichero llamado ***web.xml*** donde en la cuarta línea se debe incluir el nombre del proyecto creado en Eclipse.

El fichero ***web.xml*** también se adjunta en el repositorio dentro de la carpeta **Material configuración proyecto**.

![Entorno 2](https://github.com/rmelgo/DIS-Seccion-critica-distribuida/assets/145989723/fff57042-b3b7-4a0e-9c72-a0256b568159)

- **Paso 3**: Descargar la carpeta **jaxrs-ri** que se encuentra en el repositorio dentro de la carpeta **Material configuración proyecto**. Una vez se ha hecho esto, se debe pegar en la carpeta "src/webapp/WEB-INF/lib" los ficheros .jar de las carpetas **bin**, **lib** y **ext** que se encuentran dentro de la carpeta **jaxrs-ri**.
  
<p align="center">
  <img src="https://github.com/rmelgo/DIS-Seccion-critica-distribuida/assets/145989723/e781da71-fa59-4fd8-980e-cc32b0ff2905">
</p>

- **Paso 4**: Descargar la carpeta **apache-tomcat-8.5.87.zip** que se encuentra en el repositorio dentro de la carpeta **Material configuración proyecto**. Una vez se ha hecho esto, descomprimir la carpeta en cualquier ubicación.
- **Paso 5**: Crear un nuevo servidor Tomcat versión 8.5 en el proyecto de Eclipse, y asignarte la ruta de la carpeta apache-tomcat-8.5.87 descomprimida.

<p align="center">
  <img src="https://github.com/rmelgo/DIS-Seccion-critica-distribuida/assets/145989723/8e9f13d4-5ac3-4015-9309-fe49a1780454">
</p>

Por cada máquina crear 2 servidores Tomcat y asignarle a cada servidor un número de puerto (del 8080 al 8085).

- **Paso 6**: Añadir los ficheros .java al proyecto. Los ficheros .java se encuentran en el repositorio dentro de la carpeta **Material configuración proyecto**.

# - Despliegue del proyecto

Para desplegar el proyecto, se deben seguir los siguientes pasos:

- **Paso 1**: En cada máquina, que cuenta con 2 servidores Tomcat ejecutar ***Servidor.java*** en cada uno de ellos. De esta manera, se tienen ejecutando 6 procesos en 3 maquinas distintas.
- **Paso 2**: En la máquina de referencia, crear una nueva configuración en Eclipse. Esta configuración estará destinada a ejecutar el fichero ***ServicioArranque.java*** el cual creará los 6 hilos (clientes) los cuales se conectarán con los servidores a través de las peticiones REST. Para ejecutar ***ServicioArranque.java*** se deben pasar como parámetros, las direcciones IP de las 3 máquinas.

![Entorno 5](https://github.com/rmelgo/DIS-Seccion-critica-distribuida/assets/145989723/fbd40803-8dc8-4e9a-b52a-489880fbce06)
![Entorno 6](https://github.com/rmelgo/DIS-Seccion-critica-distribuida/assets/145989723/34a1fb00-560b-4107-8113-a78adc24abbb)

Nota: Cada uno debe configurar sus direcciones IP y no copiar las que aparecen en la imagen.

- **Paso 3**: Ejecutar ***ServicioArranque.java*** con la configuración realizada en el paso anterior y con los 6 servidores lanzando ***Servidor.java***.
   
# - Ejemplo de ejecución
