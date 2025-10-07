# Calc21 — Calculadora educativa (Java 21)

## Índice

1. [Detalles e instrucciones sobre el código](#detalles-e-instrucciones-sobre-el-c%C3%B3digo)
2. [Soluciones a los retos propuestos por el profesor](#soluciones-a-los-retos-propuestos-por-el-profesor)

    - [Retos de comprensión](#retos-de-comprensi%C3%B3n)
    - [Retos de depuración](#retos-de-depuraci%C3%B3n)
    - [Retos de modificación](#retos-de-modificaci%C3%B3n)
    - [Retos de predicción](#retos-de-predicci%C3%B3n)
    - [Retos de diseño](#retos-de-dise%C3%B1o)
3. [Solución a un reto propuests por los alumnos]()

---

## Detalles e instrucciones sobre el código 

- Expresiones: `+ - * / ^`, paréntesis, funciones `sin(x)`, `cos(x)`.
- Arquitectura por capas: Lexer → Parser → AST → Evaluator → REPL.
- Java moderno: records + sealed, switch con pattern matching, text blocks.
- Ejecutar:
  ```bash
  ./gradlew run
  ```
- Tests:
  ```bash
  ./gradlew test
  ```
> Si falta el wrapper JAR: `gradle wrapper --gradle-version 8.9` (una vez).

---

## Soluciones a los retos propuestos por el profesor

### Retos de comprensión

- Explica qué es un Token y da un ejemplo de cómo aparece en la expresión `3 + 5 * 2`.
 
- ¿Qué diferencia hay entre el Lexer y el Parser?  
- ¿Qué significa que el parser sea recursivo? Pon un ejemplo de función que lo demuestra.
---
### Retos de depuración    

- ¿Qué devuelve la calculadora con la entrada: `2 + 3 * 4` y por qué?

Cuando se introduce la expresión ``2 + 3 * 4``, la calculadora empieza leyendo el texto carácter por carácter. El analizador léxico (``lexer``) convierte cada elemento en un token: primero el número 2, luego el signo ``+``, después el número 3, el operador ``*``, el número 4 y finalmente un token de fin de entrada (``EOF``). Esta etapa sirve para que el parser trabaje con unidades reconocibles y no con simples caracteres.

**Entrada**: ``2 + 3 * 4``

**Tokens generados**:
``[2] [+] [3] [*] [4] [EOF]``

Después, el parser analiza esos tokens y construye una estructura jerárquica que representa la expresión. No se limita a leer de izquierda a derecha, sino que respeta la precedencia de operadores. En este caso, detecta que la multiplicación tiene prioridad sobre la suma, por lo que primero agrupa ``3 * 4`` y luego añade el ``+ 2`` al principio. El árbol resultante coloca la suma como operación principal y la multiplicación como una rama dentro de ella.

          (+)
         /   \
        (2)  (*)
        / \
      (3) (4)

Durante la evaluación, el intérprete resuelve primero el subárbol de la multiplicación, obteniendo ``3 * 4 = 12``, y a continuación aplica la suma superior: ``2 + 12 = 14``. Por eso, el valor final de la expresión es ``14.0``.

Este comportamiento se debe a que el parser está construido siguiendo las reglas clásicas de precedencia matemática, organizando la interpretación mediante las funciones ``expr()``, ``term()`` y ``factor()``.

- ¿Qué ocurre si escribimos una expresión no válida como `2 + *`? ¿Cómo reacciona el código?

Cuando se introduce la expresión ``2 + *``, la calculadora comienza leyendo el texto carácter por carácter y el lexer convierte cada símbolo en un token: primero 2 como ``NUMBER``, luego ``+`` como ``PLUS`` y finalmente ``*`` como ``STAR``, completando con un token ``EOF``.

**Entrada**: ``2 + *``

**Tokens generados**:
``[2] [+] [*] [EOF]``

El problema surge cuando el parser intenta construir el árbol de expresión. Tras procesar el ``+``, espera encontrar un número, un paréntesis o una función como siguiente operando, pero encuentra el token ``STAR (*)``. Dado que no hay ninguna producción válida en la función ``primary()`` que permita un operador en esa posición, el parser lanza una excepción. Concretamente, se ejecuta:

```java
    throw error("Token inesperado: " + peek().type() + " en pos " + peek().position());
```

Esto genera un IllegalArgumentException con el mensaje:

```java
    Token inesperado: STAR en pos 4
```

En el ``main()``, este error es capturado por el bloque:

```java
    catch (IllegalArgumentException ex) {
        System.err.println("Error: " + ex.getMessage());
    }
```

Y se muestra en consola:

```java
    Error: Token inesperado: STAR en pos 4
```

No se realiza ninguna evaluación de la expresión y no se devuelve ningún resultado numérico.

El motivo es que el parser asume que los tokens siguen una secuencia válida (operando → operador → operando). Al encontrar dos operadores consecutivos (``+`` seguido de ``*``) sin un operando intermedio, detecta la inconsistencia y detiene la ejecución lanzando la excepción para evitar interpretaciones ambiguas o incorrectas.

- ¿Qué devuelve si calculamos `(2 + 3) ^ 2`

Cuando se introduce la expresión ``(2 + 3) ^ 2``, la calculadora comienza leyendo carácter por carácter. El lexer convierte cada elemento en un token: un paréntesis izquierdo ``(`` como ``LPAREN``, ``2`` como ``NUMBER``, ``+`` como ``PLUS``, ``3`` como ``NUMBER``, paréntesis derecho ``)`` como ``RPAREN``, el operador ``^`` como ``CARET``, ``2`` como ``NUMBER`` y finalmente ``EOF``.

**Entrada**: ``(2 + 3) ^ 2``

**Tokens generados**:
``[LPAREN] [2] [+] [3] [RPAREN] [CARET] [2] [EOF]``

El parser analiza los tokens y construye un árbol de expresión (``AST``) que refleja la precedencia de operadores y los paréntesis:

           (^)
          /   \
        (+)    (2)
       /   \
     (2)   (3)

El subárbol de la suma ``(2 + 3)`` se evalúa primero, dando 5, y después se aplica la potencia con ``Math.pow(5, 2)``, que resulta en 25.

El evaluador recorre el árbol llamando a ``Evaluator.eval()`` en cada nodo: primero calcula el lado izquierdo del ``^`` sumando ``2 + 3``, luego eleva el resultado a la potencia 2.

**Resultado final**: ``25.0``

El motivo de este comportamiento es que el parser y el evaluador respetan la precedencia de operadores, considerando que la potencia (``^``) tiene mayor prioridad que la suma fuera de paréntesis y que los paréntesis fuerzan la evaluación de la suma antes de aplicar la potencia.

---
### Retos de modificación    

- Añade la función `tan(x)` usando el mismo esquema que `sin` y `cos`. 
- Modifica el parser para que acepte también la función `sqrt(x)`.
- Haz que la calculadora acepte números negativos explícitos como `-5 + 3`.
---
### Retos de predicción   

- ¿Qué resultado debería devolver esta expresión y por qué?: `cos(0) + sin(90)` (recuerda: las funciones trigonométricas usan radianes).
  
  El resultado obtenido a la hora de realizar es  `cos(0) + sin(90) = 1.8939966636` dado que en el programa no se convierte el sin(90º) sino que lo realiza por medio del sin(90 radianes),dando asi a que el resultado de realizar la operacion no sea exactamente 1.
  
  siendo:    
  cos(0) = 1    
  sin(90) = 0.8939966636    
- ¿Cuál es el resultado de `2 ^ 3 ^ 2`? Explica por qué según la precedencia implementada.
  
  El resultado a la hora de realizar la operacion es  `2 ^ 3 ^ 2 = 512.0` dado a que el exponente '^' es asociativo a la derecha tenemos que: `2 ^ 3 ^ 2 = 2 ^ (3 ^ 2) = 2 ^ 9 = 512.0`
  
- ¿Qué devuelve la calculadora con `(2 + 3) * (4 + 5)`?

  El resultado a la hora de realizar la operacion es  `(2 + 3) * (4 + 5) = 45.0` debido a la propiedad que tienen las matematicas de que las operaciones realizadas dentro de los paretesis se realizan con anterioridad dando que : `(2 + 3) * (4 + 5) = (5) * (9) = 45.0`
---
  ### Retos de diseño    

- El código actual usa un parser recursivo. ¿Qué ventaja tiene frente a procesar los tokens con un bucle y pila manual?

  - Esto hace que el código sea más claro, más fácil de entender y mantener, ya que sigue directamente la estructura de la gramática, en cambio, con un bucle y una pila manual habría que gestionar explícitamente el orden y jerarquía de los operadores o elementos, lo cual puede complicar el código y aumentar el riesgo de errores

- ¿Por qué es útil separar la calculadora en las fases lexer → parser → evaluator en vez de hacerlo todo en un solo método?

  - para seguir los principios SOLIF (Separación de responsabilidades), ademas de tener un codigo mas mantenible y claro, permitiendo facilitar su reutilizacion.

- Si quisieras añadir soporte para variables (`x = 5`, `y = 2 * x`), ¿en qué parte del código lo implementarías y por qué?

  - Implementar variables requiere ampliar la lógica del parser (para reconocer asignaciones) y del evaluator (para guardar y usar los valores).
  
---
## Solución a un reto propuests por los alumnos
