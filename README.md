# Implementación de una sección crítica distribuida a través del algortimo de Ricart y Agrawala

![Inicial](https://github.com/rmelgo/DIS-Seccion-critica-distribuida/assets/145989723/0efba7f9-a8e4-4a5e-b7a2-7f6d2f9e2a7c)

# - Introducción

Proyecto realizado en la asignatura de Sistemas Distribuidos del grado de Ingenieria Informática de la Universidad de Salamanca. El enunciado del proyecto se encuentra subido en el repositorio en un archivo PDF llamado <a href="https://github.com/rmelgo/ANIM-Juego-plataformas-Unity/blob/main/Enunciado%20Unity.pdf" target="_blank">*PracticaObligatoria.pdf*</a>.

El principal objetivo de este proyecto es la realización de un programa en Java que implemente el algoritmo de Ricart y Agrawala para regular el acceso a una zona de exclusión mutua la cual sera compartida por varios procesos ubicados en máquinas distintas.
Para implementar dicho algoritmo, será necesario implementar un mecanismo NTP para calcular el desplazamiento de los relojes respecto a una maquina de referencia.
Esta maquina de referencia se encarga registrar los accesos a la sección crítica y comprobar que nos se han producido violaciones en la sección crítica distribuida.

# - Comentarios sobre el entorno de ejecución

Para ejecutar este programa, se puede hacer uso de cualquier Sistema Operativo, pero será necesario la utilización de la herramienta ***Eclipse***.    












# - Parámetros de ejecución

Para ejecutar el programa es necesario proporcionar 2 argumentos. 

El primer argumento se trata del **número máximo de procesos** (peatones y coches) que pueden existir de manera simultanea en el cruce. Este primer argumento puede tomar valores del ***3 al 49***, en caso contrario la practica no funcionará.

El segundo argumento hace referencia a la **velocidad** con la que se desplazaran los peatones y los coches. Este argumento podrá tomar valores del ***0 al 40*** de forma que la velocidad del programa será mas rápida cuanto mas pequeño sea el número introducido.  
Si el segundo argumento es 0, el programa se ejecutará a ***máxima velocidad***, lo que implicará un alto consumo de CPU.

Si los parámetros introducidos no respetan las reglas anteriores, el programa lo detectará, informará al usuario y acabará.

# Comentarios sobre el material adjuntado

El proyecto cuenta con los siguientes ficheros:

- Un fichero llamado ***cruce.c*** que contendrá el código necesario para poner en marcha la infraestructura del cruce, leer y interpretar los argumentos introducidos y crear los peatones y coches correspondientes.
- Un fichero llamado ***libcruce.a*** el cual se trata de una biblioteca estática de funciones que contendrá una serie de funciones para gestionar la creación y finalización del cruce así como la gestión del valor de los semáforos creación y movimiento de peatones y coches.
- Un fichero llamado ***cruce.h*** que contiene las cabeceras de las funciones definidas en la biblioteca *libcruce.a* así como una serie de macros que utiliza la biblioteca.

# - Funciones de la biblioteca estática de funciones ***libcruce.a***

La biblioteca estática de funciones ***libcruce.a*** cuenta con las siguientes funciones:

- **int CRUCE_inicio(int ret, int maxProcs, int semAforos, char * zona)**: Esta función deberá ser ejecutada por el proceso principal despues de crear los mecanismos IPC correspodientes y antes de haber creado algún peatón o coche. Esta función recibe los siguientes parámetros:
  
  - **ret**: Número entero que hace referencia a la velocidad de representación de los peatones y coches en el cruce (segundo argumento pasado en la línea de órdenes).
  - **maxProcs**: Número entero que hace referencia al número máximo de procesos (peatones y coches) que pueden existir de manera simultanea en el cruce (primer argumento pasado en la línea de órdenes).
  - **semAforos**: Número entero que hace referencia al identificador de un conjunto o array de semáforos que se deberá haber creado previamente donde el primer semáforo será utilizado por la biblioteca y el resto pueden ser utilizados para realizar tareas de sincronización.
  - **zona**: Puntero a una zona de memoria compartida que se deberá haber creado previamente donde los primeros 256 bytes serán utilizados por la biblioteca mientras el resto pueden ser utilizados para realizar tareas de sincronización.
 
- **int CRUCE_pon_semAforo(int sem, int color)**: Esta función pone el semáforo pasado en el primer parámetro al color indicado en el segundo parámetro. Esta función recibe los siguientes parámetros:

  - **sem**: Número entero que hace referencia a un semáforo del cruce. Este parámetro puede tomar los valores *SEM_P1*, *SEM_P2*, *SEM_C1*, *SEM_C2* dependiendo del semáforo al que se desea cambiar el color.
  - **color**: Número entero que hace referencia al color al que se desea cambiar el semáforo pasado como primer parámetro. Este parámetro puede tomar los valores *ROJO*, *AMARILLO* (solamente para los semaforos de los coches) y *VERDE*.
 
Nota: *SEM_P1*, *SEM_P2*, *SEM_C1*, *SEM_C2*, *ROJO*, *AMARILLO* y *VERDE* son macros definidas en ***cruce.h***.

- **int CRUCE_nuevo_proceso(void)**: Esta función crea un nuevo proceso que puede ser un coche o un peatón. Esta función devuelve *COCHE* en el caso de que el proceso creado se trate de un coche y *PEAToN* en el caso de que el proceso creado se trate de un peatón.

Nota: *COCHE* y *PEAToN* son macros definidas en ***cruce.h***.

- **struct posiciOn CRUCE_inicio_coche(void)**: Cada vez que se crea un nuevo proceso de tipo *COCHE*, debe llamar a esta función. Esta función devuelve las coordenadas de la posición siguiente que ocupará el coche a través de un tipo de datos llamado *posiciOn*.

Nota: *posiciOn* es un tipo de datos definido en ***cruce.h*** y cuenta con dos enteros que hacen referencia a la coordenada x y a la coordenada y.

- **struct posiciOn CRUCE_inicio_peatOn_ext(struct posiciOn *posNacimiento)**: Cada vez que se crea un nuevo proceso de tipo *PEAToN*, debe llamar a esta función. Esta función devuelve las coordenadas de la posición siguiente que ocupará el peatón a través de un tipo de datos llamado *posiciOn*. Esta función cuenta con un único parámetro en el cual se devolverá por referencia la posición actual que ocupa el peatón a través de un tipo de datos llamado *posiciOn*.

- **struct posiciOn CRUCE_avanzar_coche(struct posiciOn sgte)**: Esta función debe ser llamada por cada coche despues de haber ejecutado ***CRUCE_inicio_coche***. Esta función recibe como parámetro la posición a la que desea ir el coche y devuelve la nueva posición siguiente que ocupará el coche.

- **struct posiciOn CRUCE_avanzar_peatOn(struct posiciOn sgte)**: Esta función debe ser llamada por cada peatón despues de haber ejecutado ***CRUCE_inicio_peatOn_ext***. Esta función recibe como parámetro la posición a la que desea ir el peatón y devuelve la nueva posición siguiente que ocupará el peatón.

- **int pausa_coche(void)**: Esta función debe ser llamada por cada coche cada vez que se haya producido un avance mediante la función ***CRUCE_avanzar_coche***.

- **int pausa(void)**: Esta función debe ser llamada por cada peatón cada vez que se haya producido un avance mediante la función ***CRUCE_avanzar_peatOn***.

- **int CRUCE_fin_coche(void)**: Cuando un coche tenga en la posición siguiente un valor negativo para la coordenada y, deberá llamar a esta función para finalizar el proceso coche de manera correcta.

- **int CRUCE_fin_peatOn(void)**: Cuando un peatón tenga en la posición siguiente un valor negativo para la coordenada y, deberá llamar a esta función para finalizar el proceso peatón de manera correcta.

- **int CRUCE_fin(void)**: Cuando la ejecución del cruce haya finalizado, el proceso principal debe ejecutar esta función. Esta función se encargará de finalziar la representación del cruce y limpiar todos los procesos y mecanismos IPC que estuvieran en uso.

- **void pon_error(char *mensaje)**: Esta función coloca el mensaje pasado como parámetro en un recuadro y espera a que el usuario pulse Intro para seguir con la ejecución del programa.

# - Ciclo semafórico

Como se puede observar en la siguiente imagen, el cruce cuenta con 4 semáforos, 2 para coches y 2 para peatones.

![esquemacruce](https://github.com/rmelgo/SSOO-II-Programacion-multiproceso-cruce-semaforos/assets/145989723/75191847-4f28-4036-8468-459646b98f73)

De esta manera, el ciclo semafórico del cruce estará compuesto por 3 fases:

- **Fase 1**: El semáforo *C1* y *P2* se encontrarán en verde mientras que el resto de semáforos se encontrarán en rojo. La duración de esta fase será de *6 pausas* (una pausa equivale a una llamada a la funcion *pausa*).
- **Fase 2**: El semáforo *C2* se encontrará en verde mientras que el resto de semáforos se encontrarán en rojo. La duración de esta fase será de *8 pausas*.
- **Fase 3**: El semáforo *P1* se encontrará en verde mientras que el resto de semáforos se encontrarán en rojo. La duración de esta fase será de *12 pausas*.

Para cambiar de una fase a otra, existirá una pequeña fase de transición donde el color de los semáforos de los coches se encontrará en amarillo. Esta fase de transición durará en principio 2 pausas aunque esta fase puede extenderse hasta que los cruces se encuentren libres para así evitar posibles colisiones entre peatones y coches. 
  
# - Pasos necesarios para ejecutar el programa

**Paso 1: Compilar el programa**  

Para ello se debe introducir el siguiente comando:    

```gcc cruce.c libcruce.a -o cruce```

Como la libreria *libcruce.a* esta diseñada para sistemas de 32 bits y no es posible mezclar código de 64 bits con codigo de 32 bits, será necesario incluir una nueva directiva en el comando gcc para que genere codigo en 32 bits compatible con la biblioteca. De esta manera, el comando necesario para compilar el programa es el siguiente:

```gcc -m32 cruce.c libcruce.a -o cruce```

Si se produce algún tipo de error al realizar la compilación será por que el sistema donde se ejecuta el programa no tiene las librerias de 32 bits necesarias. Para incluirlas se deberá ejecutar el siguyiente comando:

```sudo apt-get install gcc-multilib```

Tras ejecutar este comando, se generará un fichero ejecutable llamado *cruce*. Observese como es necesario tanto el fichero *cruce.c* como la biblioteca estática de funciones *libcruce.a* para generar el ejecutable.

**Paso 2: Ejecutar el programa**  

Para ello se debe introducir el siguiente comando:    

```./cruce <número-máximo-procesos> <velocidad>```

Tras ejecutar este comando, el programa se habra ejecutado correctamente, siempre y cuendo se hayan introducido los argumentos correspondientes.

**Finalización del programa**

Para finalizar la ejecución del programa simplemente bastara con pulsar las teclas CTRL+C desde el terminal. De esta manera, el programa automaticamente recibira una señal de terminación por lo que realizará las tareas necsarias para finalizar el cruce y liberar todos los recursos utilizados.

# - Ejemplo de ejecución

## Ejecución a velocidad media (10) y máximo de 4 procesos

En la siguiente imagen, se muestra un ejemplo del uso y funcionamiento del programa con un límite de 4 procesos y una velocidad de 10:    

![Ejemplo ejecucion 1](https://github.com/rmelgo/SSOO-II-Programacion-multiproceso-cruce-semaforos/assets/145989723/a72bece7-157e-4224-93f6-48f9a7595866)

## Ejecución a velocidad máxima y máximo de 49 procesos

En la siguiente imagen, se muestra un ejemplo del uso y funcionamiento del programa con un límite de 49 procesos y una velocidad de 0:    

![Ejemplo ejecucion 2](https://github.com/rmelgo/SSOO-II-Programacion-multiproceso-cruce-semaforos/assets/145989723/cbe17750-07ad-44d2-91ac-cbf5288b5e08)
