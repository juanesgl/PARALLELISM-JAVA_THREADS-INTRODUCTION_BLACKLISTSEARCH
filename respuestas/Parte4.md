# Parte 4 - Ejercicio Black List Search

Para respaldar el siguiente análisis, se ejecutó el programa variando la cantidad de hilos concurrentes. Los tiempos de ejecución obtenidos fueron:

*   **1 Hilo (Secuencial):** 468.655 ms
*   **Tantos hilos como núcleos ($N$):** 22.238 ms
*   **Doble de núcleos ($2N$):** 53.858 ms
*   **50 hilos:** 2.495 ms
*   **100 hilos:** 1.342 ms

---

**1. Según la ley de Amdahl, donde S(n) es el mejoramiento teórico del desempeño, P la fracción paralelizable del algoritmo, y n el número de hilos, a mayor n, mayor debería ser dicha mejora. ¿Por qué el mejor desempeño no se logra con los 500 hilos? ¿Cómo se compara este desempeño cuando se usan 200?**

El mejor desempeño no se logra con 500 hilos debido a que la Ley de Amdahl es un modelo teórico matemático que asume que crear y gestionar hilos no tiene ningún costo. En la realidad física del hardware, existe el **Cambio de Contexto (*Context Switching*)**.

Al crear 500 hilos en un procesador con un número limitado de núcleos físicos, el Sistema Operativo se ve obligado a pausar, guardar el estado, cargar el estado de otro hilo y reanudar la ejecución miles de veces por segundo. Se llega a un punto de saturación donde la CPU gasta más tiempo y recursos administrando el tráfico de los hilos que ejecutando el código real. Además, realizar 500 peticiones de red simultáneas desde una sola máquina puede saturar los límites de conexiones TCP locales o provocar rechazos por parte del servidor (*Rate Limiting* / Anti-DDoS).

Al comparar esto con **200 hilos**, el desempeño suele ser mejor (o mantenerse estable en su punto más óptimo) porque no se ha superado tan drásticamente el umbral de saturación de recursos. El sobrecosto (*overhead*) de administración del Sistema Operativo sigue siendo manejable y la red aún puede despachar las peticiones sin colapsar.

---

**2. ¿Cómo se comporta la solución usando tantos hilos de procesamiento como núcleos comparado con el resultado de usar el doble de éste?**

Analizando los tiempos reales, ocurre una anomalía interesante:
*   **Núcleos ($N$):** 22.238 ms
*   **Doble de núcleos ($2N$):** 53.858 ms

Para un problema que depende de la red (*I/O-bound*) como la consulta a servidores externos, teóricamente usar el doble de hilos ($2N$) debería ser más rápido. Esto se debe a que, cuando un hilo se bloquea esperando la respuesta de internet, el Sistema Operativo le cedería el núcleo a un hilo extra para enviar otra petición, aprovechando los tiempos muertos.

Sin embargo, en la prueba real **el tiempo con $2N$ fue más del doble de lento**. Esto se explica por factores ambientales de ejecución:
1.  **Inconsistencia de red (Latencia):** Es muy probable que durante la ejecución de $2N$ haya ocurrido un pico de latencia en internet o que el servidor de las listas negras haya tardado más en responder.
2.  **Mantenimiento de la JVM:** Si la prueba se ejecutó justo después de otra, la Máquina Virtual de Java pudo haber pausado los hilos momentáneamente para que el Recolector de Basura (*Garbage Collector*) limpiara la memoria, degradando el tiempo de esa ejecución en particular.

---

**3. De acuerdo con lo anterior, si para este problema en lugar de 100 hilos en una sola CPU se pudiera usar 1 hilo en cada una de 100 máquinas hipotéticas, ¿la ley de Amdahl se aplicaría mejor? Si en lugar de esto se usaran $c$ hilos en $100/c$ máquinas distribuidas (siendo $c$ el número de núcleos de dichas máquinas), ¿se mejoraría? Explique su respuesta.**

**Sobre usar 1 hilo en 100 máquinas:**
Sí, la Ley de Amdahl se aplicaría mucho mejor y se acercaría más a su límite teórico perfecto. En una sola máquina, los 100 hilos compiten por cuellos de botella físicos compartidos (el ancho de banda de la tarjeta de red, la memoria RAM y la caché del procesador). Al escalar horizontalmente con 100 máquinas, se eliminan estos embudos: cada hilo cuenta con su propia CPU al 100% y su propia tarjeta de red dedicada.

**Sobre usar $c$ hilos en $100/c$ máquinas distribuidas:**
Sí, esta sería **la arquitectura más óptima** y la que mejor rendimiento entregaría en la vida real.
Asumiendo máquinas de 4 núcleos ($c=4$), tendríamos 25 máquinas ejecutando 4 hilos cada una. Esta topología híbrida mejora el desempeño por dos razones fundamentales:
1.  **Maximiza el hardware local:** Al no superar el límite de núcleos físicos por máquina (4 hilos para 4 núcleos), se elimina por completo el penalizador por cambio de contexto (*context switching*). La CPU trabaja al 100% sin sobrecarga de administración.
2.  **Optimiza la red distribuida:** Se reparte la carga de internet entre 25 tarjetas de red distintas (evitando saturar un solo canal local), y al mismo tiempo requiere mucho menos tráfico interno para coordinar y agrupar los resultados que si se tuvieran que sincronizar 100 máquinas completamente independientes.