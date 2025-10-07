package com.example.calc;

import java.util.List;
import java.util.Scanner;

85public class Main {
    private static final String HELP = """
        === Calc21 ===
        Operaciones: +  -  *  /  ^    Funciones: sin(x), cos(x), tan(x) (en pi/rad)
        Precedencia: ^ (derecha), luego * /, luego + -
        Ejemplos:
          1 + 2*3
          (1 + 2) * 3 ^ 2
          -2 ^ 3
          sin(3.14159/2) + cos(0)
          tan(45) * sqrt(9)
        Escribe 'exit' para salir.
        """;

    public static void main(String[] args) {
        System.out.println(HELP);
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine();
            if (line == null) break;
            line = line.trim();
            if (line.equalsIgnoreCase("exit")) break;

            // Reto de César (4): Hacer una función para limpiar la pantalla sin eliminar el resultado: añadir un comando clear que limpia la consola sin salir del programa y borrar el resultado (sin reiniciar).
            if (line.equalsIgnoreCase("clear")) {
                for (int i = 0; i < 20; i++) {
                    System.out.print("\n");
                }
                System.out.println(HELP);
                continue;
            };

            if (line.isBlank()) continue;
            try {
                List<Token> tokens = new Lexer(line).lex();
                Expr ast = new Parser(tokens).parse();
                double result = Evaluator.eval(ast);
                System.out.println(result);
            } catch (IllegalArgumentException ex) {
                System.err.println("Error: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("Error inesperado: " + ex);
            }
        }
        System.out.println("Adiós!");
    }
}
