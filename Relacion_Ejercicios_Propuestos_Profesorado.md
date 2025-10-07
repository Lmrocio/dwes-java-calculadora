# Retos de comprensión 
- Explica qué es un Token y da un ejemplo de cómo aparece en la expresión `3 + 5 * 2`.
 
- ¿Qué diferencia hay entre el Lexer y el Parser?  
- ¿Qué significa que el parser sea recursivo? Pon un ejemplo de función que lo demuestra.
  
# Retos de depuración    

- ¿Qué devuelve la calculadora con la entrada: `2 + 3 * 4` y por qué? 
- ¿Qué ocurre si escribimos una expresión no válida como `2 + *`? ¿Cómo reacciona el código?
- ¿Qué devuelve si calculamos `(2 + 3) ^ 2`?
  
# Retos de modificación    

- Añade la función `tan(x)` usando el mismo esquema que `sin` y `cos`. 
- Modifica el parser para que acepte también la función `sqrt(x)`.
- Haz que la calculadora acepte números negativos explícitos como `-5 + 3`.
  
# Retos de predicción   

- ¿Qué resultado debería devolver esta expresión y por qué?: `cos(0) + sin(90)` (recuerda: las funciones trigonométricas usan radianes).
  
  El resultado obtenido a la hora de realizar es  `cos(0) + sin(90) = 1.8939966636` dado que en el programa no se convierte el sin(90º) sino que lo realiza por medio del sin(90 radianes),dando asi a que el resultado de realizar la operacion no sea exactamente 1.
  
  siendo:    
  cos(0) = 1    
  sin(90) = 0.8939966636    
- ¿Cuál es el resultado de `2 ^ 3 ^ 2`? Explica por qué según la precedencia implementada.
  
  El resultado a la hora de realizar la operacion es  `2 ^ 3 ^ 2 = 512.0` dado a que el exponente '^' es asociativo a la derecha tenemos que:
  `2 ^ 3 ^ 2 = 2 ^ (3 ^ 2) = 2 ^ 9 = 512.0`
  
- ¿Qué devuelve la calculadora con `(2 + 3) * (4 + 5)`?

  El resultado a la hora de realizar la operacion es  `(2 + 3) * (4 + 5) = 45.0` debido a la propiedad que tienen las matematicas de que las operaciones realizadas dentro de los paretesis se realizan con anterioridad dando que :
  
  `(2 + 3) * (4 + 5) = (5) * (9) = 45.0`
  
# Retos de diseño    

- El código actual usa un parser recursivo. ¿Qué ventaja tiene frente a procesar los tokens con un bucle y pila manual?
- ¿Por qué es útil separar la calculadora en las fases lexer → parser → evaluator en vez de hacerlo todo en un solo método? 
- Si quisieras añadir soporte para variables (`x = 5`, `y = 2 * x`), ¿en qué parte del código lo implementarías y por qué?
