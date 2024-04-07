/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codigo;

import java.awt.Color;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java_cup.runtime.Symbol;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JOptionPane;

/**
 *
 * @author emyma
 */
public class FrmCompilador extends javax.swing.JFrame {

    /**
     * Creates new form FrmCompilador
     */
    DefaultTableModel modelo = new DefaultTableModel();
    ArrayList<ArrayList<Object>> tablaSimbolos = new ArrayList<>();
    private Map<String, String> variables;
    
    public FrmCompilador() {
        initComponents();
        this.getContentPane().setBackground(Color.white);
        this.variables = new HashMap<>();
        this.setTitle("Compilador");
    }
    
    public void analizarDeclaracion(String declaracion) {
        String[] partes = declaracion.split(" ");
        if (partes.length < 2) {
            System.out.println("Error: Declaracion incompleta");
            return;
        }

        String tipo = partes[0];
        String nombre = partes[1];

        if (!validarTipo(tipo)) {
            System.out.println("Error: Tipo de dato no valido");
            return;
        }

        if (variables.containsKey(nombre)) {
            System.out.println("Error: La variable " + nombre + " ya ha sido declarada");
            return;
        }

        if (partes.length > 2) {
            String asignacion = declaracion.substring(declaracion.indexOf('=') + 1).trim();
            if (!validarAsignacion(tipo, asignacion)) {
                System.out.println("Error: Asignacion incorrecta para el tipo de dato " + tipo);
                return;
            }
            // Verificar asignación específica para tipo float
            if (tipo.equals("float") && !asignacion.contains(".")) {
                System.out.println("Error: Asignacion incorrecta para el tipo de dato " + tipo);
                return;
            }
            // Evaluar la expresión si hay una asignación
            evaluarExpresion(asignacion);
        }

        variables.put(nombre, tipo);
        System.out.println("Declaracion exitosa: " + tipo + " " + nombre);
    }
    
    private boolean validarTipo(String tipo) {
        return tipo.equals("int") || tipo.equals("float") || tipo.equals("string");
    }

    private boolean validarAsignacion(String tipo, String asignacion) {
        if (tipo.equals("int")) {
            try {
                Integer.parseInt(asignacion);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (tipo.equals("float")) {
            try {
                Float.parseFloat(asignacion);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (tipo.equals("string")) {
            return asignacion.startsWith("\"") && asignacion.endsWith("\"");
        }
        return false;
    }


    private void evaluarExpresion(String expresion) {
        try {
            // Reemplazar los espacios en blanco y eliminar cualquier posible espacio al inicio o al final
            expresion = expresion.replaceAll("\\s+", "");

            // Utilizar la función eval() de JavaScript para evaluar la expresión
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("javascript");
            Object resultado = engine.eval(expresion);

            System.out.println("Resultado de la expresion: " + resultado.toString());
        } catch (ScriptException e) {
            System.out.println("Error al evaluar la expresion: " + e.getMessage());
        }
    }
    
    private String translateToCpp(String javaCode) {
        
        int openBraceIndex = javaCode.indexOf("{");
        if (openBraceIndex != -1) {
            javaCode = "#include <iostream>\nusing namespace std;\n" + javaCode.substring(0, openBraceIndex) + javaCode.substring(openBraceIndex);
        }
        
        //Reemplazo de la funcion principal
        javaCode = javaCode.replaceAll("start\\{", "int main() {\n");
        
        //Reemplazo de la expresion "cout"
        javaCode = javaCode.replaceAll("imprimir\\((.*?)\\)", "cout << $1 << endl;");
        
        //Remplazo de la expresion "if"
        javaCode = javaCode.replaceAll("si\\((.*?)\\)\\{", "if($1) {\n");
        
        //Remplazo de la expresion "else if"
        javaCode = javaCode.replaceAll("o_sino\\s*\\(\\s*(.*?)\\)\\s*\\{", "else if ($1) {");
        
        //Remplazo de la expresion "else"
        javaCode = javaCode.replaceAll("sino\\s*\\{", "else {");
        
        //Remplazo de la expresion "for"
        javaCode = javaCode.replaceAll("para\\s*\\(\\s*(.*?);\\s*(.*?);\\s*(.*?)\\)\\s*\\{", "for ($1; $2; $3) {");
        
        //Remplazo de la expresion "while"
        javaCode = javaCode.replaceAll("mientras\\s*\\(\\s*(.*?)\\)\\s*\\{", "while ($1) {");
              
        //Reemplazar la declaracion de un array
        javaCode = javaCode.replaceAll("(\\b(int|float)\\s+)(\\w+)\\[(.*?)\\]\\s*;", "$1$3[$4];");
        
        return javaCode;
    }
   
    private String generarCodigoIntermedio(String codigo) {
        StringBuilder codigoIntermedio = new StringBuilder();
        String[] lineas = codigo.split("\\n");

        for (int i = 0; i < lineas.length; i++) {
            codigoIntermedio.append("Código intermedio: ").append(lineas[i]).append("\n");
        }

        return codigoIntermedio.toString();
    }

    
    private void analizarLexico() throws IOException{
        int cont = 1;
        
        String expr = (String) txtCodigo.getText();
        Lexer lexer = new Lexer(new StringReader(expr));
        String resultado = "LINEA " + cont + "\t\tSIMBOLO\n";
        while(true){
            Tokens token = lexer.yylex();
            if(token == null){
                txtSalidaLex.setText(resultado);
                return;
            }
            
            ArrayList<Object> fila = new ArrayList<>();
            fila.add(token.toString());  // Token
            fila.add(lexer.lexeme);      // Lexema
            fila.add(cont);              // Fila (número de línea)
            fila.add(lexer.lexeme.length()); // Columna (longitud del lexema)
            tablaSimbolos.add(fila);
            
            switch(token){
                case Linea:
                    cont++;
                    resultado += "LINEA " + cont + "\n";
                    break;
                case Int:
                    resultado += "  <Reservada int>\t" + lexer.lexeme + "\n";
                    break;
                case Float:
                    resultado += "  <Reservada float>\t" + lexer.lexeme + "\n";
                    break;
                case If:
                    resultado += "  <Reservada if>\t" + lexer.lexeme + "\n";
                    break;
                case Else:
                    resultado += "  <Reservada else>\t" + lexer.lexeme + "\n";
                    break;
                case Else_If:
                    resultado += "  <Reservada else_if>\t" + lexer.lexeme + "\n";
                    break;
                case For:
                    resultado += "  <Reservada For>\t" + lexer.lexeme + "\n";
                    break;
                case While:
                    resultado += "  <Reservada while>\t" + lexer.lexeme + "\n";
                    break;
                case Imprimir:
                    resultado += "  <Reservada imprimir>\t" + lexer.lexeme + "\n";
                    break;
                case ParentesisAbierto:
                    resultado += "  <Parentesis de apertura>\t" + lexer.lexeme + "\n";
                    break;
                case ParentesisCerrado:
                    resultado += "  <Parentesis de cierre>\t" + lexer.lexeme + "\n";
                    break;
                case LlaveAbierto:
                    resultado += "  <Llave de apertura>\t" + lexer.lexeme + "\n";
                    break;
                case LlaveCerrado:
                    resultado += "  <Llave de cierre>\t" + lexer.lexeme + "\n";
                    break;
                case Arreglo:
                    resultado += "  <Creacion de arreglo>\t" + lexer.lexeme + "\n";
                    break;
                case OP_Igualdad:
                    resultado += "  <Operador igualdad>\t" + lexer.lexeme + "\n";
                    break;
                case OP_Mayor:
                    resultado += "  <Operador mayor>\t" + lexer.lexeme + "\n";
                    break;
                case OP_Menor:
                    resultado += "  <Operador menor>\t" + lexer.lexeme + "\n";
                    break;
                case OP_MayorOIgual:
                    resultado += "  <Operador mayor o igual>\t" + lexer.lexeme + "\n";
                    break;
                case OP_MenorOIgual:
                    resultado += "  <Operador menor o igual>\t" + lexer.lexeme + "\n";
                    break;
                case OP_NoIgual:
                    resultado += "  <Operador desigualdad>\t" + lexer.lexeme + "\n";
                    break;
                case Igual:
                    resultado += "  <Operador Igual>\t" + lexer.lexeme + "\n";
                    break;
                case Suma:
                    resultado += "  <Operador Suma>\t" + lexer.lexeme + "\n";
                    break;
                case Resta:
                    resultado += "  <Operador Resta>\t" + lexer.lexeme + "\n";
                    break;
                case Multiplicacion:
                    resultado += "  <Operador Multiplicacion>\t" + lexer.lexeme + "\n";
                    break;
                case Division:
                    resultado += "  <Operador Division>\t" + lexer.lexeme + "\n";
                    break;
                case Modulo:
                    resultado += "  <Operador Modulo>\t" + lexer.lexeme + "\n";
                    break;
                case Start:
                    resultado += "  <Reservada main>\t" + lexer.lexeme + "\n";
                    break;
                case NumeroDecimal:
                    resultado += "  <Numero Decimal>\t" + lexer.lexeme + "\n";
                    break;
                case Identificador:
                    resultado += "  <Identificador>\t" + lexer.lexeme + "\n";
                    break;
                case Numero:
                    resultado += "  <Numero>\t\t" + lexer.lexeme + "\n";
                    break;
                case Cadena:
                    resultado += "  <Cadena>\t\t" + lexer.lexeme + "\n";
                    break;
                case Error:
                    resultado += "  <Simbolo no definido>\t";
                    break;
                default:
                    resultado += "  < " + lexer.lexeme + " >\n";
                    break;
                
            }
        }
    }
    
    private boolean esDeclaracionValida(String linea) {
        // Utiliza una expresión regular para identificar líneas de declaración válidas
        return linea.matches("\\s*(int|float|string)\\s+\\w+\\s*(=.+)?\\s*");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtCodigo = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtSalidaLex = new javax.swing.JTextArea();
        btnAnalizarLex = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtSalidaSin = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        btnAnalizarSin = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblSimbolos = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        btnTabla = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        txtSalidaInter = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        btnIntermedio = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        txtTraductor = new javax.swing.JTextArea();
        btnTraducir = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        txtSalidaSem = new javax.swing.JTextArea();
        btnAnalizarSem = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Compilador");

        txtCodigo.setColumns(20);
        txtCodigo.setRows(5);
        jScrollPane1.setViewportView(txtCodigo);

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Analizador Lexico");

        txtSalidaLex.setEditable(false);
        txtSalidaLex.setColumns(20);
        txtSalidaLex.setRows(5);
        jScrollPane2.setViewportView(txtSalidaLex);

        btnAnalizarLex.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        btnAnalizarLex.setText("Analizar");
        btnAnalizarLex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnalizarLexActionPerformed(evt);
            }
        });

        txtSalidaSin.setEditable(false);
        txtSalidaSin.setColumns(20);
        txtSalidaSin.setRows(5);
        jScrollPane3.setViewportView(txtSalidaSin);

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel3.setText("Analizador Sintactico");

        btnAnalizarSin.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        btnAnalizarSin.setText("Analizar");
        btnAnalizarSin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnalizarSinActionPerformed(evt);
            }
        });

        tblSimbolos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Token", "Lexema", "Fila", "Columna"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(tblSimbolos);

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel4.setText("Tabla de Simbolos");

        btnTabla.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        btnTabla.setText("Generar");
        btnTabla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTablaActionPerformed(evt);
            }
        });

        txtSalidaInter.setEditable(false);
        txtSalidaInter.setColumns(20);
        txtSalidaInter.setRows(5);
        jScrollPane5.setViewportView(txtSalidaInter);

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel5.setText("Codigo Intermedio");

        btnIntermedio.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        btnIntermedio.setText("Generar");
        btnIntermedio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIntermedioActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel6.setText("Traductor de Codigo");

        txtTraductor.setEditable(false);
        txtTraductor.setColumns(20);
        txtTraductor.setRows(5);
        jScrollPane6.setViewportView(txtTraductor);

        btnTraducir.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        btnTraducir.setText("Traducir");
        btnTraducir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTraducirActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel7.setText("Analizador Semantico");

        txtSalidaSem.setEditable(false);
        txtSalidaSem.setColumns(20);
        txtSalidaSem.setRows(5);
        jScrollPane7.setViewportView(txtSalidaSem);

        btnAnalizarSem.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        btnAnalizarSem.setText("Analizar");
        btnAnalizarSem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnalizarSemActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(401, 401, 401)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addGap(119, 119, 119))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(108, 108, 108)
                                        .addComponent(btnAnalizarSin)
                                        .addGap(202, 202, 202)
                                        .addComponent(btnIntermedio))
                                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 474, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(163, 163, 163)
                                        .addComponent(jLabel4))))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(146, 146, 146)
                        .addComponent(jLabel3)
                        .addGap(141, 141, 141)
                        .addComponent(jLabel5))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(104, 104, 104)
                        .addComponent(btnAnalizarLex))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(482, 482, 482)
                        .addComponent(btnTabla)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(60, 60, 60)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(147, 147, 147)
                                .addComponent(btnTraducir, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(57, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(116, 116, 116))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(btnAnalizarSem, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(150, 150, 150))))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2)
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAnalizarLex)
                                .addGap(81, 81, 81))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(btnAnalizarSin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(btnIntermedio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(17, 17, 17)
                                        .addComponent(btnTraducir, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(3, 3, 3)
                                        .addComponent(jScrollPane7)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnTabla)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(btnAnalizarSem, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap())))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAnalizarLexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnalizarLexActionPerformed
        // TODO add your handling code here:
        try {
            analizarLexico();
        } catch (IOException ex) {
            Logger.getLogger(FrmCompilador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAnalizarLexActionPerformed

    private void btnAnalizarSinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnalizarSinActionPerformed
        // TODO add your handling code here:
        String ST = txtCodigo.getText();
        Sintax s = new Sintax(new codigo.LexerCup(new StringReader(ST)));

        try {
            s.parse();
            txtSalidaSin.setText("Analisis realizado correctamente");
            txtSalidaSin.setForeground(new Color(25, 111, 61));

        } catch (Exception ex) {
            Symbol sym = s.getS();
            txtSalidaSin.setText("Error de sintaxis. Linea: " + (sym.right + 1) + " Columna: " + (sym.left + 1) + ", Texto: \"" + sym.value + "\"");
            txtSalidaSin.setForeground(Color.red);

        }
    }//GEN-LAST:event_btnAnalizarSinActionPerformed

    private void btnTablaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTablaActionPerformed
        // TODO add your handling code here:// Limpia el modelo de la tabla
        modelo = new DefaultTableModel();
        tblSimbolos.setModel(modelo);

        // Define las columnas de la tabla
        modelo.addColumn("Token");
        modelo.addColumn("Lexema");
        modelo.addColumn("Fila");
        modelo.addColumn("Columna");

        // Llena la tabla con los datos de tablaSimbolos
        for (ArrayList<Object> fila : tablaSimbolos) {
            modelo.addRow(fila.toArray());
        }
        
    }//GEN-LAST:event_btnTablaActionPerformed

    private void btnIntermedioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIntermedioActionPerformed
        // TODO add your handling code here:
        try{
            String codigoFuente = txtCodigo.getText();
            String codigoIntermedio = generarCodigoIntermedio(codigoFuente);
            txtSalidaInter.setText(codigoIntermedio);
        }catch(Exception ex){
            JOptionPane.showMessageDialog(rootPane, ex);
            
        }
    }//GEN-LAST:event_btnIntermedioActionPerformed

    private void btnTraducirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTraducirActionPerformed
        // TODO add your handling code here:
        String javaCode = txtCodigo.getText();

        String cppCode = translateToCpp(javaCode);
        txtTraductor.setText(cppCode);
    }//GEN-LAST:event_btnTraducirActionPerformed

    private void btnAnalizarSemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnalizarSemActionPerformed
        // TODO add your handling code here:
        // Obtener el texto de entrada
        String entrada = txtCodigo.getText();
        FrmCompilador analizador = new FrmCompilador();

        // Reemplazar el System.out.println por la manipulación del texto en txtResultado
        StringBuilder resultado = new StringBuilder();

        // Redirigir la salida al txtResultado
        System.setOut(new java.io.PrintStream(new java.io.OutputStream() {
            @Override
            public void write(int b) {
                resultado.append((char) b);
            }
        }));

        // Dividir las líneas de entrada
        String[] lineas = entrada.split("\n");

        // Procesar cada línea
        for (String linea : lineas) {
            // Filtrar líneas que no son declaraciones válidas y no son espacios en blanco ni tabulaciones
            if (!linea.trim().isEmpty() && esDeclaracionValida(linea)) {
                analizador.analizarDeclaracion(linea);
            }
        }

        // Mostrar el resultado en txtResultado
        txtSalidaSem.setText(resultado.toString());
    }//GEN-LAST:event_btnAnalizarSemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrmCompilador.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmCompilador.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmCompilador.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmCompilador.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrmCompilador().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAnalizarLex;
    private javax.swing.JButton btnAnalizarSem;
    private javax.swing.JButton btnAnalizarSin;
    private javax.swing.JButton btnIntermedio;
    private javax.swing.JButton btnTabla;
    private javax.swing.JButton btnTraducir;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTable tblSimbolos;
    private javax.swing.JTextArea txtCodigo;
    private javax.swing.JTextArea txtSalidaInter;
    private javax.swing.JTextArea txtSalidaLex;
    private javax.swing.JTextArea txtSalidaSem;
    private javax.swing.JTextArea txtSalidaSin;
    private javax.swing.JTextArea txtTraductor;
    // End of variables declaration//GEN-END:variables
}
