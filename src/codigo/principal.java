/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codigo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author emyma
 */
public class principal {
    public static void main(String[] args) throws Exception {
        String ruta = "C:/Users/emyma/OneDrive/Documentos/NetBeansProjects/Compilador/src/codigo/Lexer.flex";
        String ruta2 = "C:/Users/emyma/OneDrive/Documentos/NetBeansProjects/Compilador/src/codigo/LexerCup.flex";
        String[] rutaS = {"-parser", "Sintax", "C:/Users/emyma/OneDrive/Documentos/NetBeansProjects/Compilador/src/codigo/Sintax.cup"};
        generar(ruta, ruta2, rutaS);
    }
    
    public static void generar(String ruta, String ruta2, String[] rutaS) throws IOException, Exception{
        File archivo;
        archivo = new File (ruta); 
        JFlex.Main.generate(archivo);
        archivo = new File (ruta2); 
        JFlex.Main.generate(archivo);
        java_cup.Main.main(rutaS);
        
        Path rutaSym = Paths.get("C:/Users/emyma/OneDrive/Documentos/NetBeansProjects/Compilador/src/codigo/sym.java");
        if(Files.exists(rutaSym)){
            Files.delete(rutaSym);
        }
        Files.move(
                Paths.get("C:/Users/emyma/OneDrive/Documentos/NetBeansProjects/Compilador/sym.java"), 
                Paths.get("C:/Users/emyma/OneDrive/Documentos/NetBeansProjects/Compilador/src/codigo/sym.java")
        );
        Path rutaSin = Paths.get("C:/Users/emyma/OneDrive/Documentos/NetBeansProjects/Compilador/src/codigo/Sintax.java");
        if(Files.exists(rutaSin)){
            Files.delete(rutaSin);
        }
        Files.move(
                Paths.get("C:/Users/emyma/OneDrive/Documentos/NetBeansProjects/Compilador/Sintax.java"), 
                Paths.get("C:/Users/emyma/OneDrive/Documentos/NetBeansProjects/Compilador/src/codigo/Sintax.java")
        );   
    }
}

