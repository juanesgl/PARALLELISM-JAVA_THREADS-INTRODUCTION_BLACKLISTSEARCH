# Parte 2 - BlackListSearch paralelizado

---

## ¿Qué se hizo?

Se refactorizó el componente de validación de direcciones IP en listas negras para que aproveche la capacidad multi-núcleo de la CPU. Originalmente, el método `checkHost()` recorría las **80.000 listas negras registradas de forma secuencial** (una por una). Ahora, ese mismo recorrido se divide entre **N hilos de trabajo** que buscan al mismo tiempo en segmentos distintos de las listas, reduciendo drásticamente el tiempo de espera.

Para lograrlo se implementaron los siguientes cambios:

### 1. Nueva clase `BlackListThread`

Se creó una clase que extiende de `Thread` y representa el ciclo de vida de un hilo de búsqueda:

```java
public class BlackListThread extends Thread {
    private int startIndex;   // índice inicial del segmento (inclusivo)
    private int endIndex;     // índice final del segmento (exclusivo)
    private String ipAddress; // IP a buscar
    private HostBlacklistsDataSourceFacade skds; // fachada de consulta

    private LinkedList<Integer> blackListOccurrences;
    private int occurrencesCount;
    private int checkedListsCount;
    ...
}
```

**¿Qué hace cada parte?**

- **Constructor**: recibe el segmento de listas que le toca revisar `[startIndex, endIndex)`, la dirección IP a buscar y la fachada de consulta (que es Thread-Safe, es decir, puede ser usada por varios hilos al mismo tiempo sin problemas).
- **Método `run()`**: recorre su segmento asignado y pregunta a la fachada si la IP está reportada en cada lista. Si está, guarda el número de esa lista.
- **Métodos getter**: permiten "preguntarle" al hilo después de que termine cuántas ocurrencias encontró, cuáles fueron las listas, y cuántas listas alcanzó a revisar.

### 2. Modificación de `HostBlackListsValidator.checkHost()`

Se agregó una sobrecarga del método con un segundo parámetro: `checkHost(String ipaddress, int N)`, donde **N es el número de hilos** entre los que se divide la búsqueda. El método original ahora simplemente llama al nuevo con N=1 (manteniendo la compatibilidad).

El nuevo método funciona así:

**Paso 1 - División del espacio de búsqueda:**
```
serversPerThread = totalServers / N
residue         = totalServers % N
```
Se calcula cuántas listas le tocan a cada hilo. Si N no divide exactamente el total (por ejemplo 80.000 listas entre 3 hilos), el residuo se le asigna al **último hilo**, que revisa hasta el final del rango.

**Paso 2 - Creación e inicio de los hilos:**
Cada hilo recibe su segmento y se lanzan todos con `start()`. Como `start()` crea hilos reales, todos los segmentos se empiezan a buscar **al mismo tiempo**.

**Paso 3 - Espera con `join()`:**
```
for (BlackListThread thread : threads) {
    thread.join();
}
```
`join()` le dice al hilo principal: "espérame hasta que yo termine". Así el programa espera a que TODOS los hilos terminen su segmento antes de continuar. Sin esto, intentaríamos leer resultados que aún no existen.

**Paso 4 - Recolección de resultados:**
Se suman las ocurrencias de todos los hilos, se unen las listas de cada uno en una sola, y se suma cuántas listas revisó cada hilo para tener el total real.

**Paso 5 - Reporte final:**
Con el total de ocurrencias se decide si la IP es confiable o no confiable (se considera "no confiable" si está en al menos 5 listas, la constante `BLACK_LIST_ALARM_COUNT`), y se registra el LOG con la cantidad real de listas revisadas.

### 3. Modificación de `Main.java`

Ahora el programa:
- Recibe el número de hilos como argumento de la línea de comandos (ej: `Main 8`).
- Mide el tiempo de ejecución con `System.currentTimeMillis()` antes y después de la búsqueda.
- Muestra la IP verificada, los hilos usados, las listas donde se encontró y el tiempo total en milisegundos.

---

## ¿Por qué se hizo así?

1. **El problema es "vergonzosamente paralelo"**: la búsqueda de una IP en una lista negra no depende de las demás listas. Cada consulta es independiente, así que no hay ningún problema en hacerlas simultáneamente.

2. **Dividir el problema en segmentos** evita que dos hilos revisen la misma lista (no hay trabajo duplicado) y garantiza que entre todos cubran el total de listas exactamente una vez.

3. **`join()` es obligatorio** para la sincronización: el hilo principal debe esperar a que todos terminen para poder recolectar los resultados. Es la forma más sencilla de coordinar hilos en Java.

4. **El residuo va al último hilo** porque así los demás hilos tienen segmentos del mismo tamaño y terminan "al mismo tiempo", evitando que un hilo esté ocioso mientras otro trabaja de más.

---

## ¿Por qué hay que esperar tanto con 1 hilo? (El tiempo de espera)

Al ejecutar el programa puede parecer que se "congela", pero **no es un error**. La causa es la fachada `HostBlacklistsDataSourceFacade`:

- Este componente simula la latencia de consultar servidores externos reales: internamente tiene una pausa (`Thread.sleep`) en cada consulta a una lista negra, como si estuviera enviando una petición por la red.
- El código de la fachada **NO ES MODIFICABLE** (el propio archivo lo indica con el comentario "NO TOCAR ESTE CODIGO!!"), ya que es parte del diseño del laboratorio.
- Hay **80.000 listas negras** registradas. Con **1 solo hilo**, el programa hace las 80.000 consultas una tras otra, cada una con su latencia simulada → la espera es larga (puede ser de más de un minuto).
- Con **más hilos**, las 80.000 consultas se reparten: por ejemplo con 8 hilos, cada uno hace solo 10.000 consultas al mismo tiempo → el tiempo baja casi 8 veces.

Esa demora no es un bug: **es justamente la evidencia de que el paralelismo sirve**, y es lo que se mide en la Parte 3 (tiempo de solución vs. número de hilos).

---

## ¿Cómo se verifica que funciona?

Con este comando se ejecuta la búsqueda con N hilos (por ejemplo 8):

```
java -cp target/classes edu.eci.arsw.blacklistvalidator.Main 8
```

Salida esperada:

```
================================================
Checking IP: 200.24.34.55
Number of threads: 8
================================================
INFO: HOST 200.24.34.55 Reported as NOT trustworthy
The host was found in the following blacklists: [23, 50, 200, 500, 1000]
Execution time: 15639 ms
INFO: Checked Black Lists:80.000 of 80.000
================================================
```

Se puede probar con distintos números de hilos (1, 4, 8, 50, 100) y comparar el `Execution time` de cada uno.

---

## Resumen técnico (en simple)

| Concepto | Explicación sencilla |
|---|---|
| `BlackListThread` | Un "trabajador" que revisa solo su parte de las listas negras |
| `checkHost(ip, N)` | Divide las 80.000 listas en N partes y lanza N trabajadores |
| `join()` | El programa espera a que todos los trabajadores terminen antes de juntar resultados |
| Residuo | Si la división no es exacta, el último trabajador revisa los servidores sobrantes |
| `Thread.sleep` en la fachada | Latencia simulada de la "red"; es lo que hace lento el modo de 1 solo hilo |
| `Execution time` | Tiempo que tardó toda la búsqueda; debe bajar al aumentar los hilos |
