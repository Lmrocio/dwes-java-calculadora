# Calc21 — Calculadora educativa (Java 21)

## Índice

1. [Detalles e instrucciones sobre el código](#detalles-e-instrucciones-sobre-el-c%C3%B3digo)
2. [Soluciones a los retos propuestos por el profesor](#soluciones-a-los-retos-propuestos-por-el-profesor)

    - [Retos de comprensión](#retos-de-comprensi%C3%B3n)
    - [Retos de depuración](#retos-de-depuraci%C3%B3n)
    - [Retos de modificación](#retos-de-modificaci%C3%B3n)
    - [Retos de predicción](#retos-de-predicci%C3%B3n)
    - [Retos de diseño](#retos-de-dise%C3%B1o)
3. [Solución a un reto propuesto por los alumnos](#soluci%C3%B3n-a-un-reto-propuesto-por-los-alumnos)

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

Un Token es la unidad atómica de significado que reconoce el analizador léxico (lexer) de un lenguaje de programación. Es decir, cuando escribimos un código, lo primero que hace el compilador/intérprete es dividir el texto en tokens que tienen un tipo (número, identificador, operador, etc.) y un lexema (texto original); además, puede llevar datos extras como valor numérico o posición.

En el ejemplo:

| Token | Tipo   | Lexema |
|-------|--------|-------|
| 1     | `NUMBER` | `3` |
| 2     | `PLUS`   | `+` |
| 3     | `NUMBER` | `5` |
| 4     | `MUL`    | `*` |
| 5     | `NUMBER` | `2` |

El tokenizado es importante porque reduce la ambigüedad de caracteres y permite al `parser` trabajar con piezas ya categorizadas.

- ¿Qué diferencia hay entre el Lexer y el Parser?

El lexer es el programa o componente que lee la secuencia de caracteres y la convierte en una secuencia de tokens, para lo que usa expresiones regulares (`regex`) o autómatas finitos. Su función se desarrolla leyendo dichos caracteres para agruparlos según los patrones (números, identificadores, etc.), para devolver los tokens de uno en uno al parser.

Por otro lado, el `parser` (analizador sintáctico) toma la secuencia de tokens para comprobar/crear la estructura sintáctica según la gramática del lenguaje. El `parser` genera un árbol de parseo, que contiene nodos por cada producción de la gramática, o un árbol de sintaxis abstracta, es una versión más simple que el anterior ya que se suprimen las redundancias. Con este proceso, el `parser` verifica/decide:

  1. Orden y anidamiento correcto.
  2. Precedencia y asociatividad de los operadores. (Por ejemplo, * antes que +)
  3. Estructuras válidas según las reglas. (If, while, expresiones, etc.)

- ¿Qué significa que el parser sea recursivo? Pon un ejemplo de función que lo demuestra.

Un parser recursivo implementa cada regla de la gramática como una función, y estas se llaman entre sí para analizar estructuras anidadas. Es un tipo de parser muy práctico porque las reglas de un lenguaje suelen ser recursivas, es decir, nos encontramos expresiones dentro de expresiones, paréntesis, etc. Una de sus principales ventajas es que permiten un mayor control sobre el manejo de errores y la construcción de árboles de sintaxis abstracta.
Ejemplo:

```
java
Expr parseExpression() {
    Expr node = parseTerm();
    while (current.type == TokenType.PLUS || current.type == TokenType.MINUS) {
        Token op = current; eat(op.type);
        Expr right = parseTerm();
        node = new BinaryExpr(node, op, right);
    }
    return node;
}
Expr parseTerm() {
    Expr node = parseFactor();
    while (current.type == TokenType.MUL || current.type == TokenType.DIV) {
        Token op = current; eat(op.type);
        Expr right = parseFactor();
        node = new BinaryExpr(node, op, right);
    }
    return node;
}
Expr parseFactor() {
    if (current.type == TokenType.NUMBER) {
        int v = Integer.parseInt(current.lexeme);
        eat(TokenType.NUMBER);
        return new NumberExpr(v);
    } else if (current.type == TokenType.LPAREN) {
        eat(TokenType.LPAREN);
        Expr e = parseExpression(); // <-- aquí hay recursión: factor -> expression -> (term -> factor -> ...)
        eat(TokenType.RPAREN);
        return e;
    } else {
        throw new RuntimeException("Sintaxis inválida");
    }
}
```
En este ejemplo, encontramos la recursión en:

- ``parseExpression()`` llama a ``parseTerm()``; ``parseTerm()`` llama a ``parseFactor()``; ``parseFactor()`` puede llamar a ``parseExpression()`` otra vez si encuentra '('.
- Esa llamada ``parseFactor()`` → ``parseExpression()`` → ... es precisamente la recursión necesaria para analizar paréntesis y expresiones anidadas.

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
  - Aquí simplemente hemos añadido un caso en el evaluator para que cuando detecte que es un funcion tambien acepte la funcion tan() y utilice la libreria de Math para calcular la tangente de x 

```java
    case Call c -> {
    double x = eval(c.arg());
    yield switch (c.name()) {
            case "sin" -> Math.sin(x);
            case "cos" -> Math.cos(x);
            case "tan" -> Math.tan(x);
            default -> throw new IllegalArgumentException("Función no soportada: " + c.name());
            };
    }
```

- Modifica el parser para que acepte también la función `sqrt(x)`.
  - En el parser no hace falta cambiar nada para que acepte una función nueva, como hemos hecho antes solo hace falta añadir un nuevo caso de una función, añadiendo solo esta línea:
  ```java
    case "sqrt" -> Math.sqrt(x);
  ```
    
- Haz que la calculadora acepte números negativos explícitos como `-5 + 3`.
  - La calculadora ya acepta numeros negativos explícitos como el que sale en el ejemplo.
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
## Solución a un reto propuesto por los alumnos

En nuestro caso, hemos elegido un reto propuesto por César: Hacer una función para limpiar la pantalla sin eliminar el resultado.

El fragmento de código implementa una forma sencilla de “limpiar” la consola sin borrar el resultado previo del programa, simulando un efecto de limpieza visual al desplazar el contenido existente hacia arriba mediante saltos de línea.

````
java
if (line.equalsIgnoreCase("clear")) {
    for (int i = 0; i < 20; i++) {
        System.out.print("\n");
    }
    System.out.println(HELP);
    continue;
}
````

Explicación paso a paso:

1. Condición de entrada (if (line.equalsIgnoreCase("clear"))): Comprueba si el texto introducido por el usuario es “clear”, ignorando mayúsculas o minúsculas gracias al método equalsIgnoreCase(). Esto permite aceptar entradas como clear, Clear o CLEAR.

2. Limpieza visual de la consola: Dentro del bloque if, se ejecuta un bucle for que imprime 20 saltos de línea (\n). Este método no borra realmente el contenido de la consola, pero lo “desplaza” fuera de la vista, generando el mismo efecto que una limpieza de pantalla en la mayoría de entornos de consola básicos (como la terminal integrada de un IDE o CMD).

3. Reimpresión de la ayuda (System.out.println(HELP)): Después de “limpiar” la consola, se muestra nuevamente la variable HELP, que probablemente contiene información útil o un menú de ayuda para el usuario. Esto refuerza la idea de que la pantalla se limpia pero sin perder el contexto o el contenido importante del programa.

4. Instrucción continue: Hace que el programa salte al siguiente ciclo del bucle principal, evitando ejecutar el resto del código asociado a otras instrucciones. Esto garantiza que, tras limpiar la consola, el flujo del programa continúe correctamente.

En Java, no existe una función nativa universal para limpiar la consola, ya que su comportamiento depende del sistema operativo y del entorno de ejecución. Por ello, imprimir múltiples saltos de línea es una solución práctica, multiplataforma y segura para simular la limpieza visual de la pantalla, especialmente en ejercicios o aplicaciones de consola simples. Además, el uso de equalsIgnoreCase() mejora la usabilidad al permitir al usuario escribir el comando sin preocuparse por las mayúsculas.
