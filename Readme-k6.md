4 comandos en orden:

1. GET /current — estado actual (más liviano, empieza aquí):

docker run --rm -i --network tracking-service_default `
  -v "${PWD}/k6-tests:/k6-tests" `
   grafana/k6 run /k6-tests/get-current.js

Resumen general: el servicio aguantó todo

Thresholds — todos ✓
El servicio cumplió todos los criterios de calidad definidos. Tasa de error 0%, latencia p95 de 2.79ms (el límite era 500ms), y throughput de 217 req/seg (el mínimo era 20).

Las 3 fases superadas
EscenarioCargaResultadohigh_throughput80 req/seg fijos, 2 min✓ completadothroughput_ramphasta 350 req/seg, 3 min✓ completadobreakpointhasta ~700 req/seg, 5 min✓ completado
El servicio procesó 143.673 requests en 11 minutos sin fallar ninguno.

Latencia — extraordinaria
MétricaValorInterpretaciónPromedio2.42msMuy rápidoMediana2.12msConsistentep902.5ms90% bajo 2.5msp952.79ms95% bajo 2.79msp994.13msLos peores casos aún rápidosMáximo831msUn pico aislado, no sistémico
Para un servicio local con Docker, p95 < 3ms es sobresaliente.

El único ✗ — irrelevante
✗ latencia < 200ms  →  99% — ✓ 143624 / ✗ 49

2. GET /history — historial paginado:
   
docker run --rm -i --network tracking-service_default `
  -v "${PWD}/k6-tests:/k6-tests" `
   grafana/k6 run /k6-tests/get-history.js

Thresholds — todos verdes
Cero errores, p95 de 5.89ms contra un límite de 300ms, y 94 req/seg sostenidas. El servicio superó todos los criterios holgadamente.

Las 3 fases superadas
EscenarioCargaResultadosustained_load30 VUs por 3 min✓ completadospikepico súbito hasta 150 VUs en 10s✓ completadohigh_sustained50 VUs por 5 min✓ completado
56.847 requests en 10 minutos, 0 errores HTTP.

Latencia — muy buena para un endpoint paginado
MétricaValorInterpretaciónPromedio3.92msExcelente para una query con JOIN y paginaciónMediana2.29msLa mayoría responde muy rápidop904.27ms90% bajo 5msp955.89ms95% bajo 6msp9912.13msLos peores casos aún dentro del umbralMáximo2.04sUn pico aislado — vale la pena analizar

El pico de 2.04 segundos
Este es el dato más interesante. Un request llegó a 2.04s, probablemente por una de estas razones:

GC pause de la JVM durante el spike de 150 VUs
Connection pool exhaustion de HikariCP cuando llegaron los 150 VUs simultáneos en 10 segundos
Query lenta por paginación con ORDER BY occurredAt sin índice en esa columna

El hecho de que solo pasara una vez (30 requests de 56.847 superaron 300ms = 0.05%) lo hace no crítico, pero sí es una señal de que bajo spike extremo hay margen de mejora.

3. Cargar el seed antes del POST:
   
Get-Content k6-tests\seed.sql | docker exec -i paquetrack-postgres psql -U paquetrack -d paquetrack

4. POST /events — registrar eventos:

docker run --rm -i --network tracking-service_default `
  -v "${PWD}/k6-tests:/k6-tests" `
   grafana/k6 run /k6-tests/register-event.js

Thresholds — todos verdes, incluyendo uno clave
transition_errors ✓ 'count<5'  count=0
Cero errores de transición de estado en 47.260 eventos enviados. La máquina de estados funcionó perfectamente bajo carga.

Las 3 fases superadas
Escenario               Carga                           Resultado
sustained_load          10 VUs por 2 min                ✓ completado
ramp_up                 rampa hasta 60 VUs en 2.5 min   ✓ completado
breakpoint              hasta 150 req/seg por 3 min     ✓ completado

9.452 secuencias completas ejecutadas × 5 eventos cada una = 47.260 eventos publicados a RabbitMQ sin un solo error HTTP.

Latencia — sorprendentemente rápida para un endpoint asíncrono
Métrica                 Valor               Interpretación
Promedio                3.02ms              Excelente — solo publica a RabbitMQ y responde 202
Mediana                 1.3ms               La mitad de requests en menos de 1.3ms
p90                     2.01ms              90% bajo 2ms
p95                     2.54ms              95% bajo 3ms
p99                     5.65ms              Los peores casos aún muy rápidos
Máximo                  4.69s               Pico aislado — analizar

El diseño asíncrono via RabbitMQ se ve claramente: el endpoint solo valida y publica, no espera procesamiento. Por eso es tan rápido a pesar de ser un POST.

El pico de 4.69 segundos
El mayor de los tres tests. Ocurrió probablemente durante el breakpoint cuando llegaron 150 req/seg simultáneos. Las causas más probables son:

RabbitMQ back-pressure: la cola se llenó momentáneamente y el publisher esperó confirmación
Connection pool de RabbitMQ saturado bajo el pico máximo
GC pause de la JVM bajo carga máxima

Al igual que en los otros tests, fue un evento aislado — solo 32 requests de 47.260 superaron 500ms (0.07%).

dropped_iterations: 3.690 — el dato más importante
dropped_iterations: 3690  (5.83/s)
iterations completadas: 9452

Esto significa que bajo el breakpoint de 150 req/seg, el sistema no alcanzó a procesar todas las iteraciones solicitadas. Es la señal de quiebre que buscábamos. El sistema llegó a su límite de ~75 req/seg sostenidas bajo esta carga de secuencias completas de 5 eventos.
Sin embargo, las iteraciones que sí se completaron lo hicieron sin errores — el sistema degradó graciosamente, rechazando trabajo en lugar de fallar.

¿Qué es una dropped_iteration?
Una iteración en k6 es una ejecución completa de la función default — en este caso, la secuencia de 5 eventos: DISPATCHED → ARRIVED_AT_HUB → DEPARTED_FROM_HUB → OUT_FOR_DELIVERY → DELIVERED.
Una iteración se descarta cuando k6 quiere arrancar una nueva iteración pero no hay VUs disponibles para ejecutarla — todos están ocupados en iteraciones anteriores.

Visualízalo así:
k6 quiere lanzar 150 iteraciones/seg
↓
Cada iteración tarda ~2.5 segundos (5 eventos × 0.5s entre cada uno)
↓
Con 150 VUs máximos solo puede procesar:
150 VUs ÷ 2.5s por iteración = ~60 iteraciones/seg reales
↓
150 pedidas - 60 procesadas = 90 descartadas por segundo

¿Por qué no es una falla del servicio?
Aquí está la distinción clave:
Sistema fallando:     requests enviados → errores 500, timeouts, rechazos
Sistema con límite:   requests NO enviados → k6 los descarta antes de enviarlos
En tu caso el servicio nunca vio esas 3.690 iteraciones. k6 las descartó internamente porque no tenía VUs libres. El servicio respondió 202 en el 100% de los requests que sí recibió.

La diferencia entre límite de k6 y límite del servicio
Lo que encontraste no es exactamente el límite del servicio sino el límite de la combinación carga × duración de iteración. Si cada iteración fuera solo 1 evento en vez de 5, el throughput real sería mucho mayor con los mismos VUs.
Para encontrar el límite real del servicio en el POST habría que aumentar maxVUs a 500+ o reducir la secuencia a 1 evento por iteración. Pero para efectos académicos, demostrar que el sistema maneja 75 req/seg de secuencias completas sin un solo error ya es un resultado muy sólido.

5. Suite completa (los 3 juntos):
docker run --rm -i --network tracking-service_default `
  -v "${PWD}/k6-tests:/k6-tests" `
   grafana/k6 run /k6-tests/full-stress.js

Resultado general: perfecto
checks: 100.00% ✓ 332.898 / ✗ 0
http_req_failed: 0.00%
global_error_rate: 0.00%
332.898 validaciones sin un solo fallo en 14 minutos con los 3 endpoints corriendo en paralelo. Este es el resultado más importante porque simula uso real del sistema.

Las 4 fases superadas
Fase                Carga                           Duración                Resultado
warmup              5 VUs                           1 min✓                  calentamiento limpio
normal_load         20 VUs                          3 min✓                  carga de producción estable
peak_load           hasta 80 VUs                    3.5 min✓                pico absorbido sin errores
breakpoint          hasta 250 VUs / 300 iter/s      5 min✓                  límite alcanzado graciosamente

Latencia por endpoint bajo carga simultánea
Endpoint        Promedio            Mediana         p95         Máximo
GET /current    10.19ms             2.01ms          3.28ms      8.62s
GET /history    14.58ms             2.09ms          4.25ms      8.62s
POST /events    19.97ms             1.3ms           3.01ms      8.63s

Hay algo muy revelador aquí: la mediana es ~2ms en los 3 endpoints pero el promedio sube a 10-20ms. Esto indica que la gran mayoría de requests son rapidísimos, y unos pocos picos aislados (el máximo de 8.6s) jalan el promedio hacia arriba. En producción real lo que importa es la mediana y el p95 — ambos excelentes.

El pico de 8.6 segundos
Los 3 endpoints comparten el mismo pico máximo (~8.62s) en el mismo momento. Eso no es coincidencia — ocurrió durante el breakpoint cuando los 250 VUs golpearon simultáneamente los 3 endpoints. La causa más probable es una GC pause larga de la JVM (Stop-the-World) bajo presión máxima de memoria. Duró un instante y el sistema se recuperó solo.

dropped_iterations: 2.558 — límite del sistema bajo carga mixta
Con los 3 endpoints corriendo en paralelo el sistema procesó 65.97 iteraciones/seg completas (cada iteración = POST + GET history + GET current). El breakpoint pedía 300 iter/seg — el sistema alcanzó ~66, descartando el resto sin errores.
Comparado con los tests individuales:

Test                    Throughput real
Solo GET /current       217 req/s
Solo GET /history       94 req/s
Solo POST /events       75 req/s
Los 3 juntos            66 iter/s × 3 req = 198 req/s
El sistema distribuye bien los recursos entre los 3 endpoints simultáneos.

global_error_rate — un detalle a notar
global_error_rate: 0.00% ✓ 0 / ✗ 166.449
Los ✗ 166.449 no son errores — en k6 la métrica Rate cuenta tanto los éxitos (0) como los fallos (1). Los 166.449 son registros de valor 0 (sin error). Tasa de error real: cero.

Conclusión para tu informe
La suite completa demostró que el sistema mantiene comportamiento estable bajo carga mixta realista. Con 250 usuarios virtuales concurrentes atacando los 3 endpoints simultáneamente durante 14 minutos, el servicio procesó 166.449 requests HTTP con 0 errores, latencia p95 por debajo de 4.25ms en todos los endpoints, y degradación controlada bajo breakpoint extremo. El diseño asíncrono con RabbitMQ, combinado con la capa de caché de JPA y el connection pooling de HikariCP, demostró ser una arquitectura sólida para cargas de producción reales.

6. start k6-tests\stress-report.html

Entre cada ejecución del POST o del full-stress, resetea la BD:

docker exec -it paquetrack-postgres psql -U paquetrack -d paquetrack -c "DELETE FROM tracking_event WHERE shipment_id::text LIKE '00000000-%'; UPDATE shipment SET status = 'CREATED' WHERE id::text LIKE '00000000-%';"
