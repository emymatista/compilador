package codigo;
import java_cup.runtime.Symbol;
%%
%class LexerCup
%type java_cup.runtime.Symbol
%cup
%full
%line
%char
L=[a-zA-Z_]+
D=[0-9]+
espacio=[ ,\t,\r,\n]+
%{
    private Symbol symbol(int type, Object value){
        return new Symbol(type, yyline, yycolumn, value);
    }
    private Symbol symbol(int type){
        return new Symbol(type, yyline, yycolumn);
    }
%}
%%
int {return new Symbol(sym.Int, yychar, yyline, yytext());}
float {return new Symbol(sym.Float, yychar, yyline, yytext());}
string {return new Symbol(sym.String, yychar, yyline, yytext());}
si | if {return new Symbol(sym.If, yychar, yyline, yytext());}
else | sino {return new Symbol(sym.Else, yychar, yyline, yytext());}
else if | o_sino {return new Symbol(sym.Else_If, yychar, yyline, yytext());}
para | for {return new Symbol(sym.For, yychar, yyline, yytext());}
mientras | while {return new Symbol(sym.While, yychar, yyline, yytext());}
print | imprimir {return new Symbol(sym.Imprimir, yychar, yyline, yytext());}
{espacio} {/*Ignore*/}
"//".* {/*Ignore*/}
"(" {return new Symbol(sym.ParentesisAbierto, yychar, yyline, yytext());}
")" {return new Symbol(sym.ParentesisCerrado, yychar, yyline, yytext());}
"{" {return new Symbol(sym.LlaveAbierto, yychar, yyline, yytext());}
"}" {return new Symbol(sym.LlaveCerrado, yychar, yyline, yytext());}
"[]" {return new Symbol(sym.Arreglo, yychar, yyline, yytext());}
"==" {return new Symbol(sym.OP_Igualdad, yychar, yyline, yytext());}
">" {return new Symbol(sym.OP_Mayor, yychar, yyline, yytext());}
"<" {return new Symbol(sym.OP_Menor, yychar, yyline, yytext());}
">=" {return new Symbol(sym.OP_MayorOIgual, yychar, yyline, yytext());}
"<=" {return new Symbol(sym.OP_MenorOIgual, yychar, yyline, yytext());}
"!=" {return new Symbol(sym.OP_NoIgual, yychar, yyline, yytext());}
"=" {return new Symbol(sym.Igual, yychar, yyline, yytext());}
"+" {return new Symbol(sym.Suma, yychar, yyline, yytext());}
"-" {return new Symbol(sym.Resta, yychar, yyline, yytext());}
"*" {return new Symbol(sym.Multiplicacion, yychar, yyline, yytext());}
"**" {return new Symbol(sym.Exponenciacion, yychar, yyline, yytext());}
"/" {return new Symbol(sym.Division, yychar, yyline, yytext());}
"%" {return new Symbol(sym.Modulo, yychar, yyline, yytext());}
"start" {return new Symbol(sym.Start, yychar, yyline, yytext());}
{D}+"."{D}* {return new Symbol(sym.NumeroDecimal, yychar, yyline, yytext());}
{L}({L}|{D})* {return new Symbol(sym.Identificador, yychar, yyline, yytext());}
("(-"{D}+")")|{D}+ {return new Symbol(sym.Numero, yychar, yyline, yytext());}
\"([^\"\n]|\\.)*\" {return new Symbol(sym.Cadena, yychar, yyline, yytext());}
 . {return new Symbol(sym.Error, yychar, yyline, yytext());}

